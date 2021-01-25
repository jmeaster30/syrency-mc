package net.fabricmc.mechanicsplus.blockentities;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.fabricmc.mechanicsplus.helpers.ImplementedInventory;
import net.fabricmc.mechanicsplus.screens.BreakerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BreakerBlockEntity extends BlockEntity
    implements ImplementedInventory, NamedScreenHandlerFactory, Tickable {

  // call "markDirty()" when changes are made to this block so the world will
  // properly save the changes

  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);

  private int actionDelay = 4;
  private boolean activated = false;

  public BreakerBlockEntity() {
    super(MechanicsPlusMod.BREAKER_BLOCK_ENTITY);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    super.toTag(tag);

    Inventories.toTag(tag, items);

    tag.putInt("actionDelay", actionDelay);
    tag.putBoolean("activated", activated);

    return tag;
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);

    Inventories.fromTag(tag, items);

    actionDelay = tag.getInt("actionDelay");
    activated = tag.getBoolean("activated");
  }

  @Override
  public DefaultedList<ItemStack> getItems() {
    return items;
  }

  // returns the left over items that wer not added
  public List<ItemStack> addToInventory(List<ItemStack> toAdd) {
    List<ItemStack> notAdded = new ArrayList<ItemStack>();

    for (ItemStack itemStack : toAdd) {
      ItemStack leftovers = itemStack;
      for (int i = 0; i < items.size(); i++) {
        leftovers = addStack(i, leftovers);
        if (leftovers == null)
          break;
      }

      if (leftovers != null) {
        notAdded.add(leftovers);
      }
    }

    return notAdded;
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
    return new BreakerScreenHandler(syncId, playerInventory, this);
  }

  @Override
  public Text getDisplayName() {
    return new TranslatableText(getCachedState().getBlock().getTranslationKey());
  }

  @Override
  public void tick() {
    if (actionDelay == 0) {
      Direction facing = world.getBlockState(pos).get(Properties.FACING);
      BlockPos inFront = pos.add(facing.getVector());

      BlockState breakingState = world.getBlockState(inFront);
      BlockEntity breakingEntity = breakingState.getBlock().hasBlockEntity() ? world.getBlockEntity(inFront) : null;

      List<ItemStack> droppedStacks = new ArrayList<ItemStack>();
      if (world instanceof ServerWorld) {
        droppedStacks = Block.getDroppedStacks(breakingState, (ServerWorld) world, inFront, breakingEntity);
      }

      List<ItemStack> leftovers = addToInventory(droppedStacks);
      leftovers.forEach((itemStack) -> {
        Block.dropStack(world, pos, itemStack);
      });

      int maxUpdateDepth = 64;

      world.breakBlock(inFront, false, (Entity) null, maxUpdateDepth);
      actionDelay = 4;
    }

    if (actionDelay < 4) {
      actionDelay -= 1;
    }

    if (world.isReceivingRedstonePower(pos) && actionDelay == 4 && !activated) {
      actionDelay -= 1;
      activated = true;
    }

    if (!world.isReceivingRedstonePower(pos)) {
      activated = false;
    }
  }
}