package knightminer.inspirations.utility.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

public class TorchLeverWallBlock extends WallTorchBlock {
  private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

  public TorchLeverWallBlock(AbstractBlock.Properties props, IParticleData particles) {
    super(props, particles);
    setDefaultState(getDefaultState().with(POWERED, false));
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder) {
    builder.add(POWERED, FACING);
  }


  @Override
  public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
    Direction facing = state.get(FACING);
    double x = pos.getX() + 0.5D;
    double y = pos.getY() + 0.7D;
    double z = pos.getZ() + 0.5D;

    Direction opposite = facing.getOpposite();
    int offsetX = opposite.getXOffset();
    int offsetZ = opposite.getZOffset();
    if (state.get(POWERED)) {
      world.addParticle(ParticleTypes.SMOKE, x + 0.10D * offsetX, y + 0.08D, z + 0.10D * offsetZ, 0.0D, 0.0D, 0.0D);
      world.addParticle(ParticleTypes.FLAME, x + 0.10D * offsetX, y + 0.08D, z + 0.10D * offsetZ, 0.0D, 0.0D, 0.0D);
    } else {
      world.addParticle(ParticleTypes.SMOKE, x + 0.27D * offsetX, y + 0.22D, z + 0.27D * offsetZ, 0.0D, 0.0D, 0.0D);
      world.addParticle(ParticleTypes.FLAME, x + 0.27D * offsetX, y + 0.22D, z + 0.27D * offsetZ, 0.0D, 0.0D, 0.0D);
    }
  }

  /*
   * Powering
   */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
    if (world.isRemote) {
      return ActionResultType.SUCCESS;
    }

    // update state
    state = state.func_235896_a_(POWERED);
    world.setBlockState(pos, state, 3);
    // play sound
    float pitch = state.get(POWERED) ? 0.6F : 0.5F;
    world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, pitch);
    // notify update
    world.notifyNeighborsOfStateChange(pos, this);
    world.notifyNeighborsOfStateChange(pos.offset(state.get(FACING).getOpposite()), this);
    return ActionResultType.SUCCESS;
  }


  /**
   * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
   */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
    // if powered, send updates for power
    if (state.getBlock() != newState.getBlock() && !isMoving && state.get(POWERED)) {
      world.notifyNeighborsOfStateChange(pos, this);
      world.notifyNeighborsOfStateChange(pos.offset(state.get(FACING).getOpposite()), this);
    }
    super.onReplaced(state, world, pos, newState, isMoving);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
    return state.get(POWERED) ? 15 : 0;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
    if (!state.get(POWERED)) {
      return 0;
    }
    return state.get(FACING) == side ? 15 : 0;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean canProvidePower(BlockState state) {
    return true;
  }
}
