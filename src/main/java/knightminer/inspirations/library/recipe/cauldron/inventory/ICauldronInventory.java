package knightminer.inspirations.library.recipe.cauldron.inventory;

import net.minecraft.item.ItemStack;

/**
 * Inventory representing contents of the cauldron
 */
public interface ICauldronInventory extends ICauldronState {
  /**
   * Gets the stack held by the player interacting with the cauldron
   * @return  Held stack
   */
  ItemStack getStack();

  /**
   * If true, the cauldron is simple and does not support fluids other than water
   * @return  True if the cauldron is simple
   */
  boolean isSimple();
}
