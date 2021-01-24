package net.fabricmc.mechanicsplus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.mechanicsplus.blocks.BreakerBlock;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MechanicsPlusMod implements ModInitializer {

  public static final BreakerBlock BREAKER_BLOCK = new BreakerBlock(
      FabricBlockSettings.of(Material.METAL).hardness(1.5f));

  @Override
  public void onInitialize() {
    Registry.register(Registry.BLOCK, new Identifier("mechanicsplus", "breaker_block"), BREAKER_BLOCK);
    Registry.register(Registry.ITEM, new Identifier("mechanicsplus", "breaker_block"),
        new BlockItem(BREAKER_BLOCK, new Item.Settings().group(ItemGroup.REDSTONE)));
  }
}
