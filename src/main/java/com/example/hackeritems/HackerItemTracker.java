package com.example.hackeritems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.UUID;

public class HackerItemTracker {
    private static int tickCounter = 0;

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 6000 != 0) return; // alle 5 Minuten (6000 Ticks)
            scan(server);
        });
    }

    private static void scan(MinecraftServer server) {
        // Für jede Welt: Findings sammeln und an alle Online-Spieler senden
        for (ServerWorld world : server.getWorlds()) {
            // 1) Spieler, die die Items halten oder im Inventar haben
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                String abbrName = abbreviate(player.getName().getString());

                // Mainhand
                if (player.getMainHandStack().getItem() == ModItems.HACKER_MACE) {
                    String msg = String.format("HACKER-MACE-TRACKER (%s) bei %s", player.getBlockPos().toShortString(), abbrName);
                    broadcast(server, Text.of(msg));
                }
                if (player.getMainHandStack().getItem() == ModItems.HACKER_SPEAR) {
                    String msg = String.format("HACKER-SPEAR-TRACKER (%s) bei %s", player.getBlockPos().toShortString(), abbrName);
                    broadcast(server, Text.of(msg));
                }

                // Offhand
                if (player.getOffHandStack().getItem() == ModItems.HACKER_MACE) {
                    String msg = String.format("HACKER-MACE-TRACKER (offhand, %s) bei %s", player.getBlockPos().toShortString(), abbrName);
                    broadcast(server, Text.of(msg));
                }
                if (player.getOffHandStack().getItem() == ModItems.HACKER_SPEAR) {
                    String msg = String.format("HACKER-SPEAR-TRACKER (offhand, %s) bei %s", player.getBlockPos().toShortString(), abbrName);
                    broadcast(server, Text.of(msg));
                }

                // Inventory
                try {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_MACE) {
                            String msg = String.format("HACKER-MACE-TRACKER (inv slot %d, %s) bei %s", i, player.getBlockPos().toShortString(), abbrName);
                            broadcast(server, Text.of(msg));
                        }
                        if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_SPEAR) {
                            String msg = String.format("HACKER-SPEAR-TRACKER (inv slot %d, %s) bei %s", i, player.getBlockPos().toShortString(), abbrName);
                            broadcast(server, Text.of(msg));
                        }
                    }
                } catch (Exception ignored) {
                    // ignore inventory scan errors
                }
            }

            // 2) Fallengelassene Item-Entities
            Box full = new Box(-3.0E7D, -3.0E7D, -3.0E7D, 3.0E7D, 3.0E7D, 3.0E7D);
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, full, ie -> {
                try {
                    return ie.getStack().getItem() == ModItems.HACKER_MACE || ie.getStack().getItem() == ModItems.HACKER_SPEAR;
                } catch (Exception e) {
                    return false;
                }
            });

            for (ItemEntity ie : items) {
                String type = ie.getStack().getItem() == ModItems.HACKER_MACE ? "HACKER-MACE-TRACKER" : "HACKER-SPEAR-TRACKER";
                String pos = ie.getBlockPos().toShortString();
                String ownerAbbr = "??";

                // Versuche Owner via NBT "hackeritems:owner" zu lesen (falls vorhanden)
                try {
                    if (ie.getStack().hasNbt() && ie.getStack().getNbt().containsUuid("hackeritems:owner")) {
                        UUID ownerUuid = ie.getStack().getNbt().getUuid("hackeritems:owner");
                        ServerPlayerEntity owner = server.getPlayerManager().getPlayer(ownerUuid);
                        if (owner != null) ownerAbbr = abbreviate(owner.getName().getString());
                    }
                } catch (Exception ignored) {}

                String msg = String.format("%s (%s) liegend bei %s (owner=%s)", type, pos, world.getRegistryKey().getValue().toString(), ownerAbbr);
                broadcast(server, Text.of(msg));
            }
        }
    }

    private static void broadcast(MinecraftServer server, Text message) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            try {
                p.sendMessage(message, false);
            } catch (Exception ignored) {}
        }
    }

    private static String abbreviate(String name) {
        if (name == null || name.isEmpty()) return "??";
        name = name.trim();
        if (name.length() == 1) return name + name;
        String first = name.substring(0, 1);
        String last = name.substring(name.length() - 1);
        return first + last;
    }
}
