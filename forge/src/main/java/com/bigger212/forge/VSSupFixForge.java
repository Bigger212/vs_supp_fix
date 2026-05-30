package com.bigger212.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.bigger212.VSSupFixCommon;

@Mod(VSSupFixCommon.MOD_ID)
public final class VSSupFixForge {
    public VSSupFixForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(VSSupFixCommon.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        VSSupFixCommon.init();
    }
}
