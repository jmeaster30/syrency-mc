package com.syrency.mc.server.blockentities;

import com.syrency.mc.server.SyrencyMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;


// TODO is this necessary??
public class GrowthDetectorBlockEntity extends BlockEntity {
    public GrowthDetectorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.GROWTH_DETECTOR_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        super.readNbt(nbt, registry);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        super.writeNbt(nbt, registry);
    }
}
