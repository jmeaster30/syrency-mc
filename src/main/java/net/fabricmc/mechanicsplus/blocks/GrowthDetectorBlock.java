package net.fabricmc.mechanicsplus.blocks;

import net.fabricmc.mechanicsplus.blockentities.GrowthDetectorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GrowthDetectorBlock extends FacingBlock implements BlockEntityProvider {
  
  public static final IntProperty POWER = IntProperty.of("power", 0, 16);

  public GrowthDetectorBlock(Settings settings) {
    super(settings);
    setDefaultState(getStateManager().getDefaultState().with(Properties.FACING, Direction.NORTH).with(POWER, 0));
  }

  @Override
  public void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
    stateManager.add(Properties.FACING);
    stateManager.add(POWER);
  }

  @Override
  public BlockEntity createBlockEntity(BlockView blockView) {
    return new GrowthDetectorBlockEntity();
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

  public BlockState getPlacementState(ItemPlacementContext ctxt) {
    return (BlockState) this.getDefaultState().with(FACING, ctxt.getPlayerLookDirection());
  }

  public boolean emitsRedstonePower(BlockState state) {
    return true;
  }
  
  public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
    boolean behind = state.get(Properties.FACING).equals(direction);
    return behind ? state.get(POWER) : 0;
  }
}
