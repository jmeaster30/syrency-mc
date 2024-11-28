package com.syrency.mc.server.blocks;

import com.mojang.serialization.MapCodec;
import com.syrency.mc.server.blockentities.GrowthDetectorBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class GrowthDetectorBlock extends FacingBlock implements BlockEntityProvider {
    public static final MapCodec<GrowthDetectorBlock> CODEC = createCodec(GrowthDetectorBlock::new);
    public static final IntProperty POWER = IntProperty.of("power", 0, 9);

    public GrowthDetectorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.FACING, Direction.NORTH).with(POWER, 0));
    }

    @Override
    public MapCodec<GrowthDetectorBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.FACING);
        stateManager.add(POWER);
    }

    @Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(FACING) == direction) {
			this.scheduleTick(world, pos);
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

    private void scheduleTick(WorldAccess world, BlockPos blockPos)
    {
        if (!world.isClient() && !world.getBlockTickScheduler().isQueued(blockPos, this)) {
            world.scheduleBlockTick(blockPos, this, 2);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState thisState = world.getBlockState(pos);
        Direction facing = thisState.get(Properties.FACING);
        BlockPos inFront = pos.add(facing.getVector());

        BlockState lookingState = world.getBlockState(inFront);

        Integer originalPower = state.get(POWER);
        Integer plantAge = getBlockStateAge(lookingState);

        if (plantAge == null && originalPower == 0)
            return;

        if (plantAge == null)
            plantAge = -1;

        int newPower = plantAge + 1;
        if (originalPower == newPower)
            return;

        world.setBlockState(pos, thisState.with(GrowthDetectorBlock.POWER, newPower));
        this.updateNeighbors(world, pos, state);
    }

    private Integer getBlockStateAge(BlockState bs) {
        //age 1 bamboo
        //age 25 kelp / vines
        //age 15 sugarcane
        // ^^ the ages for these did not give anything useful

        if (bs.contains(Properties.AGE_2)) {
            return bs.get(Properties.AGE_2);
        } else if (bs.contains(Properties.AGE_3)) {
            return bs.get(Properties.AGE_3);
        } else if (bs.contains(Properties.AGE_5)) {
            return bs.get(Properties.AGE_5);
        } else if (bs.contains(Properties.AGE_7)) {
            return bs.get(Properties.AGE_7);
        } else if (bs.contains(Properties.STAGE)) {
            return bs.get(Properties.STAGE);
        } else {
            return null;
        }
    }

    protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctxt) {
        return this.getDefaultState().with(FACING, ctxt.getPlayerLookDirection());
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakRedstonePower(state, world, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        boolean behind = state.get(Properties.FACING).equals(direction);
        return behind ? state.getOrEmpty(POWER).orElse(0) : 0;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return emitsRedstonePower(state);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return this.getWeakRedstonePower(state, world, pos, state.get(Properties.FACING));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GrowthDetectorBlockEntity(pos, state);
    }
}
