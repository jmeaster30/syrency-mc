package net.fabricmc.mechanicsplus.blockentities;

import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.fabricmc.mechanicsplus.helpers.ImplementedInventory;
import net.fabricmc.mechanicsplus.screens.AutoCrafterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;

public class AutoCrafterBlockEntity extends BlockEntity
    implements ImplementedInventory, NamedScreenHandlerFactory, Tickable {

  private final DefaultedList<ItemStack> items = DefaultedList.ofSize(37, ItemStack.EMPTY);
  //0 : output
  //1-9 : crafting table
  //10-36 : block inventory

  public AutoCrafterBlockEntity() {
    super(MechanicsPlusMod.AUTOCRAFTER_BLOCK_ENTITY);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    super.toTag(tag);

    Inventories.toTag(tag, items);

    return tag;
  }

  @Override
  public void fromTag(BlockState state, CompoundTag tag) {
    super.fromTag(state, tag);

    Inventories.fromTag(tag, items);
  }

  @Override
  public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
    return new AutoCrafterScreenHandler(syncId, playerInventory, this);
  }

  @Override
  public Text getDisplayName() {
    return new TranslatableText(getCachedState().getBlock().getTranslationKey());
  }

  @Override
  public DefaultedList<ItemStack> getItems() {
    return items;
  }

  @Override
  public void tick() {
    
  }
}
