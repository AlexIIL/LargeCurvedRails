package alexiil.mods.traincraft.block;

import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.lib.MathUtil;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mods.traincraft.track.Curve;

public class BlockTrackCurvedFull extends BlockTrackSeperated {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    private final Curve curve;
    @Deprecated
    private final Map<EnumFacing, TrackPath2DArc> trackPaths = new HashMap<>();
    private final Map<IBlockState, List<BlockPos>> slaveMap = new HashMap<>();

    public BlockTrackCurvedFull(Curve curve, double radius) {
        super(PROPERTY_FACING);
        this.curve = curve;
        curve.fullBlock = this;
        int[][] angles = {//
            { 180, 270 },// North
            { 0, 90 },// South
            { 90, 180 },// West
            { 270, 360 },// East
        };

        Vec3[] centers = {//
            new Vec3(0.5 + radius, 0.125, 1),// North
            new Vec3(0.5 - radius, 0.125, 0),// South
            new Vec3(1, 0.125, 0.5 - radius),// West
            new Vec3(0, 0.125, 0.5 + radius),// East
        };

        BlockPos creator = new BlockPos(0, 0, 0);
        for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
            int[] ang = angles[horizontal.getIndex() - 2];
            Vec3 center = centers[horizontal.getIndex() - 2];

            TrackPath2DArc path = TrackPath2DArc.createDegrees(creator, center, radius, ang[0], ang[1]);
            trackPaths.put(horizontal, path);
        }

        for (IBlockState state : stateToInt.keySet()) {
            TrackPath2DArc path = path(state.getValue(PROPERTY_FACING));
            Set<BlockPos> slaves = new HashSet<>();
            // Calculate slaves
            for (int i = 0; i < path.length() * 5; i++) {
                double offset = i;
                offset += 0.5;
                offset /= path.length();
                Vec3 pos = path.interpolate(offset);
                Vec3 dir = path.direction(offset);
                dir = MathUtil.cross(dir, new Vec3(0, 1, 0)).normalize();

                slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, 0.2))));
                slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, -0.2))));
            }
            slaves.remove(new BlockPos(path.end().add(MathUtil.scale(path.direction(1), 0.1))));

            slaveMap.put(state, ImmutableList.copyOf(slaves));
        }
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

    @Deprecated
    public TrackPath2DArc path(EnumFacing mainDirection) {
        return trackPaths.get(mainDirection);
    }

    @Override
    public List<BlockPos> getSlaveOffsets(IBlockState state) {
        return slaveMap.get(state);
    }
}
