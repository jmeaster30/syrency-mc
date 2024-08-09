package com.syrency.mc.screens;

import com.syrency.mc.SyrencyMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FastHopperScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public FastHopperScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5)); // TODO have this set in SyrencyMod as a constant value
    }

    public FastHopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(SyrencyMod.FAST_HOPPER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        checkSize(inventory, 5);
        inventory.onOpen(playerInventory.player);

        int m;
        for (m = 0; m < 5; ++m) {
            this.addSlot(new Slot(inventory, m, 44 + m * 18, 20));
        }

        for (m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, m * 18 + 51));
            }
        }

        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 109));
        }
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack())
            return itemStack;

        ItemStack itemStack2 = slot.getStack();
        itemStack = itemStack2.copy();
        if (slotIndex < this.inventory.size()) {
            if (!this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
            return ItemStack.EMPTY;
        }

        if (itemStack2.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return itemStack;
    }
}
