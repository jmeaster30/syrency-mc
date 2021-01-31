package net.fabricmc.mechanicsplus.helpers;

import java.util.Iterator;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.collection.DefaultedList;

public class EntityCraftingInventory extends CraftingInventory {

  private final DefaultedList<ItemStack> stacks;
  private final int width;
  private final int height;
  
  public EntityCraftingInventory(int width, int height) {
    super(null, width, height);
    this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
    this.width = width;
    this.height = height;
  }

  public DefaultedList<ItemStack> getStacks() {
    return stacks;
  }

  public ItemStack removeStack(int slot, int amount) {
    return Inventories.splitStack(this.stacks, slot, amount);
  }

  public void setStack(int slot, ItemStack stack) {
    this.stacks.set(slot, stack);
  }
  
  public int size() {
    return this.stacks.size();
  }

  public boolean isEmpty() {
    Iterator<ItemStack> var1 = this.stacks.iterator();

    ItemStack itemStack;
    do {
      if (!var1.hasNext()) {
        return true;
      }

      itemStack = (ItemStack)var1.next();
    } while(itemStack.isEmpty());

    return false;
  }

  public ItemStack getStack(int slot) {
    return slot >= this.size() ? ItemStack.EMPTY : (ItemStack) this.stacks.get(slot);
  }

  public void markDirty() {
  }

  public boolean canPlayerUse(PlayerEntity player) {
    return true;
  }

  public void clear() {
    this.stacks.clear();
  }

  public int getHeight() {
    return this.height;
  }

  public int getWidth() {
    return this.width;
  }

  public void provideRecipeInputs(RecipeFinder finder) {
    Iterator<ItemStack> var2 = this.stacks.iterator();

    while (var2.hasNext()) {
      ItemStack itemStack = (ItemStack) var2.next();
      finder.addNormalItem(itemStack);
    }
  }
}
