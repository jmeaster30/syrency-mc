package net.fabricmc.mechanicsplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.mechanicsplus.blockentities.*;
import net.fabricmc.mechanicsplus.blocks.*;
import net.fabricmc.mechanicsplus.screens.*;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MechanicsPlusMod implements ModInitializer {

  public static final Identifier BREAKER = new Identifier("mechanicsplus", "breaker_block");
  public static final Identifier AUTOCRAFTER = new Identifier("mechanicsplus", "autocrafter_block");
  public static final Identifier PLACER = new Identifier("mechanicsplus", "placer_block");
  public static final Identifier FASTHOPPER = new Identifier("mechanicsplus", "fast_hopper");

  public static final BreakerBlock BREAKER_BLOCK;
  public static final BlockItem BREAKER_BLOCK_ITEM;
  public static final BlockEntityType<BreakerBlockEntity> BREAKER_BLOCK_ENTITY;
  public static final ScreenHandlerType<BreakerScreenHandler> BREAKER_SCREEN_HANDLER;

  public static final AutoCrafterBlock AUTOCRAFTER_BLOCK;
  public static final BlockItem AUTOCRAFTER_BLOCK_ITEM;
  public static final BlockEntityType<AutoCrafterBlockEntity> AUTOCRAFTER_BLOCK_ENTITY;
  public static final ScreenHandlerType<AutoCrafterScreenHandler> AUTOCRAFTER_SCREEN_HANDLER;

  public static final PlacerBlock PLACER_BLOCK;
  public static final BlockItem PLACER_BLOCK_ITEM;
  public static final BlockEntityType<PlacerBlockEntity> PLACER_BLOCK_ENTITY;
  public static final ScreenHandlerType<PlacerScreenHandler> PLACER_SCREEN_HANDLER;

  public static final FastHopperBlock FAST_HOPPER;
  public static final BlockItem FAST_HOPPER_ITEM;
  public static final BlockEntityType<FastHopperBlockEntity> FAST_HOPPER_ENTITY;
  public static final ScreenHandlerType<HopperScreenHandler> FAST_HOPPER_SCREEN_HANDLER;

  static {
    BREAKER_BLOCK = Registry.register(Registry.BLOCK, BREAKER,
        new BreakerBlock(FabricBlockSettings.copyOf(Blocks.DISPENSER)));
    BREAKER_BLOCK_ITEM = Registry.register(Registry.ITEM, BREAKER,
        new BlockItem(BREAKER_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
    BREAKER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, BREAKER,
        BlockEntityType.Builder.create(BreakerBlockEntity::new, BREAKER_BLOCK).build(null));
    BREAKER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(BREAKER, BreakerScreenHandler::new);

    AUTOCRAFTER_BLOCK = Registry.register(Registry.BLOCK, AUTOCRAFTER,
        new AutoCrafterBlock(FabricBlockSettings.copyOf(Blocks.DISPENSER)));
    AUTOCRAFTER_BLOCK_ITEM = Registry.register(Registry.ITEM, AUTOCRAFTER,
        new BlockItem(AUTOCRAFTER_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
    AUTOCRAFTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, AUTOCRAFTER,
        BlockEntityType.Builder.create(AutoCrafterBlockEntity::new, AUTOCRAFTER_BLOCK).build(null));
    AUTOCRAFTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(AUTOCRAFTER, AutoCrafterScreenHandler::new);

    PLACER_BLOCK = Registry.register(Registry.BLOCK, PLACER,
        new PlacerBlock(FabricBlockSettings.copyOf(Blocks.DISPENSER)));
    PLACER_BLOCK_ITEM = Registry.register(Registry.ITEM, PLACER,
        new BlockItem(PLACER_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
    PLACER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, PLACER,
        BlockEntityType.Builder.create(PlacerBlockEntity::new, PLACER_BLOCK).build(null));
    PLACER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(PLACER, PlacerScreenHandler::new);

    FAST_HOPPER = Registry.register(Registry.BLOCK, FASTHOPPER,
        new FastHopperBlock(FabricBlockSettings.copyOf(Blocks.HOPPER)));
    FAST_HOPPER_ITEM = Registry.register(Registry.ITEM, FASTHOPPER,
        new BlockItem(FAST_HOPPER, new Item.Settings().group(ItemGroup.REDSTONE)));
    FAST_HOPPER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, FASTHOPPER,
        BlockEntityType.Builder.create(FastHopperBlockEntity::new, FAST_HOPPER).build(null));
    FAST_HOPPER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(FASTHOPPER, FastHopperScreenHandler::new);
  }

  @Override
  public void onInitialize() {

  }
}
