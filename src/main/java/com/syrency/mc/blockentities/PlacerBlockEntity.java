package com.syrency.mc.blockentities;

import java.util.ArrayList;
import java.util.List;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.blocks.PlacerBlock;
import com.syrency.mc.helpers.ImplementedInventory;
import com.syrency.mc.screens.PlacerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlacerBlockEntity extends BlockEntity
    implements ImplementedInventory, NamedScreenHandlerFactory, Tickable {
 
  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);

  private int actionDelay = 4;
  private boolean activated = false;
    
  public PlacerBlockEntity() {
    super(SyrencyMod.PLACER_BLOCK_ENTITY);
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
    return new PlacerScreenHandler(syncId, playerInventory, this);
  }

  @Override
  public Text getDisplayName() {
    return new TranslatableText(getCachedState().getBlock().getTranslationKey());
  }

  @Override
  public void tick() {
    if (actionDelay == 0) {
      BlockState thisState = world.getBlockState(pos);
      Direction facing = thisState.get(Properties.FACING);
      BlockPos inFront = pos.add(facing.getVector());

      //if there is a block in front of the placer do nothing
      BlockState frontState = world.getBlockState(inFront);
      if (!frontState.isAir()) {
        actionDelay = 4;
        world.setBlockState(pos, thisState.with(PlacerBlock.ACTIVE, false));
        return;
      }

      int idx = -1;
      for (int i = 0; i < items.size(); i++) {
        if (!items.get(i).getItem().equals(ItemStack.EMPTY.getItem())) {
          idx = i;
          break;
        }
      }

      //if the placer is empty do nothing
      if(idx == -1) {
        actionDelay = 4;
        world.setBlockState(pos, thisState.with(PlacerBlock.ACTIVE, false));
        return;
      }

      Item itemToPlace = items.get(idx).getItem();

      Block newBlock = Block.getBlockFromItem(itemToPlace);
      if (newBlock.is(Blocks.AIR)) {
        ItemStack toDrop = new ItemStack(itemToPlace, 1);
        Block.dropStack(world, inFront, toDrop);
      } else {
        BlockState newBlockState = newBlock.getDefaultState();
        Block.replace(frontState, newBlockState, world, inFront, -33); // the -33 makes the block show up
      }

      items.get(idx).setCount(items.get(idx).getCount() - 1);
      if (items.get(idx).getCount() == 0) {
        items.set(idx, ItemStack.EMPTY);
      }

      actionDelay = 4;
      world.setBlockState(pos, thisState.with(PlacerBlock.ACTIVE, false));
    }

    if (actionDelay < 4) {
      actionDelay -= 1;
    }

    if (world.isReceivingRedstonePower(pos) && actionDelay == 4 && !activated) {
      actionDelay -= 1;
      activated = true;
      BlockState thisState = world.getBlockState(pos);
      world.setBlockState(pos, thisState.with(PlacerBlock.ACTIVE, true));
    }

    if (!world.isReceivingRedstonePower(pos) && actionDelay == 4) {
      activated = false;
    }
  }
}
