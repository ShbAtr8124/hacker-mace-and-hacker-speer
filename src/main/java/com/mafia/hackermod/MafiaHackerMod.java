package com.mafia.hackermod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MafiaHackerMod implements ModInitializer {
    public static final String MOD_ID = "mafia_hacker_mod";

    public static final Item HACKER_MACE = registerItem("hacker_mace");
    public static final Item HACKER_SPEAR = registerItem("hacker_spear");

    private static Item registerItem(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Item item = new Item(new Item.Properties()
                .setId(itemKey)
                .stacksTo(1)
                .rarity(Rarity.EPIC));

        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    @Override
    public void onInitialize() {
        System.out.println("[Mafia Hacker Mod] Hacker Mace and Hacker Spear registered.");
    }
}
