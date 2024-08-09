package com.syrency.mc.blockentities;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public abstract class AbstractHopperBlockEntity extends LootableContainerBlockEntity implements Hopper {
    private static final int[][] AVAILABLE_SLOTS_CACHE = new int[54][];
    protected final int ItemTransferSize;
    protected final int TransferCooldown;
    protected DefaultedList<ItemStack> inventory;
    protected int transferCooldown = -1;
    protected long lastTickTime;
    protected Direction facing;
    protected DirectionProperty facingProperty;

    protected AbstractHopperBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, DirectionProperty facingProperty, int inventorySize, int itemTransferSize, int transferCooldown) {
        super(blockEntityType, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(inventorySize, ItemStack.EMPTY);
        this.facingProperty = facingProperty;
        this.facing = blockState.get(facingProperty);

        this.ItemTransferSize = itemTransferSize;
        this.TransferCooldown = transferCooldown;
    }

    public static <T extends AbstractHopperBlockEntity> void serverTick(World world, BlockPos pos, BlockState state, T blockEntity) {
        --blockEntity.transferCooldown;
        blockEntity.lastTickTime = world.getTime();
        if (!blockEntity.needsCooldown()) {
            blockEntity.setTransferCooldown(0);
            insertAndExtract(world, pos, state, blockEntity, () -> extract(world, blockEntity, blockEntity.ItemTransferSize));
        }
    }

    private static <T extends AbstractHopperBlockEntity> boolean insertAndExtract(World world, BlockPos pos, BlockState state, T blockEntity, BooleanSupplier booleanSupplier) {
        if (world.isClient || blockEntity.needsCooldown() || !state.get(HopperBlock.ENABLED)) {
            return false;
        }

        boolean insertedOrExtracted = false;
        if (!blockEntity.isEmpty()) {
            insertedOrExtracted = insert(world, pos, blockEntity);
        }

        if (!blockEntity.isFull()) {
            insertedOrExtracted |= booleanSupplier.getAsBoolean();
        }

        if (insertedOrExtracted) {
            blockEntity.setTransferCooldown(blockEntity.TransferCooldown);
            markDirty(world, pos, state);
            return true;
        }

        return false;
    }

    private static <T extends AbstractHopperBlockEntity> boolean insert(World world, BlockPos pos, T blockEntity) {
        Inventory inventory = getOutputInventory(world, pos, blockEntity);
        if (inventory == null)
            return false;

        Direction direction = blockEntity.facing.getOpposite();
        if (isInventoryFull(inventory, direction))
            return false;

        for (int i = 0; i < blockEntity.size(); i++) {
            ItemStack itemStack = blockEntity.getStack(i);
            if (itemStack.isEmpty())
                continue;

            int j = itemStack.getCount();
            ItemStack itemStack2 = transfer(blockEntity, inventory, blockEntity.removeStack(i, blockEntity.ItemTransferSize), direction);
            if (itemStack2.isEmpty()) {
                inventory.markDirty();
                return true;
            }

            itemStack.setCount(j);
            if (j == 1) {
                blockEntity.setStack(i, itemStack);
            }
        }

        return false;
    }

    private static int[] getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory sidedInventory) {
            return sidedInventory.getAvailableSlots(side);
        } else {
            int i = inventory.size();
            if (i < AVAILABLE_SLOTS_CACHE.length) {
                int[] is = AVAILABLE_SLOTS_CACHE[i];
                if (is != null) {
                    return is;
                } else {
                    int[] js = indexArray(i);
                    AVAILABLE_SLOTS_CACHE[i] = js;
                    return js;
                }
            } else {
                return indexArray(i);
            }
        }
    }

    private static int[] indexArray(int size) {
        int[] is = new int[size];
        int i = 0;

        while (i < is.length) {
            is[i] = i++;
        }

        return is;
    }

    private static boolean isInventoryFull(Inventory inventory, Direction direction) {
        int[] is = getAvailableSlots(inventory, direction);

        for (int i : is) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.getCount() < itemStack.getMaxCount()) {
                return false;
            }
        }

        return true;
    }

    public static boolean extract(World world, Hopper hopper, int transferSize) {
        BlockPos blockPos = BlockPos.ofFloored(hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
        BlockState blockState = world.getBlockState(blockPos);
        Inventory inventory = getInputInventory(world, hopper, blockPos, blockState);
        if (inventory != null) {
            Direction direction = Direction.DOWN;

            for (int i : getAvailableSlots(inventory, direction)) {
                if (extract(hopper, inventory, i, direction, transferSize)) {
                    return true;
                }
            }

            return false;
        } else {
            boolean bl = hopper.canBlockFromAbove() && blockState.isFullCube(world, blockPos) && !blockState.isIn(BlockTags.DOES_NOT_BLOCK_HOPPERS);
            if (!bl) {
                for (ItemEntity itemEntity : getInputItemEntities(world, hopper)) {
                    if (extract(hopper, itemEntity)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side, int transferSize) {
        ItemStack itemStack = inventory.getStack(slot);
        if (!itemStack.isEmpty() && canExtract(hopper, inventory, itemStack, slot, side)) {
            int i = itemStack.getCount();
            ItemStack itemStack2 = transfer(inventory, hopper, inventory.removeStack(slot, transferSize), null);
            if (itemStack2.isEmpty()) {
                inventory.markDirty();
                return true;
            }

            itemStack.setCount(i);
            if (i == 1) {
                inventory.setStack(slot, itemStack);
            }
        }

        return false;
    }

    public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getStack().copy();
        ItemStack itemStack2 = transfer(null, inventory, itemStack, null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.setStack(ItemStack.EMPTY);
            itemEntity.discard();
        } else {
            itemEntity.setStack(itemStack2);
        }

        return bl;
    }

    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory sidedInventory && side != null) {
            int[] is = sidedInventory.getAvailableSlots(side);

            for (int i = 0; i < is.length && !stack.isEmpty(); i++) {
                stack = transfer(from, to, stack, is[i], side);
            }

            return stack;
        }

        int j = to.size();

        for (int i = 0; i < j && !stack.isEmpty(); i++) {
            stack = transfer(from, to, stack, i, side);
        }

        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else {
            return !(inventory instanceof SidedInventory sidedInventory) || sidedInventory.canInsert(slot, stack, side);
        }
    }

    private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
        if (!fromInventory.canTransferTo(hopperInventory, slot, stack)) {
            return false;
        } else {
            return !(fromInventory instanceof SidedInventory sidedInventory) || sidedInventory.canExtract(slot, stack, facing);
        }
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, side)) {
            boolean bl = false;
            boolean bl2 = to.isEmpty();
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxCount() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                bl = j > 0;
            }

            if (bl) {
                if (bl2 && to instanceof AbstractHopperBlockEntity hopperBlockEntity && !hopperBlockEntity.isDisabled()) {
                    int j = 0;
                    if (from instanceof AbstractHopperBlockEntity hopperBlockEntity2 && hopperBlockEntity.lastTickTime >= hopperBlockEntity2.lastTickTime) {
                        j = 1;
                    }

                    hopperBlockEntity.setTransferCooldown(hopperBlockEntity.TransferCooldown - j);
                }

                to.markDirty();
            }
        }

        return stack;
    }

    @Nullable
    private static Inventory getOutputInventory(World world, BlockPos pos, AbstractHopperBlockEntity blockEntity) {
        return getInventoryAt(world, pos.offset(blockEntity.facing));
    }

    @Nullable
    private static Inventory getInputInventory(World world, Hopper hopper, BlockPos pos, BlockState state) {
        return getInventoryAt(world, pos, state, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
    }

    public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
        Box box = hopper.getInputAreaShape().offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5);
        return world.getEntitiesByClass(ItemEntity.class, box, EntityPredicates.VALID_ENTITY);
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return getInventoryAt(world, pos, world.getBlockState(pos), (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, BlockPos pos, BlockState state, double x, double y, double z) {
        Inventory inventory = getBlockInventoryAt(world, pos, state);
        if (inventory == null) {
            inventory = getEntityInventoryAt(world, x, y, z);
        }

        return inventory;
    }

    @Nullable
    private static Inventory getBlockInventoryAt(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof InventoryProvider) {
            return ((InventoryProvider) block).getInventory(state, world, pos);
        } else if (state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory inventory) {
            if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                inventory = ChestBlock.getInventory((ChestBlock) block, state, world, pos, true);
            }

            return inventory;
        } else {
            return null;
        }
    }

    @Nullable
    private static Inventory getEntityInventoryAt(World world, double x, double y, double z) {
        List<Entity> list = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
        return !list.isEmpty() ? (Inventory) list.get(world.random.nextInt(list.size())) : null;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        return first.getCount() <= first.getMaxCount() && ItemStack.areItemsAndComponentsEqual(first, second);
    }

    public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, AbstractHopperBlockEntity blockEntity) {
        if (entity instanceof ItemEntity itemEntity
                && !itemEntity.getStack().isEmpty()
                && entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()).intersects(blockEntity.getInputAreaShape())) {
            insertAndExtract(world, pos, state, blockEntity, () -> extract(blockEntity, itemEntity));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory, registryLookup);
        }

        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory, registryLookup);
        }

        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.generateLoot(null);
        return Inventories.splitStack(this.getHeldStacks(), slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.generateLoot(null);
        this.getHeldStacks().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.facing = state.get(facingProperty);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    boolean isFull() {
        for (ItemStack itemStack : this.inventory) {
            if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxCount()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public double getHopperX() {
        return (double) this.pos.getX() + 0.5;
    }

    @Override
    public double getHopperY() {
        return (double) this.pos.getY() + 0.5;
    }

    @Override
    public double getHopperZ() {
        return (double) this.pos.getZ() + 0.5;
    }

    @Override
    public boolean canBlockFromAbove() {
        return true;
    }

    void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    private boolean isDisabled() {
        return this.transferCooldown > this.TransferCooldown;
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }
}
