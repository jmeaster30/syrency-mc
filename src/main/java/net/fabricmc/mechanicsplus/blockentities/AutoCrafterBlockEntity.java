package net.fabricmc.mechanicsplus.blockentities;

import java.util.Optional;

import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.fabricmc.mechanicsplus.helpers.EntityCraftingInventory;
import net.fabricmc.mechanicsplus.screens.AutoCrafterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class AutoCrafterBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory, Tickable {

  private static final int[] BOTTOM_SLOTS = new int[] { 0 };
  private static final int[] OTHER_SLOTS;

  static {
    OTHER_SLOTS = new int[27];
    for (int i = 0; i < 27; i++) {
      OTHER_SLOTS[i] = i + 10;
    }
  }

  private int delay = 4;
  private boolean activated = false;
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

    tag.putInt("delay", delay);
    tag.putBoolean("activated", activated);

    return tag;
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);

    Inventories.fromTag(tag, items);

    delay = tag.getInt("delay");
    activated = tag.getBoolean("activated");
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
    return new AutoCrafterScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(world, pos));
  }

  @Override
  public Text getDisplayName() {
    return new TranslatableText(getCachedState().getBlock().getTranslationKey());
  }

  private DefaultedList<ItemStack> combineItemStacks(DefaultedList<ItemStack> stacks) {
    DefaultedList<ItemStack> combined = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);

    for (int i = 0; i < stacks.size(); i++) {
      ItemStack s = stacks.get(i).copy();

      for (int j = 0; j < combined.size(); j++) {
        ItemStack c = combined.get(j);

        if (c.equals(ItemStack.EMPTY)) {
          combined.set(j, s);
          break;
        } else if (c.getItem().equals(s.getItem())) {
          if (c.getCount() + s.getCount() <= c.getMaxCount()) {
            c.setCount(c.getCount() + s.getCount());
            break;
          } else {
            c.setCount(c.getMaxCount());
            s.setCount(c.getCount() + s.getCount() - c.getMaxCount());
          }
        }
      }
    }

    return combined;
  }

  private boolean contains(DefaultedList<ItemStack> list, DefaultedList<ItemStack> toFind) {
    for (ItemStack find : toFind) {
      if(find.getItem().equals(ItemStack.EMPTY.getItem()))
        continue;

      int idx = -1;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i).getItem().equals(find.getItem())) {
          idx = i;
          break;
        }
      }

      if (idx == -1) {
        return false;
      }

      ItemStack found = list.get(idx);
      if (found.getCount() < find.getCount()){
        return false;
      }
    }

    return true;
  }

  private DefaultedList<ItemStack> getNeeded(DefaultedList<ItemStack> stacks) {
    DefaultedList<ItemStack> remaining = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);

    for (int i = 0; i < remaining.size(); i++) {
      ItemStack r = stacks.get(i).copy();
      if(r.getCount() > 0)
        r.setCount(1);
      
      remaining.set(i, r);
    }

    return remaining;
  }

  @Override
  public void tick() {

    if (delay == 0) {
      if (!world.isClient) {
        EntityCraftingInventory craftingInventory = new EntityCraftingInventory(3, 3);
        //load up crafting inventory
        for(int i = 1; i < 10; i++)
          craftingInventory.setStack(i - 1, items.get(i));

        Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING,
            craftingInventory, world);
        if (optional.isPresent()) {
          CraftingRecipe recipe = optional.get();
          ItemStack crafted = recipe.craft(craftingInventory);

          if ((crafted.getItem().equals(items.get(0).getItem())
              && crafted.getCount() + items.get(0).getCount() <= items.get(0).getMaxCount())
              || items.get(0).getItem().equals(ItemStack.EMPTY.getItem())) {
      
            DefaultedList<ItemStack> neededRecipeStacks = combineItemStacks(getNeeded(craftingInventory.getStacks()));

            DefaultedList<ItemStack> baseInventory = DefaultedList.ofSize(items.size() - 10, ItemStack.EMPTY);
            for (int i = 0; i < baseInventory.size(); i++) {
              baseInventory.set(i, items.get(i + 10));
            }

            if (!contains(combineItemStacks(baseInventory), neededRecipeStacks)) {
              delay = 4;
              return;
            }

            for (ItemStack needed : neededRecipeStacks) {
              for(int j = 10; j < items.size(); j++) {
                ItemStack stack = items.get(j);

                if (stack.getItem().equals(needed.getItem())) {
                  if (needed.getCount() <= stack.getCount()) {
                    stack.setCount(stack.getCount() - needed.getCount());
                    needed.setCount(0);
                    break;
                  } else {
                    needed.setCount(needed.getCount() - stack.getCount());
                    stack.setCount(0);
                  }
                }
              }
            }

            if (items.get(0).equals(ItemStack.EMPTY)) {
              items.set(0, crafted);
            } else {
              items.get(0).setCount(crafted.getCount() + items.get(0).getCount());
            }
          }
        }
      }

      delay = 4;
    }

    if (delay < 4) {
      delay -= 1;
    }

    if (world.isReceivingRedstonePower(pos) && delay == 4 && !activated) {
      delay -= 1;
      activated = true;
    }

    if (!world.isReceivingRedstonePower(pos) && delay == 4) {
      activated = false;
    }
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
    return Inventories.removeStack(items, slot);
  }

  @Override
  public ItemStack removeStack(int slot, int count) {
    ItemStack result = Inventories.splitStack(items, slot, count);
    if (!result.isEmpty()) {
      markDirty();
    }
    return result;
  }

  @Override
  public void setStack(int slot, ItemStack stack) {
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
    if (dir == Direction.DOWN && slot == 0) {
      return true;
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
