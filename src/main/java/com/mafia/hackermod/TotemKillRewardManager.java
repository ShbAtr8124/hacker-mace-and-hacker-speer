package com.mafia.hackermod;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TotemKillRewardManager {
    private static final Map<UUID, Integer> KILL_COUNTS = new HashMap<>();

    private static final Item[] RANDOM_TOTEMS = new Item[] {
            MafiaHackerMod.TOTEM_OF_INVENTORY,
            MafiaHackerMod.TOTEM_OF_DROPPING,
            MafiaHackerMod.TOTEM_OF_IMMORTALITY,
            MafiaHackerMod.TOTEM_OF_CRYSTAL_PROTECTION,
            MafiaHackerMod.TOTEM_OF_DOUBLE_KNOCKBACK
    };

    private static final Item[] ALL_TOTEMS = new Item[] {
            MafiaHackerMod.TOTEM_OF_INVENTORY,
            MafiaHackerMod.TOTEM_OF_DROPPING,
            MafiaHackerMod.TOTEM_OF_IMMORTALITY,
            MafiaHackerMod.TOTEM_OF_CRYSTAL_PROTECTION,
            MafiaHackerMod.TOTEM_OF_DOUBLE_KNOCKBACK
    };

    private TotemKillRewardManager() {}

    public static void init() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((player, entity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
            if (!(entity instanceof LivingEntity)) return;

            UUID uuid = serverPlayer.getUuid();
            int kills = KILL_COUNTS.getOrDefault(uuid, 0) + 1;
            KILL_COUNTS.put(uuid, kills);

            if (kills % 2500 == 0) {
                giveItems(serverPlayer, ALL_TOTEMS, "§aYou reached 2500 kills and got all Totems!");
                return;
            }

            if (kills % 500 == 0) {
                Item reward = RANDOM_TOTEMS[ThreadLocalRandom.current().nextInt(RANDOM_TOTEMS.length)];
                giveItems(serverPlayer, new Item[] { reward }, "§aYou reached 500 kills and got a random Totem!");
            }
        });
    }

    private static void giveItems(ServerPlayerEntity player, Item[] items, String message) {
        for (Item item : items) {
            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().insertStack(stack)) {
                Vec3d pos = player.getPos();
                ItemEntity entity = new ItemEntity(player.getWorld(), pos.x, pos.y + 1, pos.z, stack);
                player.getWorld().spawnEntity(entity);
            }
        }
        player.sendMessage(Text.of(message), false);
    }
}
