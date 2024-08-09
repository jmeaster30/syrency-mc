package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.Utils;
import com.syrency.mc.blocks.AutoCrafterBlock;
import com.syrency.mc.helpers.EntityCraftingInventory;
import com.syrency.mc.screens.AutoCrafterScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;

import static com.syrency.mc.blocks.AutoCrafterBlock.CRAFTING;

public class AutoCrafterBlockEntity extends LootableContainerBlockEntity implements RecipeInputInventory, SidedInventory, NamedScreenHandlerFactory {

    private static final int CRAFTING_COOLDOWN = 4;
    private static final int INVENTORY_SIZE = 27;
    private static final int CRAFTING_TABLE_SIZE = 9;

    private static final int[] BOTTOM_SLOTS = new int[]{0};
    private static final int[] OTHER_SLOTS;
    private static final RecipeCache recipeCache = new RecipeCache(10);

    static {
        OTHER_SLOTS = IntStream.range(0, INVENTORY_SIZE).map(x -> x + CRAFTING_TABLE_SIZE + 1).toArray();
    }

    private final DefaultedList<ItemStack> mainInventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final EntityCraftingInventory craftingTable = new EntityCraftingInventory(3, 3);
    private ItemStack outputStack = ItemStack.EMPTY;

    private int craftCooldown = -1;
    private long lastTickTime;
    private boolean activated = false;

    public AutoCrafterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.AUTOCRAFTER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public Text getDisplayName() {
        return getContainerName();
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        Utils.readInventoryNbt(nbt, "inventory", this.mainInventory, registryLookup);

        this.craftingTable.readNbt(nbt.getCompound("craftingTable"), registryLookup);

        this.outputStack = Utils.readItemStackNbt(nbt, "outputStack", registryLookup);
        this.craftCooldown = nbt.getInt("craftCooldown");
        this.lastTickTime = nbt.getLong("lastTickTime");
        this.activated = nbt.getBoolean("activated");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        Utils.writeInventoryNbt(nbt, "inventory", this.mainInventory, registryLookup);
        Utils.writeItemStackNbt(nbt, "outputStack", this.outputStack, registryLookup);

        NbtCompound craftingTableNbt = new NbtCompound();
        this.craftingTable.writeNbt(craftingTableNbt, registryLookup);
        nbt.put("craftingTable", craftingTableNbt);

        nbt.putInt("craftCooldown", this.craftCooldown);
        nbt.putLong("lastTickTime", this.lastTickTime);
        nbt.putBoolean("activated", this.activated);
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new AutoCrafterScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return (DefaultedList<ItemStack>) this.craftingTable.getHeldStacks();
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.craftingTable.setHeldStacks(inventory);
    }

    @Override
    public int getWidth() {
        return this.craftingTable.getWidth();
    }

    @Override
    public int getHeight() {
        return this.craftingTable.getHeight();
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        this.craftingTable.provideRecipeInputs(finder);
    }

    private DefaultedList<ItemStack> combineItemStacks(DefaultedList<ItemStack> stacks) {
        Map<Item, Long> countsOfEachItem = stacks.stream().filter(x -> !x.isEmpty()).collect(Collectors.groupingBy(ItemStack::getItem, Collectors.mapping(ItemStack::getCount, Collectors.counting())));

        return countsOfEachItem.entrySet().stream().flatMap(entry -> {
            int maxItemCountPerStack = entry.getKey().getMaxCount();
            int numberOfFullStacks = entry.getValue().intValue() / maxItemCountPerStack;
            int remainingStackSize = entry.getValue().intValue() % maxItemCountPerStack;

            DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(numberOfFullStacks + (remainingStackSize == 0 ? 0 : 1), new ItemStack(entry.getKey(), maxItemCountPerStack));
            itemStacks.getLast().setCount(remainingStackSize);
            return itemStacks.stream();
        }).collect(Utils.toDefaultedList());
    }

    private boolean contains(DefaultedList<ItemStack> toFind) {
        DefaultedList<ItemStack> temp = DefaultedList.ofSize(toFind.size());
        temp.addAll(toFind);

        Map<Item, Long> mainInventoryItemCounts = mainInventory.stream().filter(x -> !x.isEmpty()).collect(Collectors.groupingBy(ItemStack::getItem, Collectors.mapping(ItemStack::getCount, Collectors.counting())));
        Map<Item, Long> toFindItemCounts = toFind.stream().filter(x -> !x.isEmpty()).collect(Collectors.groupingBy(ItemStack::getItem, Collectors.mapping(ItemStack::getCount, Collectors.counting())));

        for (Map.Entry<Item, Long> toFindItem : toFindItemCounts.entrySet()) {
            if (!mainInventoryItemCounts.containsKey(toFindItem.getKey()) || mainInventoryItemCounts.get(toFindItem.getKey()) < toFindItem.getValue())
                return false;
        }
        return true;
    }

    private DefaultedList<ItemStack> getNeeded(DefaultedList<ItemStack> stacks) {
        DefaultedList<ItemStack> remaining = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack r = stacks.get(i).copy();
            if (r.getCount() > 0)
                r.setCount(1);

            remaining.set(i, r);
        }

        return remaining;
    }

    @Override
    public void clear() {
        mainInventory.clear();
        craftingTable.clear();
        outputStack = ItemStack.EMPTY;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0) return outputStack;
        if (slot <= CRAFTING_TABLE_SIZE + 1) return craftingTable.getStack(slot - 1);
        return mainInventory.get(slot - CRAFTING_TABLE_SIZE - 1);
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
        ItemStack oldStack = null;
        if (slot == 0) {
            oldStack = outputStack.copy();
            outputStack = ItemStack.EMPTY;
        }
        if (slot <= CRAFTING_TABLE_SIZE + 1) {
            oldStack = craftingTable.removeStack(slot - 1);
        }
        if (oldStack == null) {
            oldStack = mainInventory.get(slot - CRAFTING_TABLE_SIZE - 1).copy();
            mainInventory.set(slot - CRAFTING_TABLE_SIZE - 1, ItemStack.EMPTY);
        }
        return oldStack;
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = null;
        if (slot == 0) result = outputStack.split(count);
        if (slot <= CRAFTING_TABLE_SIZE + 1) result = craftingTable.splitStack(slot - 1, count);
        if (result == null) result = Inventories.splitStack(mainInventory, slot - CRAFTING_TABLE_SIZE - 1, count);

        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (stack.getCount() > getMaxCount(stack)) {
            stack.setCount(getMaxCount(stack));
        }

        if (slot == 0) outputStack = stack;
        else if (slot <= CRAFTING_TABLE_SIZE + 1) craftingTable.setStack(slot - 1, stack);
        else mainInventory.set(slot - CRAFTING_TABLE_SIZE - 1, stack);
    }

    @Override
    public int size() {
        return mainInventory.size() + craftingTable.size() + 1;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 0 && dir == Direction.DOWN;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot > CRAFTING_TABLE_SIZE && dir != Direction.DOWN;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return OTHER_SLOTS;
        }
    }

    public static void serverTick(World world, BlockPos blockPos, BlockState blockState, AutoCrafterBlockEntity autoCrafterBlockEntity) {
        if (!(world instanceof ServerWorld))
            return;

        --autoCrafterBlockEntity.craftCooldown;
        if (autoCrafterBlockEntity.craftCooldown == 0) {
            world.setBlockState(blockPos, blockState.with(AutoCrafterBlock.CRAFTING, Boolean.FALSE), Block.NOTIFY_ALL);
        }

        autoCrafterBlockEntity.lastTickTime = world.getTime();
        if (autoCrafterBlockEntity.needsCooldown()) {
            autoCrafterBlockEntity.setCooldown(0);
            autoCrafterBlockEntity.tryCraft((ServerWorld)world, blockState);
        }
    }

    boolean needsCooldown() {
        return this.craftCooldown > 0;
    }

    void setCooldown(int cooldown) {
        this.craftCooldown = cooldown;
    }

    public void removeStacks(DefaultedList<ItemStack> stacksToRemove) {
        Map<Item, Long> removalItemCounts = stacksToRemove.stream().filter(x -> !x.isEmpty()).collect(Collectors.groupingBy(ItemStack::getItem, Collectors.mapping(ItemStack::getCount, Collectors.counting())));

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack currentItemStack = this.mainInventory.get(i);
            Item currentItem = currentItemStack.getItem();
            int currentItemCount = currentItemStack.getCount();
            if (!removalItemCounts.containsKey(currentItem))
                continue;

            int removalAmount = removalItemCounts.get(currentItem).intValue();
            if (removalAmount == 0) continue;
            if (removalAmount < currentItemCount) {
                currentItemStack.setCount(currentItemCount - removalAmount);
                removalItemCounts.put(currentItem, 0L);
            } else {
                this.mainInventory.set(i, ItemStack.EMPTY);
                removalItemCounts.put(currentItemStack.getItem(), (long) (removalAmount - currentItemCount));
            }
        }

        assert removalItemCounts.values().stream().allMatch(x -> x == 0L) : "Not everything was removed from the crafting inventory :( this is a logic bug as we should have checked the inventory had the necessary item stacks before getting here." ;
    }

    public void tryCraft(ServerWorld world, BlockState blockState) {
        CraftingRecipeInput recipeInput = this.craftingTable.createRecipeInput();
        if (world.isReceivingRedstonePower(pos))
            return;

        Optional<RecipeEntry<CraftingRecipe>> optionalCraftingRecipe = recipeCache.getRecipe(world, recipeInput);
        if (optionalCraftingRecipe.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }

        CraftingRecipe craftingRecipe = optionalCraftingRecipe.get().value();
        ItemStack crafted = craftingRecipe.craft(recipeInput, world.getRegistryManager());
        if (crafted.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }

        DefaultedList<ItemStack> neededRecipeStacks = combineItemStacks(getNeeded((DefaultedList<ItemStack>)craftingTable.getHeldStacks()));
        if (!contains(neededRecipeStacks)) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }

        if ((!outputStack.isOf(crafted.getItem()) && !outputStack.isEmpty())
                || (outputStack.isOf(crafted.getItem()) && outputStack.getCount() + crafted.getCount() > outputStack.getMaxCount())) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }

        world.setBlockState(pos, blockState.with(CRAFTING, Boolean.TRUE), Block.NOTIFY_LISTENERS);
        crafted.onCraftByCrafter(world);

        if (outputStack.isEmpty())
            outputStack = crafted;
        else
            outputStack.setCount(outputStack.getCount() + crafted.getCount());

        setCooldown(CRAFTING_COOLDOWN);
        removeStacks(neededRecipeStacks);

        world.syncWorldEvent(WorldEvents.CRAFTER_CRAFTS, pos, 0);
        markDirty();
    }
}
