package knightminer.inspirations.recipes.block;

import knightminer.inspirations.recipes.InspirationsRecipes;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import javax.annotation.Nullable;
import java.util.Objects;

public class SmashingAnvilBlock extends AnvilBlock {

	public SmashingAnvilBlock(Block original) {
		super(Block.Properties.from(original));
		setRegistryName(Objects.requireNonNull(original.getRegistryName()));
	}

	// Replace this to handle our different blocks.
	@Nullable
	public static BlockState damage(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.ANVIL || block == InspirationsRecipes.fullAnvil) {
		return InspirationsRecipes.chippedAnvil.getDefaultState().with(FACING, state.get(FACING));
		} else if (block == Blocks.CHIPPED_ANVIL || block == InspirationsRecipes.chippedAnvil) {
			return InspirationsRecipes.damagedAnvil.getDefaultState().with(FACING, state.get(FACING));
		} else {
			return null;
		}
	}

}
