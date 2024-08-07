package com.syrency.mc;

import com.syrency.mc.screens.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class SyrencyClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(SyrencyMod.BREAKER_SCREEN_HANDLER, BreakerScreen::new);
        ScreenRegistry.register(SyrencyMod.AUTOCRAFTER_SCREEN_HANDLER, AutoCrafterScreen::new);
        ScreenRegistry.register(SyrencyMod.PLACER_SCREEN_HANDLER, PlacerScreen::new);
        ScreenRegistry.register(SyrencyMod.FAST_HOPPER_SCREEN_HANDLER, FastHopperScreen::new);
        ScreenRegistry.register(SyrencyMod.HUPPER_SCREEN_HANDLER, HupperScreen::new);
        ScreenRegistry.register(SyrencyMod.SPLITTER_SCREEN_HANDLER, SplitterScreen::new);
    }
}
