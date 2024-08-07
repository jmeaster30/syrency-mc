package com.syrency.mc.blocks;

import com.syrency.mc.blockentities.SplitterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SplitterBlock extends BlockWithEntity {
  public static final BooleanProperty ENABLED = BooleanProperty.of("enabled");

  public SplitterBlock(Settings settings) {
     super(settings);
     this.setDefaultState(this.stateManager.getDefaultState().with(ENABLED, true));
  }

  public BlockState getPlacementState(ItemPlacementContext ctx) {
     return this.getDefaultState().with(ENABLED, true);
  }

  public BlockEntity createBlockEntity(BlockView world) {
     return new SplitterBlockEntity();
  }

  public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    if (itemStack.hasCustomName()) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof SplitterBlockEntity) {
        ((SplitterBlockEntity)blockEntity).setCustomName(itemStack.getName());
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
      if (blockEntity instanceof SplitterBlockEntity) {
        player.openHandledScreen((SplitterBlockEntity)blockEntity);
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
    if (bl != (Boolean)state.get(ENABLED)) {
      world.setBlockState(pos, (BlockState)state.with(ENABLED, bl), 4);
    }
  }

  public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
    if (!state.isOf(newState.getBlock())) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof SplitterBlockEntity) {
        ItemScatterer.spawn(world, pos, (SplitterBlockEntity)blockEntity);
        world.updateComparators(pos, this);
      }

      super.onStateReplaced(state, world, pos, newState, moved);
    }
  }

  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  public boolean hasComparatorOutput(BlockState state) {
    return false;
  }

  public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
    return 0;
  }

  protected void appendProperties(Builder<Block, BlockState> builder) {
     builder.add(ENABLED);
  }

  public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
    BlockEntity blockEntity = world.getBlockEntity(pos);
    if (blockEntity instanceof SplitterBlockEntity) {
      ((SplitterBlockEntity)blockEntity).onEntityCollided(entity);
    }
  }

  public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
     return false;
  }
}
