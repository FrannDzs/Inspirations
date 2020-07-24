package knightminer.inspirations.library.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import knightminer.inspirations.library.Util;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class BlockIngredient extends Ingredient {
	public static final ResourceLocation RECIPE_TYPE_ID = Util.getResource("blockstate");
	public final StatePropertiesPredicate predicate;

	protected BlockIngredient(StatePropertiesPredicate blockstateMatcher) {
		super(Stream.empty());
		this.predicate = blockstateMatcher;
	}

	protected abstract boolean matchesBlock(Block block);
	protected abstract Pair<String, JsonElement> matchJSON();

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public boolean hasNoMatchingItems() {
		return false;
	}

	/**
	 * Dummy implementation, should not be used on normal recipes.
	 */
	@Nonnull
	@Override
	public ItemStack[] getMatchingStacks() {
		return new ItemStack[0];
	}

	/**
	 * Dummy implementation, should not be used on normal recipes.
	 */
	@Override
	public boolean test(@Nullable ItemStack testItem) {
		if (testItem == null) {
			return false;
		}
		Block block = Block.getBlockFromItem(testItem.getItem());
		return block != Blocks.AIR && testBlock(block.getDefaultState());
	}

	/**
	 * Check if the given state matches this ingredient.
	 * @param state The state to match.
	 * @return If it matched.
	 */
	public boolean testBlock(BlockState state) {
		if (!matchesBlock(state.getBlock())) {
			return false;
		}
		return predicate.matches(state);
	}

	@Nonnull
	@Override
	public JsonElement serialize() {
		JsonObject result = new JsonObject();
		result.addProperty("type", RECIPE_TYPE_ID.toString());

		Pair<String, JsonElement> blockData = matchJSON();
		result.add(blockData.getFirst(), blockData.getSecond());

		result.add("properties", predicate.toJsonElement());
		return result;
	}

	@Nonnull
	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	public static IIngredientSerializer<BlockIngredient> SERIALIZER = new BlockIngredientSerialiser();

	private static class BlockIngredientSerialiser implements IIngredientSerializer<BlockIngredient> {
		@Nonnull
		@Override
		public BlockIngredient parse(@Nonnull JsonObject json) {
			JsonObject props = JSONUtils.getJsonObject(json, "properties", new JsonObject());
			StatePropertiesPredicate predicate = StatePropertiesPredicate.deserializeProperties(props);

			if (json.has("block") && json.has("tag")) {
				throw new JsonParseException("A Block Ingredient entry is either a tag or a block, not both");
			} else if (json.has("block")) {
				ResourceLocation blockName = new ResourceLocation(JSONUtils.getString(json, "block"));
				if (!ForgeRegistries.BLOCKS.containsKey(blockName)) {
					throw new JsonSyntaxException("Unknown block '" + blockName + "'");
				} else {
					return new DirectBlockIngredient(ForgeRegistries.BLOCKS.getValue(blockName), predicate);
				}
			} else if (json.has("tag")) {
				ResourceLocation tagName = new ResourceLocation(JSONUtils.getString(json, "tag"));
				Tag<Block> tag = BlockTags.getCollection().get(tagName);
				if (tag == null) {
					throw new JsonSyntaxException("Unknown block tag '" + tagName + "'");
				} else {
					return new TaggedBlockIngredient(tag, predicate);
				}
			} else {
				throw new JsonParseException("An Block Ingredient entry needs either a tag or a block");
			}
		}

		@Nonnull
		@Override
		public BlockIngredient parse(@Nonnull PacketBuffer buffer) {
			JsonObject predicateData = JSONUtils.fromJson(buffer.readString(32768));
			StatePropertiesPredicate predicate = StatePropertiesPredicate.deserializeProperties(predicateData.getAsJsonObject(""));
			boolean isTag = buffer.readBoolean();
			ResourceLocation loc = buffer.readResourceLocation();
			if (isTag) {
				return new TaggedBlockIngredient(loc, predicate);
			} else {
				// Direct block.
				return new DirectBlockIngredient(loc, predicate);
			}
		}

		@Override
		public void write(@Nonnull PacketBuffer buffer, @Nonnull BlockIngredient ingredient) {
			// This is ugly, but we'd otherwise need to mess with the internals to get out the data.
			JsonObject predicateData = new JsonObject();
			predicateData.add("", ingredient.predicate.toJsonElement());
			buffer.writeString(predicateData.toString());
			if (ingredient instanceof TaggedBlockIngredient) {
				buffer.writeBoolean(true);
				buffer.writeResourceLocation(((TaggedBlockIngredient) ingredient).tag.getId());
			} else if (ingredient instanceof DirectBlockIngredient) {
				buffer.writeBoolean(false);
				buffer.writeResourceLocation(((DirectBlockIngredient) ingredient).block.getRegistryName());
			} else {
				throw new IllegalArgumentException("Unknown BlockIngredient " + ingredient.getClass().getName());
			}
		}
	}


	public static class DirectBlockIngredient extends BlockIngredient {
		public final Block block;

		public DirectBlockIngredient(Block block, StatePropertiesPredicate predicate) {
			super(predicate);
			this.block = block;
		}

		public DirectBlockIngredient(ResourceLocation blockName, StatePropertiesPredicate predicate) {
			super(predicate);
			Block block = ForgeRegistries.BLOCKS.getValue(blockName);
			if (block == null) {
				throw new JsonSyntaxException("Unknown block '" + blockName + "'");
			}
			this.block = block;
		}

		@Override
		protected boolean matchesBlock(Block block) {
			return block.delegate.get() == this.block;
		}

		@Override
		protected Pair<String, JsonElement> matchJSON() {
			return Pair.of("block", new JsonPrimitive(block.getRegistryName().toString()));
		}
	}

	public static class TaggedBlockIngredient extends BlockIngredient {
		public final Tag<Block> tag;

		public TaggedBlockIngredient(Tag<Block> tag, StatePropertiesPredicate predicate) {
			super(predicate);
			this.tag = tag;
		}

		public TaggedBlockIngredient(ResourceLocation tagName, StatePropertiesPredicate predicate) {
			super(predicate);
			this.tag = BlockTags.getCollection().get(tagName);
			if (this.tag == null) {
				throw new JsonSyntaxException("Unknown block tag '" + tagName + "'");
			}
		}

		@Override
		protected boolean matchesBlock(Block block) {
			return tag.contains(block);
		}

		@Override
		protected Pair<String, JsonElement> matchJSON() {
			return Pair.of("tag", new JsonPrimitive(tag.getId().toString()));
		}
	}
}
