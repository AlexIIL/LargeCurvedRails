package alexiil.mc.mod.traincraft.block;

import java.util.Set;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.track.Curve;

public class BlockTrackCurvedFull extends BlockTrackSeperated {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public final Curve curve;

    public BlockTrackCurvedFull(Curve curve) {
        this.curve = curve;
        curve.fullBlock = this;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROPERTY_FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROPERTY_FACING, EnumFacing.getHorizontal(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROPERTY_FACING).getHorizontalIndex();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state) {
        return null;// curve.fullNative;// FIXME! Needs to be fullNative
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(IBlockState state) {
        return curve.fullFactory.getSLaveOffsets(state.getValue(PROPERTY_FACING));
    }
}
