package alexiil.mc.mod.traincraft.block;

import java.util.Set;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathTriComposite;
import alexiil.mc.mod.traincraft.track.Curve;

public class BlockTrackCurvedHalf extends BlockTrackSeperated {
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public final Curve curve;

    public BlockTrackCurvedHalf(Curve curve, double width, double length) {
        super(PROPERTY_FACING, PROPERTY_DIRECTION);
        this.curve = curve;
        curve.halfBlock = this;
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
        return new BehaviourWrapper(curve.halfNative, world, pos);
    }

    @Deprecated
    public TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> path(boolean positive, EnumFacing mainDirection) {
        return curve.halfFactory.getPath(mainDirection, positive);
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(IBlockState state) {
        return curve.halfFactory.getSlaves(state.getValue(PROPERTY_FACING), state.getValue(PROPERTY_DIRECTION));
    }
}
