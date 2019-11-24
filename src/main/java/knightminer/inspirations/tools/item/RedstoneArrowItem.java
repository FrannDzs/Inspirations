package knightminer.inspirations.tools.item;

import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.IHidable;
import knightminer.inspirations.tools.entity.RedstoneArrow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class RedstoneArrowItem extends ArrowItem implements IHidable {

	public RedstoneArrowItem(Properties builder) {
		super(builder);
	}

	@Nonnull
	@Override
	public AbstractArrowEntity createArrow(@Nonnull World world, @Nonnull ItemStack stack, LivingEntity shooter) {
		return new RedstoneArrow(world, shooter);
	}

	@Override
	public boolean isInfinite(ItemStack stack, ItemStack bow, PlayerEntity player) {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return Config.enableRedstoneCharger.get();
	}

	@Override
	public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
		if(shouldAddtoItemGroup(group)) {
			super.fillItemGroup(group, items);
		}
	}
}
