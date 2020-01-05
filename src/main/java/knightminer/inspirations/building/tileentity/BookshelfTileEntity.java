package knightminer.inspirations.building.tileentity;

import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.BookshelfBlock;
import knightminer.inspirations.building.inventory.BookshelfContainer;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.common.network.InventorySlotSyncPacket;
import knightminer.inspirations.library.InspirationsRegistry;
import knightminer.inspirations.library.client.ClientUtil;
import knightminer.inspirations.library.util.TextureBlockUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.mantle.tileentity.InventoryTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BookshelfTileEntity extends InventoryTileEntity {

	public static final ModelProperty<String> TEXTURE = TextureBlockUtil.TEXTURE_PROP;
	public static final ModelProperty<Integer> BOOKS = new ModelProperty<>();

	/** Cached enchantment bonus, so we are not constantly digging the inventory */
	private float enchantBonus;

	public static ITextComponent TITLE = new TranslationTextComponent("gui.inspirations.bookshelf.name");

	public BookshelfTileEntity() {
		super(InspirationsBuilding.tileBookshelf, TITLE, 28, 1);
		enchantBonus = Float.NaN;
	}

	public int getMaxBookCount() {
		return BookshelfBlock.getBookCount(world != null ? world.getBlockState(pos) : null);
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot > getMaxBookCount()) {
			return ItemStack.EMPTY;
		}
		return super.getStackInSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, @Nonnull ItemStack itemstack) {
		if (slot > getMaxBookCount()) {
			return;
		}
		ItemStack oldStack = this.getStackInSlot(slot);

		// we sync slot changes to all clients around
		if(getWorld() != null && getWorld() instanceof ServerWorld && !getWorld().isRemote && !ItemStack.areItemStacksEqual(itemstack, getStackInSlot(slot))) {
			InspirationsNetwork.sendToClients((ServerWorld) getWorld(), this.pos, new InventorySlotSyncPacket(itemstack, slot, pos));
		}
		super.setInventorySlotContents(slot, itemstack);

		if(getWorld() != null) {
			// update for rendering
			if(getWorld().isRemote) {
				ModelDataManager.requestModelDataRefresh(this);
			}

			// if we have redstone books and either the old stack xor the new one is a book, update
			if(oldStack.getItem() == InspirationsBuilding.redstoneBook ^ itemstack.getItem() == InspirationsBuilding.redstoneBook) {
				world.updateComparatorOutputLevel(pos, this.getBlockState().getBlock());
			}
		}

		// clear bonus to recalculate it
		enchantBonus = Float.NaN;
	}

	/*
	 * Book logic
	 */

	public boolean interact(PlayerEntity player, Hand hand, int bookClicked) {
		// if it contains a book, take the book out
		if(isStackInSlot(bookClicked)) {
			if (!world.isRemote) {
				ItemHandlerHelper.giveItemToPlayer(player, getStackInSlot(bookClicked), player.inventory.currentItem);
				setInventorySlotContents(bookClicked, ItemStack.EMPTY);
			}
			return true;
		}

		// try adding book
		ItemStack stack = player.getHeldItem(hand);
		if(InspirationsRegistry.isBook(stack)) {
			if (!world.isRemote) {
				setInventorySlotContents(bookClicked, stack.split(1));
			}
			return true;
		}

		return false;
	}


	/*
	 * GUI
	 */

	@Nullable
	@Override
	public Container createMenu(int winId, @Nonnull PlayerInventory playerInv, @Nonnull PlayerEntity player) {
		boolean secHalf = BookshelfBlock.playerOnSecondary(world, pos, player);
		return new BookshelfContainer(winId, playerInv, secHalf, this);
	}

	/*
	 * Extra logic
	 */

	public int getComparatorPower() {
		for(int i = 0; i < 14; i++) {
			if(getStackInSlot(i).getItem() == InspirationsBuilding.redstoneBook) {
				// we do plus two so a book in slot 13 (last one) gives 15
				return i + 2;
			}
		}
		return 0;
	}

	public float getEnchantPower() {
		// if we have a cached value, use that
		if(!Float.isNaN(enchantBonus)) {
			return enchantBonus;
		}
		// simple sum of all books with the power of a full shelf
		float books = 0;
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(isStackInSlot(i)) {
				float power = InspirationsRegistry.getBookEnchantingPower(getStackInSlot(i));
				if (power >= 0) {
					books += power;
				}
			}
		}

		// divide by the inventory size.
		enchantBonus = books / getSizeInventory();
		return enchantBonus;
	}

	/*
	 * Rendering
	 */
	@Nonnull
	@Override
	public IModelData getModelData() {
		// pack books into integer
		int books = 0;
		for(int i = 0; i < getMaxBookCount(); i++) {
			if (isStackInSlot(i)) {
				books |= 1 << i;
			}
		}
		ModelDataMap.Builder data = new ModelDataMap.Builder().withInitial(BOOKS, books);
		// texture not loaded
		String texture = ClientUtil.getTexturePath(this);
		if(!texture.isEmpty()) {
			data = data.withInitial(TEXTURE, texture);
		}
		return data.build();
	}


	/*
	 * Networking
	 */

	@Nonnull
	@Override
	public CompoundNBT getUpdateTag() {
		// new tag instead of super since default implementation calls the super of writeToNBT
		return write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		// note that this sends all of the tile data. you should change this if you use additional tile data
		CompoundNBT tag = getTileData().copy();
		write(tag);
		// Tile entity type here is used for Vanilla only.
		return new SUpdateTileEntityPacket(this.getPos(), 0, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getNbtCompound();
		TextureBlockUtil.updateTextureBlock(this, tag);
		read(tag);
	}
}
