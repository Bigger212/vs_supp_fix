package com.bigger212.fabric;

import net.fabricmc.api.ModInitializer;

import com.bigger212.VSSuppFixCommon;

public final class VSSuppFixFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        VSSuppFixCommon.init();
    }
}
