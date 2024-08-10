package com.syrency.mc.blockentities;

import com.syrency.mc.SyrencyMod;
import com.syrency.mc.blocks.AutoCrafterBlock;
import com.syrency.mc.blocks.BreakerBlock;
import com.syrency.mc.helpers.ImplementedInventory;
import com.syrency.mc.screens.BreakerScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
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

import java.util.ArrayList;
import java.util.List;

public class BreakerBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
    private static final int BREAKER_MAX_COOLDOWN = 4;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int breakerCooldown = -1;

    public BreakerBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(SyrencyMod.BREAKER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        super.readNbt(nbt, registry);
        Inventories.readNbt(nbt, items, registry);
        this.breakerCooldown = nbt.getInt("breakerCooldown");
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registry)
    {
        super.writeNbt(nbt, registry);
        Inventories.writeNbt(nbt, items, registry);
        nbt.putInt("breakerCooldown", this.breakerCooldown);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    // returns the leftover items that were not added
    public List<ItemStack> addToInventory(List<ItemStack> toAdd) {
        List<ItemStack> notAdded = new ArrayList<>();

        for (ItemStack itemStack : toAdd) {
            ItemStack leftovers = itemStack;
            for (int i = 0; i < items.size(); i++) {
                leftovers = addStack(i, leftovers);
                if (leftovers == null)
                    break;
            }

            if (leftovers != null) {
                notAdded.add(leftovers);
            }
        }

        return notAdded;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BreakerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public boolean needsCooldown()
    {
        return this.breakerCooldown > 0;
    }

    public void setCooldown(int cooldown)
    {
        this.breakerCooldown = cooldown;
    }

    public static void serverTick(World world, BlockPos blockPos, BlockState blockState, BreakerBlockEntity breakerBlockEntity) {
        if (!(world instanceof ServerWorld))
            return;

        // TODO I took this from AutoCrafterBlockEntity and it can be used to turn off the breaker block's active state
        //--breakerBlockEntity.breakActiveCooldown;
        //if (breakerBlockEntity.breakActiveCooldown == 0) {
        //    world.setBlockState(blockPos, blockState.with(AutoCrafterBlock.CRAFTING, Boolean.FALSE), Block.NOTIFY_ALL);
        //}

        --breakerBlockEntity.breakerCooldown;
        if (breakerBlockEntity.needsCooldown()) {
            breakerBlockEntity.setCooldown(0);
            breakerBlockEntity.tryBreak((ServerWorld)world);
        }
    }

    public void tryBreak(ServerWorld world)
    {
        Direction facingDirection = this.getCachedState().get(Properties.FACING);
        BlockPos inFront = pos.add(facingDirection.getVector());
        BlockState stateOfBlockToBreak = world.getBlockState(inFront);

        // negative hardness seems to be associated with non-mine-able blocks
        if (!stateOfBlockToBreak.hasBlockEntity()
                || stateOfBlockToBreak.isAir()
                || stateOfBlockToBreak.getHardness(world, pos) < 0
                || !world.isReceivingRedstonePower(pos)) {
            return;
        }

        BlockEntity breakingEntity = world.getBlockEntity(inFront);
        List<ItemStack> droppedStacks = Block.getDroppedStacks(stateOfBlockToBreak, world, inFront, breakingEntity);

        List<ItemStack> leftovers = addToInventory(droppedStacks);
        leftovers.forEach((itemStack) -> Block.dropStack(world, pos, itemStack));

        int maxUpdateDepth = 64;
        world.breakBlock(inFront, false, null, maxUpdateDepth);
        setCooldown(BREAKER_MAX_COOLDOWN);
    }
}