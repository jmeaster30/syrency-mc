package com.syrency.mc.server.screens;

import com.syrency.mc.server.SyrencyMod;
import com.syrency.mc.utilities.BlockedCraftingResultSlot;
import com.syrency.mc.utilities.NoInputSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Optional;

public class AutoCrafterScreenHandler extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {

    private final Inventory inventory; // full 37 slot inventory
    private final CraftingInventory input; // 9 slot crafting space
    private final CraftingResultInventory result; // 1 slot result

    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    private boolean doUpdate;

    public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(37), ScreenHandlerContext.EMPTY);
    }

    public AutoCrafterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
                                    ScreenHandlerContext context) {
        super(SyrencyMod.AUTOCRAFTER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 37);
        this.inventory = inventory;
        this.inventory.onOpen(playerInventory.player);

        this.context = context;
        this.player = playerInventory.player;

        this.input = new CraftingInventory(this, 3, 3);
        this.result = new CraftingResultInventory();

        //set recipe this feels a little janky but every time we set the stack the whole thing was being overwritten in updateResult
        //so we wait until we add the last block to do the update
        doUpdate = false;
        for (int i = 1; i < 10; i++) {
            if (i == 9)
                doUpdate = true;
            this.input.setStack(i - 1, this.inventory.getStack(i).copy());
        }

        // recipe result slot
        this.addSlot(new BlockedCraftingResultSlot(playerInventory.player, this.input, this.result, 0, 110, 35));

        //output slot
        this.addSlot(new NoInputSlot(this.inventory, 0, 142, 35));

        int m;
        int l;

        // craft table 0 to 8 (9 slots)
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                this.addSlot(new Slot(this.input, l + m * 3, 16 + l * 18, 18 + m * 18));
            }
        }

        // block inventory 10 to 36 (27)
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                // inventory, index, x, y
                this.addSlot(new Slot(this.inventory, 10 + l + m * 9, 8 + l * 18, 72 + m * 18));
            }
        }

        // The player inventory index 9 to index 35 (27)
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 140 + m * 18));
            }
        }
        // The player Hotbar index 0 to 8 (9)
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 198));
        }
    }

    //this is where the update happens and the item gets crafted
    protected static void updateResult(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory,
                                       CraftingResultInventory resultInventory, Inventory baseInventory) {
        if (!world.isClient) {
            MinecraftServer serverWorld = world.getServer();
            assert serverWorld != null;
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
            ItemStack itemStack = ItemStack.EMPTY;

            CraftingRecipeInput craftingRecipeInput = craftingInventory.createRecipeInput();
            Optional<RecipeEntry<CraftingRecipe>> optionalRecipeEntry = serverWorld.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingRecipeInput, world);
            if (optionalRecipeEntry.isPresent()) {
                RecipeEntry<CraftingRecipe> craftingRecipe = optionalRecipeEntry.get();
                if (resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
                    itemStack = craftingRecipe.value().craft(craftingRecipeInput, world.getRegistryManager());
                }
            }

            resultInventory.setStack(0, itemStack);
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, 0, itemStack));
        }

        //update base inventory
        //baseInventory.setStack(0, resultInventory.getStack(0).copy());
        for (int i = 0; i < 9; i++) {
            baseInventory.setStack(i + 1, craftingInventory.getStack(i).copy());
        }
        baseInventory.markDirty();
    }

    public void onContentChanged(Inventory inventory) {
        if (!doUpdate)
            return;
        this.context.run((world, blockPos) -> {
            updateResult(this.syncId, world, this.player, this.input, this.result, this.inventory);
        });
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot fromSlot = this.slots.get(slot);
        if (fromSlot.hasStack()) {
            ItemStack originalStack = fromSlot.getStack();
            newStack = originalStack.copy();
            if (slot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                fromSlot.setStack(ItemStack.EMPTY);
            } else {
                fromSlot.markDirty();
            }
        }

        return newStack;
    }

    public boolean canInsertIntoSlot(int slot) {
        return slot != this.getCraftingResultSlotIndex();
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    //do we want this????
    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(RecipeEntry recipe) {
        return recipe.value().matches(this.input.createRecipeInput(), this.player.getWorld());
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
        return 9;
    }
}
