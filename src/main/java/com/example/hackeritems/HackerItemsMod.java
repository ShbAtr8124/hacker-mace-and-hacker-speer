package com.example.hackeritems;

import net.fabricmc.api.ModInitializer;

public class HackerItemsMod implements ModInitializer {
    public static final String MODID = "hackeritems";

    @Override
    public void onInitialize() {
        ModItems.register();
    }
}
