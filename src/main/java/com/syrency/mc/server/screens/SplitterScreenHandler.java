package com.syrency.mc.server.screens;

import com.syrency.mc.server.SyrencyMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SplitterScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public SplitterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public SplitterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(SyrencyMod.SPLITTER_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        checkSize(inventory, 9);
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 5, 80, 17)); //north
        this.addSlot(new Slot(inventory, 6, 98, 35)); //east
        this.addSlot(new Slot(inventory, 7, 80, 53)); //south
        this.addSlot(new Slot(inventory, 8, 62, 35)); //west

        int m;
        for (m = 0; m < 5; ++m) {
            this.addSlot(new Slot(inventory, m, 44 + m * 18, 77));
        }

        for (m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, m * 18 + 101));
            }
        }

        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 159));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < this.inventory.size()) {
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
        }

        return itemStack;
    }
}
