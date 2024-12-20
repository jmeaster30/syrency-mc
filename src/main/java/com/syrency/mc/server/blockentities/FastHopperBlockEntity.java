package com.syrency.mc.server.blockentities;

import com.syrency.mc.server.SyrencyMod;
import com.syrency.mc.server.blocks.FastHopperBlock;
import com.syrency.mc.server.screens.FastHopperScreenHandler;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class FastHopperBlockEntity extends AbstractHopperBlockEntity {

    public FastHopperBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.FAST_HOPPER_ENTITY, blockPos, blockState, FastHopperBlock.FACING, 5, 8, 4);
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FastHopperScreenHandler(syncId, playerInventory, this);
    }
}
