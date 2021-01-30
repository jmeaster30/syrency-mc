package net.fabricmc.mechanicsplus.helpers;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class NoInputSlot extends Slot {

  public NoInputSlot(Inventory inventory, int index, int x, int y) {
    super(inventory, index, x, y);
  }

  public boolean canInsert(ItemStack itemStack) {
    return false;
  }
  
}
