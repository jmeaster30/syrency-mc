package com.syrency.mc.server;

import com.syrency.mc.server.blockentities.*;
import com.syrency.mc.server.blocks.*;
import com.syrency.mc.server.screens.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SyrencyMod implements ModInitializer {
    public static final String NAMESPACE = "syrency";

    public static final Identifier BREAKER = Identifier.of(NAMESPACE, "breaker_block");
    public static final Identifier AUTOCRAFTER = Identifier.of(NAMESPACE, "autocrafter_block");
    public static final Identifier PLACER = Identifier.of(NAMESPACE, "placer_block");
    public static final Identifier FASTHOPPER = Identifier.of(NAMESPACE, "fast_hopper");
    public static final Identifier HUPPER = Identifier.of(NAMESPACE, "hupper");
    public static final Identifier GROWTHDETECTOR = Identifier.of(NAMESPACE, "growth_detector");
    public static final Identifier SPLITTER = Identifier.of(NAMESPACE, "splitter_block");

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
    public static final ScreenHandlerType<FastHopperScreenHandler> FAST_HOPPER_SCREEN_HANDLER;

    public static final HupperBlock HUPPER_BLOCK;
    public static final BlockItem HUPPER_ITEM;
    public static final BlockEntityType<HupperBlockEntity> HUPPER_ENTITY;
    public static final ScreenHandlerType<HupperScreenHandler> HUPPER_SCREEN_HANDLER;

    public static final SplitterBlock SPLITTER_BLOCK;
    public static final BlockItem SPLITTER_ITEM;
    public static final BlockEntityType<SplitterBlockEntity> SPLITTER_ENTITY;
    public static final ScreenHandlerType<SplitterScreenHandler> SPLITTER_SCREEN_HANDLER;

    public static final GrowthDetectorBlock GROWTH_DETECTOR;
    public static final BlockItem GROWTH_DETECTOR_ITEM;
    public static final BlockEntityType<GrowthDetectorBlockEntity> GROWTH_DETECTOR_BLOCK_ENTITY;

    static {
        BREAKER_BLOCK = Registry.register(Registries.BLOCK, BREAKER, new BreakerBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER)));
        BREAKER_BLOCK_ITEM = Registry.register(Registries.ITEM, BREAKER, new BlockItem(BREAKER_BLOCK, new Item.Settings()));
        BREAKER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, BREAKER, BlockEntityType.Builder.create(BreakerBlockEntity::new, BREAKER_BLOCK).build(null));
        BREAKER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, BREAKER, new ScreenHandlerType<>(BreakerScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        AUTOCRAFTER_BLOCK = Registry.register(Registries.BLOCK, AUTOCRAFTER, new AutoCrafterBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER)));
        AUTOCRAFTER_BLOCK_ITEM = Registry.register(Registries.ITEM, AUTOCRAFTER, new BlockItem(AUTOCRAFTER_BLOCK, new Item.Settings()));
        AUTOCRAFTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, AUTOCRAFTER, BlockEntityType.Builder.create(AutoCrafterBlockEntity::new, AUTOCRAFTER_BLOCK).build(null));
        AUTOCRAFTER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, AUTOCRAFTER, new ScreenHandlerType<>(AutoCrafterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        PLACER_BLOCK = Registry.register(Registries.BLOCK, PLACER, new PlacerBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER)));
        PLACER_BLOCK_ITEM = Registry.register(Registries.ITEM, PLACER, new BlockItem(PLACER_BLOCK, new Item.Settings()));
        PLACER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, PLACER, BlockEntityType.Builder.create(PlacerBlockEntity::new, PLACER_BLOCK).build(null));
        PLACER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, PLACER, new ScreenHandlerType<>(PlacerScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        FAST_HOPPER = Registry.register(Registries.BLOCK, FASTHOPPER, new FastHopperBlock(AbstractBlock.Settings.copy(Blocks.HOPPER)));
        FAST_HOPPER_ITEM = Registry.register(Registries.ITEM, FASTHOPPER, new BlockItem(FAST_HOPPER, new Item.Settings()));
        FAST_HOPPER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, FASTHOPPER, BlockEntityType.Builder.create(FastHopperBlockEntity::new, FAST_HOPPER).build(null));
        FAST_HOPPER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, FASTHOPPER, new ScreenHandlerType<>(FastHopperScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        HUPPER_BLOCK = Registry.register(Registries.BLOCK, HUPPER, new HupperBlock(AbstractBlock.Settings.copy(Blocks.HOPPER)));
        HUPPER_ITEM = Registry.register(Registries.ITEM, HUPPER, new BlockItem(HUPPER_BLOCK, new Item.Settings()));
        HUPPER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, HUPPER, BlockEntityType.Builder.create(HupperBlockEntity::new, HUPPER_BLOCK).build(null));
        HUPPER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, HUPPER, new ScreenHandlerType<>(HupperScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        SPLITTER_BLOCK = Registry.register(Registries.BLOCK, SPLITTER, new SplitterBlock(AbstractBlock.Settings.copy(Blocks.HOPPER)));
        SPLITTER_ITEM = Registry.register(Registries.ITEM, SPLITTER, new BlockItem(SPLITTER_BLOCK, new Item.Settings()));
        SPLITTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, SPLITTER, BlockEntityType.Builder.create(SplitterBlockEntity::new, SPLITTER_BLOCK).build(null));
        SPLITTER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, SPLITTER, new ScreenHandlerType<>(SplitterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        GROWTH_DETECTOR = Registry.register(Registries.BLOCK, GROWTHDETECTOR, new GrowthDetectorBlock(AbstractBlock.Settings.copy(Blocks.OBSERVER)));
        GROWTH_DETECTOR_ITEM = Registry.register(Registries.ITEM, GROWTHDETECTOR, new BlockItem(GROWTH_DETECTOR, new Item.Settings()));
        GROWTH_DETECTOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, GROWTHDETECTOR, BlockEntityType.Builder.create(GrowthDetectorBlockEntity::new, GROWTH_DETECTOR).build(null));
    }

    @Override
    public void onInitialize() {
        System.out.println("Syrency initialized :)");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(groupEntries -> {
            groupEntries.add(BREAKER_BLOCK_ITEM);
            groupEntries.add(AUTOCRAFTER_BLOCK_ITEM);
            groupEntries.add(PLACER_BLOCK_ITEM);
            groupEntries.add(FAST_HOPPER_ITEM);
            groupEntries.add(HUPPER_ITEM);
            groupEntries.add(SPLITTER_ITEM);
            groupEntries.add(GROWTH_DETECTOR_ITEM);
        });
    }
}
