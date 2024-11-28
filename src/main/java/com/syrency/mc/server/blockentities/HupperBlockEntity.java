package com.syrency.mc.server.blockentities;

import com.syrency.mc.server.SyrencyMod;
import com.syrency.mc.server.blocks.HupperBlock;
import com.syrency.mc.server.screens.HupperScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class HupperBlockEntity extends AbstractHopperBlockEntity {

    public HupperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.HUPPER_ENTITY, blockPos, blockState, HupperBlock.FACING, 5, 1, 8);
        this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
        this.transferCooldown = -1;
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new HupperScreenHandler(syncId, playerInventory, this);
    }
}
