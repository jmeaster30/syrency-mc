package com.syrency.mc.blocks;

import com.mojang.serialization.MapCodec;
import com.syrency.mc.blockentities.FastHopperBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FastHopperBlock extends BlockWithEntity {
    public static final DirectionProperty FACING;
    public static final BooleanProperty ENABLED;
    private static final VoxelShape TOP_SHAPE;
    private static final VoxelShape MIDDLE_SHAPE;
    private static final VoxelShape OUTSIDE_SHAPE;
    private static final VoxelShape DEFAULT_SHAPE;
    private static final VoxelShape DOWN_SHAPE;
    private static final VoxelShape EAST_SHAPE;
    private static final VoxelShape NORTH_SHAPE;
    private static final VoxelShape SOUTH_SHAPE;
    private static final VoxelShape WEST_SHAPE;
    private static final VoxelShape DOWN_RAY_TRACE_SHAPE;
    private static final VoxelShape EAST_RAY_TRACE_SHAPE;
    private static final VoxelShape NORTH_RAY_TRACE_SHAPE;
    private static final VoxelShape SOUTH_RAY_TRACE_SHAPE;
    private static final VoxelShape WEST_RAY_TRACE_SHAPE;

    static {
        FACING = Properties.HOPPER_FACING;
        ENABLED = Properties.ENABLED;
        TOP_SHAPE = Block.createCuboidShape(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        MIDDLE_SHAPE = Block.createCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
        OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE);
        DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, Hopper.INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);
        DOWN_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
        EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
        NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
        SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
        WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
        DOWN_RAY_TRACE_SHAPE = Hopper.INSIDE_SHAPE;
        EAST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
        NORTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
        SOUTH_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
        WEST_RAY_TRACE_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));
    }

    public FastHopperBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.DOWN).with(ENABLED, true));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING).ordinal()) {
            case 1 -> DOWN_SHAPE;
            case 2 -> NORTH_SHAPE;
            case 3 -> SOUTH_SHAPE;
            case 4 -> WEST_SHAPE;
            case 5 -> EAST_SHAPE;
            default -> DEFAULT_SHAPE;
        };
    }

    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return switch (state.get(FACING).ordinal()) {
            case 1 -> DOWN_RAY_TRACE_SHAPE;
            case 2 -> NORTH_RAY_TRACE_SHAPE;
            case 3 -> SOUTH_RAY_TRACE_SHAPE;
            case 4 -> WEST_RAY_TRACE_SHAPE;
            case 5 -> EAST_RAY_TRACE_SHAPE;
            default -> Hopper.INSIDE_SHAPE;
        };
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide().getOpposite();
        return this.getDefaultState().with(FACING, direction.getAxis() == Axis.Y ? Direction.DOWN : direction).with(ENABLED, true);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FastHopperBlockEntity();
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FastHopperBlockEntity) {
                ((FastHopperBlockEntity) blockEntity).setCustomName(itemStack.getName());
            }
        }
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updateEnabled(world, pos, state);
        }
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
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

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean bl = !world.isReceivingRedstonePower(pos);
        if (bl != state.get(ENABLED)) {
            world.setBlockState(pos, state.with(ENABLED, bl), 4);
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FastHopperBlockEntity) {
                ItemScatterer.spawn(world, pos, (FastHopperBlockEntity) blockEntity);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof FastHopperBlockEntity) {
            ((FastHopperBlockEntity) blockEntity).onEntityCollided(entity);
        }
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
