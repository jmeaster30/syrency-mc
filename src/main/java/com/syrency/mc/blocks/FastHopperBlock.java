package com.syrency.mc.blocks;

import com.mojang.serialization.MapCodec;
import com.syrency.mc.SyrencyMod;
import com.syrency.mc.blockentities.AbstractHopperBlockEntity;
import com.syrency.mc.blockentities.FastHopperBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FastHopperBlock extends HopperBlock {
    public static final MapCodec<HopperBlock> CODEC = createCodec(FastHopperBlock::new);

    public FastHopperBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.DOWN).with(ENABLED, true));
    }

    @Override
    public MapCodec<HopperBlock> getCodec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FastHopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, SyrencyMod.FAST_HOPPER_ENTITY, FastHopperBlockEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FastHopperBlockEntity) {
                player.openHandledScreen((FastHopperBlockEntity) blockEntity);
                player.incrementStat(Stats.INSPECT_HOPPER);
            }

            return ActionResult.CONSUME;
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FastHopperBlockEntity fastHopperBlockEntity) {
            AbstractHopperBlockEntity.onEntityCollided(world, pos, state, entity, fastHopperBlockEntity);
        }
    }
}
