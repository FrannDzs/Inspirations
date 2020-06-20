package knightminer.inspirations.recipes.datagen;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.data.PulseLoadedCondition;
import knightminer.inspirations.common.datagen.AnvilRecipeBuilder;
import knightminer.inspirations.common.datagen.CondRecipe;
import knightminer.inspirations.library.Util;
import knightminer.inspirations.library.recipe.ShapelessNoContainerRecipe;
import knightminer.inspirations.recipes.InspirationsRecipes;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;

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

		AnvilRecipeBuilder.places(Blocks.COBBLESTONE).addIngredient(Blocks.STONE).buildVanilla(consumer, "cobble_from_stone_anvil_smashing");
		AnvilRecipeBuilder.places(Blocks.COBBLESTONE).addIngredient(Blocks.STONE_BRICKS).buildVanilla(consumer, "cobble_from_bricks_anvil_smashing");
		AnvilRecipeBuilder.places(Blocks.COBBLESTONE).addIngredient(Blocks.SMOOTH_STONE).buildVanilla(consumer, "cobble_from_smooth_stone_anvil_smashing");
		AnvilRecipeBuilder.places(Blocks.MOSSY_COBBLESTONE).addIngredient(Blocks.MOSSY_STONE_BRICKS).build(consumer);
		AnvilRecipeBuilder.places(Blocks.PRISMARINE).addIngredient(Blocks.PRISMARINE_BRICKS).build(consumer);
		AnvilRecipeBuilder.places(Blocks.END_STONE).addIngredient(Blocks.END_STONE_BRICKS).build(consumer);
		AnvilRecipeBuilder.places(Blocks.GRAVEL).addIngredient(Blocks.COBBLESTONE).build(consumer);
		AnvilRecipeBuilder.places(Blocks.ANDESITE).addIngredient(Blocks.POLISHED_ANDESITE).build(consumer);
		AnvilRecipeBuilder.places(Blocks.GRANITE).addIngredient(Blocks.POLISHED_GRANITE).build(consumer);
		AnvilRecipeBuilder.places(Blocks.DIORITE).addIngredient(Blocks.POLISHED_DIORITE).build(consumer);

		AnvilRecipeBuilder.places(Blocks.SAND).addIngredient(Blocks.SANDSTONE).build(consumer);
		AnvilRecipeBuilder.places(Blocks.RED_SAND).addIngredient(Blocks.RED_SANDSTONE).build(consumer);

		AnvilRecipeBuilder.smashes().addIngredient(BlockTags.ICE).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(BlockTags.LEAVES).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.BROWN_MUSHROOM_BLOCK).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.RED_MUSHROOM_BLOCK).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.PUMPKIN).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.CARVED_PUMPKIN).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.JACK_O_LANTERN).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.MELON).build(consumer);

		AnvilRecipeBuilder.smashes().addIngredient(Tags.Blocks.GLASS).buildVanilla(consumer, "glass_tag");
		AnvilRecipeBuilder.smashes().addIngredient(Tags.Blocks.GLASS_PANES).buildVanilla(consumer, "glass_panes_tag");

		// Smash all silverfish blocks.
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_CHISELED_STONE_BRICKS).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_COBBLESTONE).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_CRACKED_STONE_BRICKS).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_MOSSY_STONE_BRICKS).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_STONE).build(consumer);
		AnvilRecipeBuilder.smashes().addIngredient(Blocks.INFESTED_STONE_BRICKS).build(consumer);

		// Smash concrete into concrete powder.
		for(DyeColor dye: DyeColor.values()) {
			AnvilRecipeBuilder
					.places(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(dye.getName() + "_concrete_powder")))
					.addIngredient(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(dye.getName() + "_concrete")))
					.build(consumer);
		}

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
