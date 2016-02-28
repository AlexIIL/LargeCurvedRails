package alexiil.mods.traincraft.block;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.track.Curve;

public class BlockTrackCurvedFull extends BlockTrackSeperated {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public final Curve curve;

    public BlockTrackCurvedFull(Curve curve, double radius) {
        super(PROPERTY_FACING);
        this.curve = curve;
        curve.fullBlock = this;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, BlockPos pos) {
        setBlockBounds(0, 0, 0, 1, TRACK_HEIGHT, 1);
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
