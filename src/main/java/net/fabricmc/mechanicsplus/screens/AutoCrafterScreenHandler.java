package net.fabricmc.mechanicsplus.screens;

import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AutoCrafterScreenHandler extends ScreenHandler {
  
  private final Inventory inventory;

  public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
    this(syncId, playerInventory, new SimpleInventory(37));
  }

  public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
    super(MechanicsPlusMod.AUTOCRAFTER_SCREEN_HANDLER, syncId);
    checkSize(inventory, 37);
    this.inventory = inventory;
    inventory.onOpen(playerInventory.player);
    
    //output slot
    this.addSlot(new Slot(inventory, 0, 120, 32));

    int m;
    int l;
    
    //craft table
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 3; ++l) {
        this.addSlot(new Slot(inventory, 1 + l + m * 3, 30 + l * 18, 18 + m * 18));
      }
    }

    //block inentory
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 9; ++l) {
        //inventory, index, x, y
        this.addSlot(new Slot(inventory, 10 + l + m * 9, 8 + l * 18, 72 + m * 18));
      }
    }

    // The player inventory
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 9; ++l) {
        this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 140 + m * 18));
      }
    }
    // The player Hotbar
    for (m = 0; m < 9; ++m) {
      this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 198));
    }
  }

  @Override
  public boolean canUse(PlayerEntity player) {
    return this.inventory.canPlayerUse(player);
  }

  // Shift + Player Inv Slot
  @Override
  public ItemStack transferSlot(PlayerEntity player, int invSlot) {
    ItemStack newStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(invSlot);
    if (slot != null && slot.hasStack()) {
      ItemStack originalStack = slot.getStack();
      newStack = originalStack.copy();
      if (invSlot < this.inventory.size()) {
        if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
        return ItemStack.EMPTY;
      }

      if (originalStack.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }
    }

    return newStack;
  }
}
