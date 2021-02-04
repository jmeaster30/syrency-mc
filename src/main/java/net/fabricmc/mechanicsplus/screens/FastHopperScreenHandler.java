package net.fabricmc.mechanicsplus.screens;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HopperScreenHandler;

public class FastHopperScreenHandler extends HopperScreenHandler {

  public FastHopperScreenHandler(int syncId, PlayerInventory playerInventory) {
    super(syncId, playerInventory);
  }

  public FastHopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
    super(syncId, playerInventory, inventory);
  }
}
