package net.fabricmc.mechanicsplus.screens;

import java.io.ObjectOutputStream.PutField;
import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.mechanicsplus.MechanicsPlusMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class AutoCrafterScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory> {

  private final Inventory inventory;
  private final CraftingInventory input;
  private final CraftingResultInventory result;
  private final ScreenHandlerContext context;
  private final PlayerEntity player;

  public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
    this(syncId, playerInventory, new SimpleInventory(37), ScreenHandlerContext.EMPTY);
  }

  public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
      ScreenHandlerContext context) {
    super(MechanicsPlusMod.AUTOCRAFTER_SCREEN_HANDLER, syncId);
    checkSize(inventory, 37);
    this.inventory = inventory;
    inventory.onOpen(playerInventory.player);

    this.context = context;
    this.player = playerInventory.player;

    this.input = new CraftingInventory(this, 3, 3);
    this.result = new CraftingResultInventory();

    //set recipe
    for (int i = 1; i < 10; i++)
      this.input.setStack(i - 1, this.inventory.getStack(i));

    //We shouldn't hide the output hole cause hiding info is bad
    //this.result.setStack(0, this.inventory.getStack(0));

    // output slot
    this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));

    int m;
    int l;

    // craft table
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 3; ++l) {
        this.addSlot(new Slot(this.input, l + m * 3, 30 + l * 18, 18 + m * 18));
      }
    }

    // block inentory
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 9; ++l) {
        // inventory, index, x, y
        this.addSlot(new Slot(inventory, 10 + l + m * 9, 8 + l * 18, 72 + m * 18));
      }
    }

    // The player inventory
    for (m = 0; m < 3; ++m) {
      for (l = 0; l < 9; ++l) {
        this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 140 + m * 18));
      }
    }
    // The player Hotbar
    for (m = 0; m < 9; ++m) {
      this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 198));
    }
  }

  //this is where the update happens and the item gets crafted
  protected static void updateResult(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory,
      CraftingResultInventory resultInventory) {
    if (!world.isClient) {
      ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
      ItemStack itemStack = ItemStack.EMPTY;
      Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING,
          craftingInventory, world);
      if (optional.isPresent()) {
        CraftingRecipe craftingRecipe = (CraftingRecipe) optional.get();
        if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
          itemStack = craftingRecipe.craft(craftingInventory);
        }
      }

      resultInventory.setStack(0, itemStack);
      serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, itemStack));
    }
  }

  public void onContentChanged(Inventory inventory) {
    this.context.run((world, blockPos) -> {
      updateResult(this.syncId, world, this.player, this.input, this.result);//we probably want to pass in the base inventory
    });
  }

  @Override
  public boolean canUse(PlayerEntity player) {
    return this.inventory.canPlayerUse(player);
  }

  // Shift + Player Inv Slot
  @Override
  public ItemStack transferSlot(PlayerEntity player, int invSlot) {
    ItemStack newStack = ItemStack.EMPTY;
    Slot slot = this.slots.get(invSlot);
    if (slot != null && slot.hasStack()) {
      ItemStack originalStack = slot.getStack();
      newStack = originalStack.copy();
      if (invSlot < this.inventory.size()) {
        if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
        return ItemStack.EMPTY;
      }

      if (originalStack.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }
    }

    return newStack;
  }

  public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
  }

  //do we want this????
  @Override
  public void clearCraftingSlots() {
    this.input.clear();
    this.result.clear();
  }

  @Environment(EnvType.CLIENT)
  public RecipeBookCategory getCategory() {
    return RecipeBookCategory.CRAFTING;
  }

  @Override
  public int getCraftingHeight() {
    return this.input.getHeight();
  }

  @Override
  public int getCraftingWidth() {
    return this.input.getWidth();
  }

  @Override
  public int getCraftingResultSlotIndex() {
    return 0;
  }

  @Environment(EnvType.CLIENT)
  public int getCraftingSlotCount() {
    return 10;
  }

  @Override
  public boolean matches(Recipe<? super CraftingInventory> recipe) {
    return recipe.matches(this.input, this.player.world);
  }

  @Override
  public void populateRecipeFinder(RecipeFinder finder) {
    this.input.provideRecipeInputs(finder);
  }
}
