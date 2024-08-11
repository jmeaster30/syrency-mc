package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.Utils;
import com.syrency.mc.screens.SplitterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;
import java.util.stream.Stream;

// TODO I don't know if I want the splitter to work EXACTLY like a hopper. Things may be simpler to implement maybe
public class SplitterBlockEntity extends AbstractHopperBlockEntity implements SidedInventory {

    private static final DirectionProperty FACING_PROPERTY = Properties.FACING;
    private static final int ItemTransferSize = 1;
    private static final int ItemTransferCooldown = 2;
    private static final int StorageSize = 5;
    private static final int[] STORAGE_SLOTS = new int[]{0, 1, 2, 3, 4};
    private final Random rng = new Random();
    private final DefaultedList<ItemStack> filterItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
  
  /*
  Slot 0-4 : Storage
  Slot 5 : North
  Slot 6 : East
  Slot 7 : South
  Slot 8 : West
  */

    public SplitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.SPLITTER_ENTITY, blockPos, blockState, FACING_PROPERTY, StorageSize, ItemTransferSize, ItemTransferCooldown);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Utils.readInventoryNbt(nbt, "filterItems", this.filterItems, registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Utils.writeInventoryNbt(nbt, "filterItems", this.filterItems, registryLookup);
    }

    @Override
    protected Direction getOutputDirection(ItemStack toMove) {
        return Stream.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                .filter(x -> toMove.isOf(filterItems.get(x.ordinal() - 2).getItem()))
                .collect(Utils.selectRandom(rng))
                .orElse(Direction.DOWN);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new SplitterScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.UP && slot < StorageSize;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return STORAGE_SLOTS;
    }
}
