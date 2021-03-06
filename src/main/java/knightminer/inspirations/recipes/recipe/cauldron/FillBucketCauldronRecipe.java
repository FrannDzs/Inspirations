package knightminer.inspirations.recipes.recipe.cauldron;

import knightminer.inspirations.library.recipe.RecipeSerializers;
import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
import knightminer.inspirations.library.recipe.cauldron.inventory.ICauldronInventory;
import knightminer.inspirations.library.recipe.cauldron.inventory.IModifyableCauldronInventory;
import knightminer.inspirations.library.recipe.cauldron.recipe.ICauldronRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Recipe that fills a bucket from cauldron contents. Supports any generic fluid handler item.
 */
public class FillBucketCauldronRecipe implements ICauldronRecipe {
  private final ResourceLocation id;
  public FillBucketCauldronRecipe(ResourceLocation id) {
    this.id = id;
  }

  /**
   * Attempts to fill a fluid handler with the given fluid
   * @param handler  Fluid handler
   * @param fluid    Fluid amount
   * @param action   Whether to simulate or execute the action
   * @return  True if successful, false if failed
   */
  private static boolean tryFill(IFluidHandlerItem handler, Fluid fluid, FluidAction action) {
    return handler.fill(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME), action) == FluidAttributes.BUCKET_VOLUME;
  }

  @Override
  public boolean matches(ICauldronInventory inv, World worldIn) {
    // must be full or no fill bucket
    if (inv.getLevel() < MAX) {
      return false;
    }

    // forge requires a stack size of 1 for empty containers. Means I have to make a copy for matches
    ItemStack stack;
    if (inv.getStack().getCount() != 1) {
      stack = inv.getStack().copy();
      stack.setCount(1);
    } else {
      stack = inv.getStack();
    }
    // must be a fluid
    return inv.getContents()
              .get(CauldronContentTypes.FLUID)
              // handler must be fillable with the given fluid and must take 1000mb
              .flatMap(fluid -> FluidUtil.getFluidHandler(stack).filter(handler -> tryFill(handler, fluid, FluidAction.SIMULATE)))
              .isPresent();
  }

  @Override
  public void handleRecipe(IModifyableCauldronInventory inv) {
    ItemStack stack = inv.splitStack(1);
    inv.getContents().get(CauldronContentTypes.FLUID).ifPresent(fluid -> FluidUtil.getFluidHandler(stack).ifPresent(handler -> {
      // if we successfully fill the handler, update the cauldron
      if (tryFill(handler, fluid, FluidAction.EXECUTE)) {
        inv.setLevel(0);
        inv.setOrGiveStack(handler.getContainer());

        // play sound
        SoundEvent sound = fluid.getAttributes().getFillSound();
        if (sound == null) {
          sound = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
        }
        inv.playSound(sound);
      }
    }));
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return RecipeSerializers.CAULDRON_FILL_BUCKET;
  }
}
