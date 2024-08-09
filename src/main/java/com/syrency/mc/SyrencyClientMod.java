package com.syrency.mc;

import com.syrency.mc.screens.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class SyrencyClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(SyrencyMod.BREAKER_SCREEN_HANDLER, BreakerScreen::new);
        HandledScreens.register(SyrencyMod.AUTOCRAFTER_SCREEN_HANDLER, AutoCrafterScreen::new);
        HandledScreens.register(SyrencyMod.PLACER_SCREEN_HANDLER, PlacerScreen::new);
        HandledScreens.register(SyrencyMod.FAST_HOPPER_SCREEN_HANDLER, FastHopperScreen::new);
        HandledScreens.register(SyrencyMod.HUPPER_SCREEN_HANDLER, HupperScreen::new);
        HandledScreens.register(SyrencyMod.SPLITTER_SCREEN_HANDLER, SplitterScreen::new);
    }
}
