package net.fabricmc.mechanicsplus.blockentities;

import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.fabricmc.mechanicsplus.screens.AutoCrafterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.block.CraftingTableBlock;

public class AutoCrafterBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory, Tickable {

  private static final int[] BOTTOM_SLOTS = new int[] { 0 };
  private static final int[] OTHER_SLOTS;

  static {
    OTHER_SLOTS = new int[27];
    for (int i = 0; i < 27; i++) {
      OTHER_SLOTS[i] = i + 10;
    }
  }

  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(37, ItemStack.EMPTY);
  // 0 : output
  // 1-9 : crafting table
  // 10-36 : block inventory

  public AutoCrafterBlockEntity() {
    super(MechanicsPlusMod.AUTOCRAFTER_BLOCK_ENTITY);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    super.toTag(tag);

    Inventories.toTag(tag, items);

    return tag;
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);

    Inventories.fromTag(tag, items);
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
    return new AutoCrafterScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(world, pos));
  }

  @Override
  public Text getDisplayName() {
    return new TranslatableText(getCachedState().getBlock().getTranslationKey());
  }

  @Override
  public void tick() {

  }

  @Override
  public void clear() {
    items.clear();
  }

  @Override
  public boolean canPlayerUse(PlayerEntity player) {
    return true;
  }

  @Override
  public ItemStack getStack(int slot) {
    // System.out.println("GET STACK :) " + slot);
    return items.get(slot);
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < size(); i++) {
      ItemStack stack = getStack(i);
      if (!stack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack removeStack(int slot) {
    // System.out.println("REMOVE STACK :) " + slot);
    return Inventories.removeStack(items, slot);
  }

  @Override
  public ItemStack removeStack(int slot, int count) {
    // System.out.println("REMOVE STACK :) " + slot + " : " + count);
    ItemStack result = Inventories.splitStack(items, slot, count);
    if (!result.isEmpty()) {
      markDirty();
    }
    return result;
  }

  @Override
  public void setStack(int slot, ItemStack stack) {
    System.out.println("SET STACK :O " + slot);
    items.set(slot, stack);
    if (stack.getCount() > getMaxCountPerStack()) {
      stack.setCount(getMaxCountPerStack());
    }
  }

  @Override
  public int size() {
    return items.size();
  }

  @Override
  public boolean canExtract(int slot, ItemStack stack, Direction dir) {
    //only extract from the output if the item is greater than one
    if (dir == Direction.DOWN && slot == 0) {
      int outputSize = stack.getCount();
      return outputSize > 1;
    }
    return false;
  }

  @Override
  public boolean canInsert(int slot, ItemStack stack, Direction dir) {
    if(slot < 10)
      return false;
    return true;
  }

  @Override
  public int[] getAvailableSlots(Direction side) {
    if (side == Direction.DOWN) {
      return BOTTOM_SLOTS;
    } else {
      return OTHER_SLOTS;
    }
  }
}
