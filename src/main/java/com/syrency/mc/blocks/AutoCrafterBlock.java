package com.syrency.mc.blocks;

import com.syrency.mc.blockentities.AutoCrafterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AutoCrafterBlock extends Block implements BlockEntityProvider{

  public AutoCrafterBlock(Settings settings) {
    super(settings);
    setDefaultState(getStateManager().getDefaultState());
  }
  
  @Override
  public void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    return;
  }
  
  @Override
  public BlockEntity createBlockEntity(BlockView blockView) {
    return new AutoCrafterBlockEntity();
  }

  @Override
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
    super.onSyncedBlockEvent(state, world, pos, type, data);
    BlockEntity blockEntity = world.getBlockEntity(pos);
    return blockEntity == null ? false : blockEntity.onSyncedBlockEvent(type, data);
  }

  public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory) blockEntity : null;
  }

  public BlockState getPlacementState(ItemPlacementContext ctxt) {
    return (BlockState) this.getDefaultState();
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
      BlockHitResult hit) {
    if (!world.isClient) {
      //player.sendMessage(new LiteralText("You used this block!!"), false);

      NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

      if (screenHandlerFactory != null) {
        //player.sendMessage(new LiteralText("Opened screen"), false);
        player.openHandledScreen(screenHandlerFactory);
      }
    }

    return ActionResult.CONSUME;
  }

  @Override
  public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (state.getBlock() != newState.getBlock()) {

      BlockEntity blockEntity = world.getBlockEntity(pos);

      if (blockEntity instanceof AutoCrafterBlockEntity) {
        ItemScatterer.spawn(world, pos, (AutoCrafterBlockEntity) blockEntity);
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
