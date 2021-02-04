package net.fabricmc.mechanicsplus.screens;

import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.text.Text;

public class FastHopperScreen extends HopperScreen {

  public FastHopperScreen(HopperScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
  }
  
}
