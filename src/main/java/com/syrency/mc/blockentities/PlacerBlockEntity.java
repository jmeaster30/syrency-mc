package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.helpers.ImplementedInventory;
import com.syrency.mc.screens.PlacerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

import static java.util.function.Predicate.not;

public class PlacerBlockEntity extends BlockEntity
        implements ImplementedInventory, NamedScreenHandlerFactory {

    private static final int PLACER_COOLDOWN = 4;
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);

    private int placingCooldown = -1;

    public PlacerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SyrencyMod.PLACER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        Inventories.writeNbt(nbt, this.items, registryLookup);
        nbt.putInt("placingCooldown", this.placingCooldown);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        Inventories.readNbt(nbt, this.items, registryLookup);
        this.placingCooldown = nbt.getInt("placingCooldown");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new PlacerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public boolean needsCooldown()
    {
        return this.placingCooldown > 0;
    }

    public void setCooldown(int cooldown)
    {
        this.placingCooldown = cooldown;
    }

    public static void serverTick(World world, BlockPos blockPos, BlockState blockState, PlacerBlockEntity placerBlockEntity) {
        if (!(world instanceof ServerWorld))
            return;

        --placerBlockEntity.placingCooldown;
        if (placerBlockEntity.needsCooldown()) {
            placerBlockEntity.setCooldown(0);
            placerBlockEntity.tryPlace((ServerWorld) world, blockState);
        }
    }

    public void tryPlace(ServerWorld world, BlockState blockState)
    {
        Direction facing = blockState.get(Properties.FACING);
        BlockPos inFront = pos.add(facing.getVector());

        if (!world.isReceivingRedstonePower(pos))
            return;

        //if there is a block in front of the placer do nothing
        BlockState toReplaceBlockState = world.getBlockState(inFront);

        //if there is a block in front of the placer do nothing
        BlockState frontState = world.getBlockState(inFront);
        if (!frontState.isAir()) {
            return;
        }

        Optional<ItemStack> optionalItemStackToPlace = items.stream().filter(not(ItemStack::isEmpty)).findFirst();
        if (optionalItemStackToPlace.isEmpty())
            return;

        ItemStack itemStackToPlace = optionalItemStackToPlace.get();
        Item itemToPlace = itemStackToPlace.getItem();

        Block newBlock = Block.getBlockFromItem(itemToPlace);
        if (newBlock.getDefaultState().isAir()) {
            ItemStack toDrop = new ItemStack(itemToPlace, 1);
            Block.dropStack(world, inFront, toDrop);
        } else {
            BlockState newBlockState = newBlock.getDefaultState();
            Block.replace(frontState, newBlockState, world, inFront, Block.NOTIFY_ALL_AND_REDRAW);
        }

        itemStackToPlace.decrement(1);

        setCooldown(PLACER_COOLDOWN);
        //world.setBlockState(pos, blockState.with(PlacerBlock.ACTIVE, false));
    }
}
