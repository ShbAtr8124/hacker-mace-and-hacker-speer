package com.mafia.hackermod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MafiaHackerMod implements ModInitializer {
    public static final String MOD_ID = "mafia_hacker_mod";

    // Starter items: visuals/models and special abilities are added in later steps.
    public static final Item HACKER_MACE = registerItem(
            "hacker_mace",
            new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    public static final Item HACKER_SPEAR = registerItem(
            "hacker_spear",
            new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name),
                item
        );
    }

    @Override
    public void onInitialize() {
        System.out.println("[Mafia Hacker Mod] Loaded starter items.");
        // TODO: Add recipe registration, ability logic, and /opitems tracking.
    }
}
