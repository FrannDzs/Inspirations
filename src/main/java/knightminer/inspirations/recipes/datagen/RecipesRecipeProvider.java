package knightminer.inspirations.recipes.datagen;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.data.PulseLoadedCondition;
import knightminer.inspirations.common.datagen.CondRecipe;
import knightminer.inspirations.library.Util;
import knightminer.inspirations.library.recipe.ShapelessNoContainerRecipe;
import knightminer.inspirations.recipes.InspirationsRecipes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Items;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class RecipesRecipeProvider extends RecipeProvider implements IConditionBuilder {
	public final ICondition RECIPES = new PulseLoadedCondition(InspirationsRecipes.pulseID);

	// Prevent needing to pass this into every method.
	private Consumer<IFinishedRecipe> consumer = null;
	// Verify that we don't double-down on recipe registry names.
	private final Set<String> bottle_suffixes = new HashSet<>();

	public RecipesRecipeProvider(DataGenerator gen) {
		super(gen);
	}

	@Nonnull
	@Override
	public String getName() {
		return "Inspirations Recipes - Recipe Module";
	}

	@Override
	protected void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
		this.consumer = consumer;
		if(!Inspirations.pulseManager.isPulseLoaded(InspirationsRecipes.pulseID)) {
			return;
		}

		// Allow using less bottles, wasting part of the item.
		for(int i=1; i<=3; i++) {
			CondRecipe.shapeless(InspirationsRecipes.lingeringBottle, i)
					.addCondition(RECIPES)
					.addCondition(ConfigEnabledCondition.CAULDRON_POTIONS)
					.addCriterion("has_cauldron", hasItem(Items.CAULDRON))
					.addIngredient(Items.DRAGON_BREATH)
					.addIngredient(Items.GLASS_BOTTLE, i)
					.build(consumer, String.format("lingering_from_%s_bottle%s", i, i != 1 ? "s" : ""));

			CondRecipe.shapeless(InspirationsRecipes.splashBottle, i)
					.addCondition(RECIPES)
					.addCondition(ConfigEnabledCondition.CAULDRON_POTIONS)
					.addCriterion("has_cauldron", hasItem(Items.CAULDRON))
					.addIngredient(Items.GUNPOWDER)
					.addIngredient(Items.GLASS_BOTTLE, i)
					.build(consumer, String.format("splash_from_%s_bottle%s", i, i != 1 ? "s" : ""));
		}

		// First dye produced by the remainder.
		bottleMixExtra(DyeColor.BROWN, DyeColor.GREEN, DyeColor.MAGENTA, DyeColor.ORANGE);
		bottleMixExtra(DyeColor.BROWN, DyeColor.ORANGE, DyeColor.BLUE);
		bottleMixVanilla(DyeColor.BROWN, DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW);
		bottleMixExtra(DyeColor.BROWN, DyeColor.RED, DyeColor.GREEN);
		bottleMixExtra(DyeColor.BROWN, DyeColor.YELLOW, DyeColor.PURPLE);

		bottleMixVanilla(DyeColor.LIGHT_GRAY, DyeColor.BLACK, DyeColor.WHITE, DyeColor.WHITE);
		bottleMixVanilla(DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.WHITE);

		bottleMixVanilla(DyeColor.MAGENTA, DyeColor.BLUE, DyeColor.RED, DyeColor.PINK);
		bottleMixVanilla(DyeColor.MAGENTA, DyeColor.BLUE, DyeColor.RED, DyeColor.RED, DyeColor.WHITE);
		bottleMixVanilla(DyeColor.MAGENTA, DyeColor.PINK, DyeColor.PURPLE);

		bottleMixVanilla(DyeColor.CYAN, DyeColor.BLUE, DyeColor.GREEN);
		bottleMixVanilla(DyeColor.GRAY, DyeColor.BLACK, DyeColor.WHITE);
		bottleMixExtra(DyeColor.GREEN, DyeColor.YELLOW, DyeColor.BLUE);
		bottleMixVanilla(DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.WHITE);
		bottleMixVanilla(DyeColor.LIME, DyeColor.GREEN, DyeColor.WHITE);
		bottleMixVanilla(DyeColor.ORANGE, DyeColor.RED, DyeColor.YELLOW);
		bottleMixVanilla(DyeColor.PINK, DyeColor.RED, DyeColor.WHITE);
		bottleMixVanilla(DyeColor.GREEN, DyeColor.BLUE, DyeColor.RED);
	}

	private void bottleMixExtra(DyeColor result, DyeColor... inputs) {
		bottleMix(ConfigEnabledCondition.EXTRA_DYED_BOTTLE_RECIPES, result, inputs);
	}

	private void bottleMixVanilla(DyeColor result, DyeColor... inputs) {
		bottleMix(ConfigEnabledCondition.PATCH_VANILLA_DYE_RECIPES, result, inputs);
	}
	/**
	 * Create a recipe to combine the given bottles into colored ones.
	 */
	private void bottleMix(ICondition conf, DyeColor result, DyeColor[] inputs) {
		StringBuilder sb = new StringBuilder("dyed_bottle/");
		sb.append(result.getName());
		sb.append('_');
		for(DyeColor color : inputs) {
			sb.append(color.getName().charAt(0));
		}
		String suffix = sb.toString();
		if (bottle_suffixes.contains(suffix)) {
			throw new RuntimeException(String.format(
					"Duplicate bottle suffix for inputs: %s",
					Arrays.stream(inputs).map(DyeColor::getName).collect(Collectors.joining(", "))
			));
		}
		bottle_suffixes.add(suffix);

		ShapelessRecipeBuilder builder = CondRecipe.shapeless(InspirationsRecipes.simpleDyedWaterBottle.get(result))
				.addCondition(RECIPES)
				.addCondition(conf)
				.custom(ShapelessNoContainerRecipe.SERIALIZER)
				.setGroup(Util.resource("dyed_bottle"))
				.addCriterion("has_bottle", hasItem(Items.GLASS_BOTTLE));

		for (DyeColor color: inputs) {
			builder.addIngredient(InspirationsRecipes.simpleDyedWaterBottle.get(color));
		}
		builder.build(consumer, suffix);
	}
}
