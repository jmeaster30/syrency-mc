package com.syrency.mc;

import com.syrency.mc.helpers.DefaultedListCollector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

import java.util.stream.Collector;

public class Utils {
    public static NbtCompound writeInventoryNbt(NbtCompound nbt, String key, DefaultedList<ItemStack> stacks, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = new NbtList();

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack itemStack = stacks.get(i);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte)i);
                nbtList.add(itemStack.encode(registries, nbtCompound));
            }
        }

        nbt.put(key, nbtList);
        return nbt;
    }

    public static void readInventoryNbt(NbtCompound nbt, String key, DefaultedList<ItemStack> stacks, RegistryWrapper.WrapperLookup registries) {
        NbtList nbtList = nbt.getList(key, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            if (j < stacks.size()) {
                stacks.set(j, ItemStack.fromNbt(registries, nbtCompound).orElse(ItemStack.EMPTY));
            }
        }
    }

    public static void writeItemStackNbt(NbtCompound nbt, String key, ItemStack stack, RegistryWrapper.WrapperLookup registries)
    {
        nbt.put(key, stack.encode(registries));
    }

    public static ItemStack readItemStackNbt(NbtCompound nbt, String key, RegistryWrapper.WrapperLookup registries)
    {
        NbtCompound element = nbt.getCompound(key);
        return ItemStack.fromNbtOrEmpty(registries, element);
    }

    public static <T> Collector<T, ?, DefaultedList<T>> toDefaultedList() {
        return new DefaultedListCollector<>();
    }
}
