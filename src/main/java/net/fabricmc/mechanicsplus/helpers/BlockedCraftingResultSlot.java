package net.fabricmc.mechanicsplus.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.CraftingResultSlot;

public class BlockedCraftingResultSlot extends CraftingResultSlot {

  public BlockedCraftingResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x,
      int y) {
    super(player, input, inventory, index, x, y);
  }

  public boolean canTakeItems(PlayerEntity pEntity) {
    return false;
  }
  
}
