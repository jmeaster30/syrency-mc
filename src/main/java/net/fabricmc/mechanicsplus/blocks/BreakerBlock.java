package net.fabricmc.mechanicsplus.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BreakerBlock extends FacingBlock {

  // warning we are not changing this at all and we didn't set up the different
  // variants for the model
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

  public BlockState getPlacementState(ItemPlacementContext ctxt) {
    return (BlockState) this.getDefaultState().with(FACING, ctxt.getPlayerLookDirection());
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
      BlockHitResult hit) {
    if (!world.isClient) {
      player.sendMessage(new LiteralText("Hello, world!"), false);
    }

    return ActionResult.SUCCESS;
  }

}
