package com.syrency.mc.helpers;

import com.syrency.mc.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;
import java.util.List;

public class EntityCraftingInventory implements RecipeInputInventory {

    private final DefaultedList<ItemStack> stacks;
    private final int width;
    private final int height;

    public EntityCraftingInventory(int width, int height) {
        this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
        this.width = width;
        this.height = height;
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        Utils.readInventoryNbt(nbt, "inventory", this.stacks, registry);
    }

    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        Utils.writeInventoryNbt(nbt, "inventory", this.stacks, registry);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.stacks, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack original = this.stacks.get(slot);
        this.stacks.set(slot, ItemStack.EMPTY);
        return original;
    }

    public ItemStack splitStack(int slot, int amount) {
        return Inventories.splitStack(this.stacks, slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return this.stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot >= this.size() ? ItemStack.EMPTY : this.stacks.get(slot);
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public List<ItemStack> getHeldStacks() {
        DefaultedList<ItemStack> copyOfList = DefaultedList.ofSize(this.size());
        copyOfList.addAll(this.stacks);
        return copyOfList;
    }

    public void setHeldStacks(DefaultedList<ItemStack> inventory)
    {
        this.stacks.clear();
        this.stacks.addAll(inventory);
    }

    @Override
    public CraftingRecipeInput createRecipeInput() {
        return RecipeInputInventory.super.createRecipeInput();
    }

    @Override
    public CraftingRecipeInput.Positioned createPositionedRecipeInput() {
        return RecipeInputInventory.super.createPositionedRecipeInput();
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack itemStack : this.stacks) {
            finder.addUnenchantedInput(itemStack);
        }
    }
}
