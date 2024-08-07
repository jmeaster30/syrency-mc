package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.blocks.SplitterBlock;
import com.syrency.mc.screens.SplitterScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SplitterBlockEntity extends LootableContainerBlockEntity implements SidedInventory, Hopper, Tickable {

    private static final int ItemTransferSize = 1;
    private static final int TotalInventorySize = 9;
    private static final int StorageSize = 5;
    private static final int[] STORAGE_SLOTS = new int[]{0, 1, 2, 3, 4};
    private DefaultedList<ItemStack> inventory;
    private int transferCooldown;
    private long lastTickTime;
    private Random rng;
  
  /*
  Slot 0-4 : Storage
  Slot 5 : North
  Slot 6 : East
  Slot 7 : South
  Slot 8 : West
  */

    public SplitterBlockEntity() {
        super(SyrencyMod.SPLITTER_ENTITY);
        this.inventory = DefaultedList.ofSize(TotalInventorySize, ItemStack.EMPTY);
        this.transferCooldown = -1;
        rng = new Random();
    }

    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory) inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
    }

    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return getAvailableSlots(inv, facing).allMatch((i) -> {
            return inv.getStack(i).isEmpty();
        });
    }

    public static boolean extract(Hopper hopper) {
        Inventory inventory = getInputInventory(hopper);
        if (inventory != null) {
            Direction direction = Direction.DOWN;
            return isInventoryEmpty(inventory, direction) ? false : getAvailableSlots(inventory, direction).anyMatch((i) -> {
                return extract(hopper, inventory, i, direction);
            });
        } else {
            Iterator<ItemEntity> var2 = getInputItemEntities(hopper).iterator();

            ItemEntity itemEntity;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                itemEntity = (ItemEntity) var2.next();
            } while (!extract(hopper, itemEntity));

            return false;
        }
    }

    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
        ItemStack itemStack = inventory.getStack(slot);
        if (!itemStack.isEmpty() && canExtract(inventory, itemStack, slot, side)) {
            ItemStack itemStack2 = itemStack.copy();
            ItemStack itemStack3 = transfer(inventory, hopper, inventory.removeStack(slot, ItemTransferSize), (Direction) null);
            if (itemStack3.isEmpty()) {
                inventory.markDirty();
                return true;
            }

            inventory.setStack(slot, itemStack2);
        }

        return false;
    }

    public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack itemStack = itemEntity.getStack().copy();
        ItemStack itemStack2 = transfer((Inventory) null, inventory, itemStack, (Direction) null);
        if (itemStack2.isEmpty()) {
            bl = true;
            itemEntity.remove();
        } else {
            itemEntity.setStack(itemStack2);
        }

        return bl;
    }

    public static ItemStack transfer(Inventory from, Inventory to, ItemStack stack, Direction side) {
        if (to instanceof SidedInventory && side != null) {
            SidedInventory sidedInventory = (SidedInventory) to;
            int[] is = sidedInventory.getAvailableSlots(side);

            for (int i = 0; i < is.length && !stack.isEmpty(); ++i) {
                stack = transfer(from, to, stack, is[i], side);
            }
        } else {
            int j = to.size();

            for (int k = 0; k < j && !stack.isEmpty(); ++k) {
                stack = transfer(from, to, stack, k, side);
            }
        }

        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else {
            return !(inventory instanceof SidedInventory) || ((SidedInventory) inventory).canInsert(slot, stack, side);
        }
    }

    private static boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing) {
        return !(inv instanceof SidedInventory) || ((SidedInventory) inv).canExtract(slot, stack, facing);
    }

    private static ItemStack transfer(Inventory from, Inventory to, ItemStack stack, int slot, Direction direction) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, direction)) {
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
                if (bl2 && to instanceof SplitterBlockEntity) {
                    SplitterBlockEntity hopperBlockEntity = (SplitterBlockEntity) to;
                    if (!hopperBlockEntity.isDisabled()) {
                        int k = 0;
                        if (from instanceof SplitterBlockEntity) {
                            SplitterBlockEntity hopperBlockEntity2 = (SplitterBlockEntity) from;
                            if (hopperBlockEntity.lastTickTime >= hopperBlockEntity2.lastTickTime) {
                                k = 1;
                            }
                        }

                        hopperBlockEntity.setCooldown(8 - k);
                    }
                }

                to.markDirty();
            }
        }

        return stack;
    }

    public static Inventory getInputInventory(Hopper hopper) {
        return getInventoryAt(hopper.getWorld(), hopper.getHopperX(), hopper.getHopperY() + 1.0D, hopper.getHopperZ());
    }

    public static List<ItemEntity> getInputItemEntities(Hopper hopper) {
        return (List<ItemEntity>) hopper.getInputAreaShape().getBoundingBoxes().stream().flatMap((box) -> {
            return hopper.getWorld().getEntitiesByClass(ItemEntity.class, box.offset(hopper.getHopperX() - 0.5D, hopper.getHopperY() - 0.5D, hopper.getHopperZ() - 0.5D), EntityPredicates.VALID_ENTITY).stream();
        }).collect(Collectors.toList());
    }

    public static Inventory getInventoryAt(World world, BlockPos blockPos) {
        return getInventoryAt(world, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D);
    }

    public static Inventory getInventoryAt(World world, double x, double y, double z) {
        Inventory inventory = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
        } else if (block.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory) blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
                }
            }
        }

        if (inventory == null) {
            List<Entity> list = world.getOtherEntities((Entity) null, new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory) list.get(world.random.nextInt(list.size()));
            }
        }

        return (Inventory) inventory;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areTagsEqual(first, second);
        }
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.fromTag(tag, this.inventory);
        }

        this.transferCooldown = tag.getInt("TransferCooldown");
    }

    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (!this.serializeLootTable(tag)) {
            Inventories.toTag(tag, this.inventory);
        }

        tag.putInt("TransferCooldown", this.transferCooldown);
        return tag;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    public ItemStack removeStack(int slot, int amount) {
        this.checkLootInteraction((PlayerEntity) null);
        return Inventories.splitStack(this.getInvStackList(), slot, amount);
    }

    public void setStack(int slot, ItemStack stack) {
        this.checkLootInteraction((PlayerEntity) null);
        this.getInvStackList().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    protected Text getContainerName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    public void tick() {
        if (this.world != null && !this.world.isClient) {
            --this.transferCooldown;
            this.lastTickTime = this.world.getTime();
            if (!this.needsCooldown()) {
                this.setCooldown(0);
                this.insertAndExtract(() -> {
                    return extract(this);
                });
            }
        }
    }

    private boolean insertAndExtract(Supplier<Boolean> extractMethod) {
        if (this.world != null && !this.world.isClient) {
            if (!this.needsCooldown() && (Boolean) this.getCachedState().get(SplitterBlock.ENABLED)) {
                boolean bl = false;
                if (!this.isEmpty()) {
                    bl = this.insert();
                }

                if (!this.isFull()) {
                    bl |= (Boolean) extractMethod.get();
                }

                if (bl) {
                    this.setCooldown(8);
                    this.markDirty();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean isFull() {
        Iterator<ItemStack> var1 = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack) var1.next();
        } while (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());

        return false;
    }

    private boolean insert() {
        ItemStack toMove = null;
        int toMoveIndex = -1;
        for (int i = 0; i < StorageSize; ++i) {
            if (!this.getStack(i).isEmpty()) {
                toMove = this.getStack(i);
                toMoveIndex = i;
                break;
            }
        }

        if (toMove == null) {
            return false;
        }

        Direction outputDirection = this.getOutputDirection(toMove);
        Inventory inventory = this.getOutputInventory(outputDirection);
        if (inventory == null) {
            return false;
        } else {
            Direction direction = outputDirection.getOpposite();
            if (this.isInventoryFull(inventory, direction)) {
                return false;
            } else {
                ItemStack itemStack = toMove.copy();
                ItemStack itemStack2 = transfer(this, inventory, this.removeStack(toMoveIndex, ItemTransferSize), direction);
                if (itemStack2.isEmpty()) {
                    inventory.markDirty();
                    return true;
                }

                this.setStack(toMoveIndex, itemStack);
                return false;
            }
        }
    }

    private boolean isInventoryFull(Inventory inv, Direction direction) {
        return getAvailableSlots(inv, direction).allMatch((i) -> {
            ItemStack itemStack = inv.getStack(i);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }

    private Direction getOutputDirection(ItemStack toMove) {
        ArrayList<Direction> options = new ArrayList<Direction>();
        for (int i = StorageSize; i < TotalInventorySize; i++) {
            if (toMove.getItem().equals(inventory.get(i).getItem())) {
                switch (i) {
                    case 5:
                        options.add(Direction.NORTH);
                        break;
                    case 6:
                        options.add(Direction.EAST);
                        break;
                    case 7:
                        options.add(Direction.SOUTH);
                        break;
                    case 8:
                        options.add(Direction.WEST);
                        break;
                    default:
                        break;
                }
            }
        }
        return options.size() == 0 ? Direction.DOWN : options.get(rng.nextInt(options.size()));
    }

    private Inventory getOutputInventory(Direction direction) {
        return getInventoryAt(this.getWorld(), this.pos.offset(direction));
    }

    public double getHopperX() {
        return (double) this.pos.getX() + 0.5D;
    }

    public double getHopperY() {
        return (double) this.pos.getY() + 0.5D;
    }

    public double getHopperZ() {
        return (double) this.pos.getZ() + 0.5D;
    }

    private void setCooldown(int cooldown) {
        this.transferCooldown = cooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    private boolean isDisabled() {
        return this.transferCooldown > 8;
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    public void onEntityCollided(Entity entity) {
        if (entity instanceof ItemEntity) {
            BlockPos blockPos = this.getPos();
            if (VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset((double) (-blockPos.getX()), (double) (-blockPos.getY()), (double) (-blockPos.getZ()))), this.getInputAreaShape(), BooleanBiFunction.AND)) {
                this.insertAndExtract(() -> {
                    return extract(this, (ItemEntity) entity);
                });
            }
        }

    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new SplitterScreenHandler(syncId, playerInventory, this);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.UP && slot < StorageSize) {
            return true;
        }
        return false;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return STORAGE_SLOTS;
    }
}
