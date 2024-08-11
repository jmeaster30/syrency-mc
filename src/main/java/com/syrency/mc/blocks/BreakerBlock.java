package com.syrency.mc.blocks;

import com.mojang.serialization.MapCodec;
import com.syrency.mc.SyrencyMod;
import com.syrency.mc.Utils;
import com.syrency.mc.blockentities.BreakerBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BreakerBlock extends FacingBlock implements BlockEntityProvider {

    public static final MapCodec<BreakerBlock> CODEC = createCodec(BreakerBlock::new);
    // TODO actually change this properly and make it change to the active form of the breaker block
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public BreakerBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.FACING, Direction.NORTH).with(ACTIVE, false));
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(ACTIVE);
        stateManager.add(Properties.FACING);
    }

    @Override
    public MapCodec<BreakerBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : Utils.validateTicker(type, SyrencyMod.BREAKER_BLOCK_ENTITY, BreakerBlockEntity::serverTick);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BreakerBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) blockEntity : null;
    }

    public BlockState getPlacementState(ItemPlacementContext ctxt) {
        return this.getDefaultState().with(FACING, ctxt.getPlayerLookDirection());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient)
            return ActionResult.SUCCESS;

        NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

        if (screenHandlerFactory != null) {
            player.openHandledScreen(screenHandlerFactory);
        }

        return ActionResult.CONSUME;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {

            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof BreakerBlockEntity) {
                ItemScatterer.spawn(world, pos, (BreakerBlockEntity) blockEntity);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
}
