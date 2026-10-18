package com.example.hackeritems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enforces a global exclusivity lock for Hacker Mace items:
 * - If any Hacker Mace exists (in inventories/hands/dropped), no other player may craft a new one.
 * - If a player crafts/obtains a Hacker Mace while another already exists, the newly obtained mace is removed
 *   and the crafting ingredients are returned to the player (or spawned at their position).
 * - The system polls inventories every few ticks and enforces the rule server-side.
 */
public class HackerItemExclusivity {
    private static final int CHECK_INTERVAL = 20; // ticks

    private static final Map<UUID, Integer> lastPerPlayerCount = new HashMap<>();
    private static int lastGlobalCount = 0;
    private static int tickCounter = 0;

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % CHECK_INTERVAL != 0) return;
            enforce(server);
        });
    }

    private static void enforce(MinecraftServer server) {
        int currentGlobal = computeGlobalMaceCount(server);

        // For each player, compare counts
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID uuid = player.getUuid();
            int prev = lastPerPlayerCount.getOrDefault(uuid, 0);
            int now = countMacesInPlayer(player);

            // New acquisition detected
            if (now > prev) {
                // If there was already at least one mace before this tick owned by someone else, block the acquisition
                if (lastGlobalCount > 0 && prev == 0) {
                    // Remove one mace from this player (the newly acquired one)
                    boolean removed = removeOneMaceFromPlayer(player);

                    // Refund crafting ingredients (try to give back to player inventory, otherwise spawn on ground)
                    refundIngredients(server, player);

                    // Notify player and server
                    player.sendMessage(Text.of("Crafting blocked: A Hacker Mace already exists on the server. Your crafted mace was removed and ingredients refunded."), false);
                }
            }

            lastPerPlayerCount.put(uuid, now);
        }

        lastGlobalCount = currentGlobal;
    }

    private static int computeGlobalMaceCount(MinecraftServer server) {
        int total = 0;
        // players
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            total += countMacesInPlayer(player);
        }

        // dropped items
        Box full = new Box(-3.0E7D, -3.0E7D, -3.0E7D, 3.0E7D, 3.0E7D, 3.0E7D);
        for (ServerWorld world : server.getWorlds()) {
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, full, ie -> {
                try {
                    return ie.getStack().getItem() == ModItems.HACKER_MACE;
                } catch (Exception e) {
                    return false;
                }
            });
            for (ItemEntity ie : items) {
                total += ie.getStack().getCount();
            }
        }

        return total;
    }

    private static int countMacesInPlayer(ServerPlayerEntity player) {
        int count = 0;
        try {
            if (player.getMainHandStack().getItem() == ModItems.HACKER_MACE) count += player.getMainHandStack().getCount();
            if (player.getOffHandStack().getItem() == ModItems.HACKER_MACE) count += player.getOffHandStack().getCount();

            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (s.getItem() == ModItems.HACKER_MACE) count += s.getCount();
            }
        } catch (Exception ignored) {}
        return count;
    }

    private static boolean removeOneMaceFromPlayer(ServerPlayerEntity player) {
        try {
            // Check main hand first
            ItemStack main = player.getMainHandStack();
            if (main.getItem() == ModItems.HACKER_MACE) {
                main.decrement(1);
                return true;
            }
            // then offhand
            ItemStack off = player.getOffHandStack();
            if (off.getItem() == ModItems.HACKER_MACE) {
                off.decrement(1);
                return true;
            }
            // then inventory
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (s.getItem() == ModItems.HACKER_MACE) {
                    s.decrement(1);
                    if (s.isEmpty()) player.getInventory().setStack(i, ItemStack.EMPTY);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static void refundIngredients(MinecraftServer server, ServerPlayerEntity player) {
        // Recipe: 6x redstone, 1x golden_apple, 1x minecraft:diamond_spear, 1x lava_bucket
        ItemStack redstone = new ItemStack(Items.REDSTONE, 6);
        ItemStack apple = new ItemStack(Items.GOLDEN_APPLE, 1);
        Item diamondSpearItem = Registries.ITEM.get(Identifier.tryParse("minecraft:diamond_spear"));
        ItemStack diamondSpear = diamondSpearItem != null ? new ItemStack(diamondSpearItem, 1) : new ItemStack(Items.DIAMOND, 1);
        ItemStack lava = new ItemStack(Items.LAVA_BUCKET, 1);

        giveOrSpawn(server, player, redstone);
        giveOrSpawn(server, player, apple);
        giveOrSpawn(server, player, diamondSpear);
        giveOrSpawn(server, player, lava);
    }

    private static void giveOrSpawn(MinecraftServer server, ServerPlayerEntity player, ItemStack stack) {
        boolean added = player.getInventory().insertStack(stack);
        if (!added) {
            // spawn in world at player pos
            ServerWorld world = (ServerWorld) player.world;
            ItemEntity ie = new ItemEntity(world, player.getX(), player.getY() + 1.0, player.getZ(), stack);
            world.spawnEntity(ie);
        }
    }
}
