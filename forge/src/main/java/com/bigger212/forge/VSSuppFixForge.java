package com.bigger212.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.bigger212.VSSuppFixCommon;

@Mod(VSSuppFixCommon.MOD_ID)
public final class VSSuppFixForge {
    public VSSuppFixForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VSSuppFixCommon.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        VSSuppFixCommon.init();
    }
}
