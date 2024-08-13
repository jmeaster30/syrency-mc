package com.syrency.mc.server.blocks;

import com.mojang.serialization.MapCodec;
import com.syrency.mc.server.SyrencyMod;
import com.syrency.mc.server.blockentities.AbstractHopperBlockEntity;
import com.syrency.mc.server.blockentities.HupperBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HupperBlock extends HopperBlock {
    public static final MapCodec<HopperBlock> CODEC = createCodec(FastHopperBlock::new);

    public static final DirectionProperty FACING;
    public static final BooleanProperty ENABLED;
    private static final VoxelShape TOP_SHAPE;
    private static final VoxelShape MIDDLE_SHAPE;
    private static final VoxelShape OUTSIDE_SHAPE;
    private static final VoxelShape INSIDE_SHAPE;
    private static final VoxelShape DEFAULT_SHAPE;
    private static final VoxelShape UP_SHAPE;
    private static final VoxelShape EAST_SHAPE;
    private static final VoxelShape NORTH_SHAPE;
    private static final VoxelShape SOUTH_SHAPE;
    private static final VoxelShape WEST_SHAPE;
    private static final VoxelShape UP_RAY_TRACE_SHAPE;
    private static final VoxelShape EAST_RAY_TRACE_SHAPE;
    private static final VoxelShape NORTH_RAY_TRACE_SHAPE;
    private static final VoxelShape SOUTH_RAY_TRACE_SHAPE;
    private static final VoxelShape WEST_RAY_TRACE_SHAPE;

    static {
        FACING = Properties.HOPPER_FACING;
        ENABLED = Properties.ENABLED;
        TOP_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
        MIDDLE_SHAPE = Block.createCuboidShape(4.0D, 6.0D, 4.0D, 12.0D, 12.0D, 12.0D);
        OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE);
        INSIDE_SHAPE = createCuboidShape(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
        DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);
        UP_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 12.0D, 6.0D, 10.0D, 16.0D, 10.0D));
        EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0D, 8.0D, 6.0D, 16.0D, 12.0D, 10.0D));
        NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 0.0D, 10.0D, 12.0D, 4.0D));
        SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 12.0D, 10.0D, 12.0D, 16.0D));
        WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0D, 8.0D, 6.0D, 4.0D, 12.0D, 10.0D));
        UP_RAY_TRACE_SHAPE = UP_SHAPE;
        EAST_RAY_TRACE_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0D, 8.0D, 6.0D, 16.0D, 12.0D, 10.0D));
        NORTH_RAY_TRACE_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 0.0D, 10.0D, 12.0D, 4.0D));
        SOUTH_RAY_TRACE_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 12.0D, 10.0D, 12.0D, 16.0D));
        WEST_RAY_TRACE_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0D, 8.0D, 6.0D, 4.0D, 12.0D, 10.0D));
    }

    public HupperBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.DOWN).with(ENABLED, true));
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case UP -> UP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> DEFAULT_SHAPE;
        };
    }

    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return switch (state.get(FACING)) {
            case DOWN -> UP_RAY_TRACE_SHAPE;
            case NORTH -> NORTH_RAY_TRACE_SHAPE;
            case SOUTH -> SOUTH_RAY_TRACE_SHAPE;
            case WEST -> WEST_RAY_TRACE_SHAPE;
            case EAST -> EAST_RAY_TRACE_SHAPE;
            default -> INSIDE_SHAPE;
        };
    }

    @Override
    public MapCodec<HopperBlock> getCodec() { return CODEC; }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HupperBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, SyrencyMod.HUPPER_ENTITY, HupperBlockEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof HupperBlockEntity hpe) {
            player.openHandledScreen(hpe);
            player.incrementStat(Stats.INSPECT_HOPPER);
        }

        return ActionResult.CONSUME;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof HupperBlockEntity hupperBlockEntity) {
            AbstractHopperBlockEntity.onEntityCollided(world, pos, state, entity, hupperBlockEntity);
        }
    }
}
