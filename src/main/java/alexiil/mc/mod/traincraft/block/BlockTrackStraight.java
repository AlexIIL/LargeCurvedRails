package alexiil.mc.mod.traincraft.block;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.item.TCItems;
import alexiil.mc.mod.traincraft.track.TrackBehaviourStraightNative;

public class BlockTrackStraight extends BlockAbstractTrackSingle {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public BlockTrackStraight() {}

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TRACK_DIRECTION);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TRACK_DIRECTION).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(TRACK_DIRECTION, EnumDirection.fromMeta(meta));
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing entFacing = placer.getHorizontalFacing();
        if (entFacing.getAxis() == Axis.X) return getDefaultState().withProperty(TRACK_DIRECTION, EnumDirection.EAST_WEST);
        else return getDefaultState().withProperty(TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        EnumDirection dir = state.getValue(TRACK_DIRECTION);
        if (dir.from.getAxis() == dir.to.getAxis()) {
            return new ItemStack(TCItems.TRACK_STRAIGHT_AXIS.getItem());
        } else return new ItemStack(TCItems.TRACK_STRAIGHT_DIAG.getItem());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state) {
        return new BehaviourWrapper(TrackBehaviourStraightNative.INSTANCE, world, pos);
    }
}
