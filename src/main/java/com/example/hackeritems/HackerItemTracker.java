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
    private static boolean active = false;
    private static int activeTickCounter = 0;
    private static int lastSeenTick = 0;

    public static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            tickCounter++;

            // Early detection phase: if no tracker active, check every 100 ticks
            if (!active) {
                if (tickCounter % 100 == 0) {
                    if (hasAnyHackerItem(server)) {
                        active = true;
                        activeTickCounter = 0;
                        lastSeenTick = tickCounter;
                        broadcast(server, Text.of("HackerItemTracker aktiviert: Hacker-Item erkannt"));
                    }
                }
                return;
            }

            // Active phase: scan every 6000 ticks = 5 minutes
            activeTickCounter++;
            if (activeTickCounter % 6000 == 0) {
                scanAndBroadcast(server);
            }

            // If a hacker item vanishes for ~30s, turn tracker off automatically
            if (tickCounter % 200 == 0) {
                if (hasAnyHackerItem(server)) {
                    lastSeenTick = tickCounter;
                } else {
                    if (tickCounter - lastSeenTick > 600) {
                        active = false;
                        broadcast(server, Text.of("HackerItemTracker deaktiviert: kein Hacker-Item mehr gefunden"));
                    }
                }
            }
        });
    }

    private static boolean hasAnyHackerItem(MinecraftServer server) {
        // Check players' main/off hand + inventory
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            try {
                if (player.getMainHandStack().getItem() == ModItems.HACKER_MACE) return true;
                if (player.getOffHandStack().getItem() == ModItems.HACKER_MACE) return true;
                if (player.getMainHandStack().getItem() == ModItems.HACKER_SPEAR) return true;
                if (player.getOffHandStack().getItem() == ModItems.HACKER_SPEAR) return true;

                for (int i = 0; i < player.getInventory().size(); i++) {
                    if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_MACE) return true;
                    if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_SPEAR) return true;
                }
            } catch (Exception ignored) {
            }
        }

        // Check dropped entity items in all worlds
        Box full = new Box(-3.0E7D, -3.0E7D, -3.0E7D, 3.0E7D, 3.0E7D, 3.0E7D);
        for (ServerWorld world : server.getWorlds()) {
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, full, ie -> {
                try {
                    return ie.getStack().getItem() == ModItems.HACKER_MACE || ie.getStack().getItem() == ModItems.HACKER_SPEAR;
                } catch (Exception e) {
                    return false;
                }
            });
            if (!items.isEmpty()) return true;
        }

        return false;
    }

    private static void scanAndBroadcast(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                String abbrName = abbreviate(player.getName().getString());

                if (player.getMainHandStack().getItem() == ModItems.HACKER_MACE) {
                    broadcast(server, Text.of(String.format("HACKER-MACE-TRACKER (%s) bei %s", player.getBlockPos().toShortString(), abbrName)));
                }
                if (player.getMainHandStack().getItem() == ModItems.HACKER_SPEAR) {
                    broadcast(server, Text.of(String.format("HACKER-SPEAR-TRACKER (%s) bei %s", player.getBlockPos().toShortString(), abbrName)));
                }
                if (player.getOffHandStack().getItem() == ModItems.HACKER_MACE) {
                    broadcast(server, Text.of(String.format("HACKER-MACE-TRACKER (offhand, %s) bei %s", player.getBlockPos().toShortString(), abbrName)));
                }
                if (player.getOffHandStack().getItem() == ModItems.HACKER_SPEAR) {
                    broadcast(server, Text.of(String.format("HACKER-SPEAR-TRACKER (offhand, %s) bei %s", player.getBlockPos().toShortString(), abbrName)));
                }

                try {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_MACE) {
                            broadcast(server, Text.of(String.format("HACKER-MACE-TRACKER (inv slot %d) bei %s", i, abbrName)));
                        }
                        if (player.getInventory().getStack(i).getItem() == ModItems.HACKER_SPEAR) {
                            broadcast(server, Text.of(String.format("HACKER-SPEAR-TRACKER (inv slot %d) bei %s", i, abbrName)));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

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

                try {
                    if (ie.getStack().hasNbt() && ie.getStack().getNbt().containsUuid("hackeritems:owner")) {
                        UUID ownerUuid = ie.getStack().getNbt().getUuid("hackeritems:owner");
                        ServerPlayerEntity owner = server.getPlayerManager().getPlayer(ownerUuid);
                        if (owner != null) ownerAbbr = abbreviate(owner.getName().getString());
                    }
                } catch (Exception ignored) {}

                broadcast(server, Text.of(String.format("%s (%s) liegend bei %s (owner=%s)", type, pos, world.getRegistryKey().getValue().toString(), ownerAbbr)));
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
        return name.substring(0, 1) + name.substring(name.length() - 1);
    }
}
