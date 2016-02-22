package alexiil.mods.traincraft.block;

import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.lib.MathUtil;
import alexiil.mods.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mods.traincraft.api.track.path.TrackPathStraight;
import alexiil.mods.traincraft.api.track.path.TrackPathTriComposite;
import alexiil.mods.traincraft.track.Curve;
import alexiil.mods.traincraft.track.TrackBehaviourCurvedHalfNative;

public class BlockTrackCurvedHalf extends BlockTrackSeperated {
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    private final Curve curve;

    // @Deprecated
    // private final Table<EnumFacing, Boolean, TrackPathTriComposite<TrackPathStraight, TrackPath2DArc,
    // TrackPathStraight>> trackPaths;
    private final Map<IBlockState, List<BlockPos>> slaveMap = new HashMap<>();

    public BlockTrackCurvedHalf(Curve curve, double width, double length) {
        super(PROPERTY_FACING, PROPERTY_DIRECTION);
        this.curve = curve;
        curve.halfBlock = this;
        // trackPaths = HashBasedTable.create();
        //
        // int[][][] angles = {//
        // { { 360, 315 }, { 180, 225 } },// North
        // { { 0, 45 }, { 180, 135 } },// South
        // { { 90, 135 }, { 270, 225 } },// West
        // { { 90, 45 }, { 270, 315 } },// East
        // };
        //
        // BlockPos creator = new BlockPos(0, 0, 0);
        // for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
        //
        // Axis axis = horizontal.getAxis();
        // int thing = (int) (horizontal.getAxisDirection().getOffset() * -0.5 + 0.5);
        // Vec3 startPoint = new Vec3(axis == Axis.Z ? 0.5 : thing, TRACK_HEIGHT, axis == Axis.X ? 0.5 : thing);
        //
        // for (boolean positive : new boolean[] { false, true }) {
        // Vec3 A = startPoint;
        // Vec3 aDir = new Vec3(horizontal.getFrontOffsetX(), 0, horizontal.getFrontOffsetZ());
        //
        // double offset = width * (positive ? 1 : -1);
        // Vec3 B = A.addVector(//
        // axis == Axis.X ? horizontal.getFrontOffsetX() * length : offset,//
        // 0,// Never change the Y
        // axis == Axis.Z ? horizontal.getFrontOffsetZ() * length : offset //
        // );
        // double pOffset = positive ? 1 : -1;
        // Vec3 bDir = aDir.addVector(//
        // axis == Axis.X ? 0 : pOffset,//
        // 0,//
        // axis == Axis.Z ? 0 : pOffset //
        // );
        //
        // /* Calculate a path from A to B, where A is the start point heading in ^A, and B is the end point
        // * heading in ^B.
        // *
        // * Calculate C as the join of the lines ^A from A and ^B from B.
        // *
        // * Calculate distance d from the minumum of (AC, BC)
        // *
        // * Calculate D (on AC) as d distance from C towards A
        // *
        // * Calculate E (on BC) as d distance from C towards B
        // *
        // * Calculate F as the join of the lines perpendicular from ^A from D and perpendicular from ^B from E
        // *
        // * Calculate r as the distance FD */
        // Vec3 C = MathUtil.findCommonPoint(A, aDir, B, bDir);
        //
        // double ac = A.distanceTo(C);
        // double bc = B.distanceTo(C);
        // double d = Math.min(ac, bc);
        //
        // Vec3 D = d == ac ? A : C.add(scale(A.subtract(C), d / ac));
        //
        // Vec3 E = d == bc ? B : C.add(scale(B.subtract(C), d / bc));
        //
        // Vec3 F = MathUtil.findCommonPoint(D, perp(aDir), E, perp(bDir));
        //
        // double r = F.distanceTo(D);
        //
        // int angD = angles[horizontal.getIndex() - 2][positive ? 1 : 0][0];
        // int angE = angles[horizontal.getIndex() - 2][positive ? 1 : 0][1];
        //
        // TrackPathStraight pathAD = new TrackPathStraight(A, D, creator);
        // TrackPath2DArc pathDE = TrackPath2DArc.createDegrees(creator, F, r, angD, angE);
        // TrackPathStraight pathEB = new TrackPathStraight(E, B, creator);
        //
        // TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> composite;
        // composite = new TrackPathTriComposite<>(creator, pathAD, pathDE, pathEB);
        // trackPaths.put(horizontal, positive, composite);
        // }
        // }

        for (IBlockState state : stateToInt.keySet()) {
            TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> composite;
            composite = path(state.getValue(PROPERTY_DIRECTION), state.getValue(PROPERTY_FACING));
            Set<BlockPos> slaves = new HashSet<>();
            // Calculate slaves
            for (int i = 0; i < composite.length() * 5; i++) {
                double offset = i;
                offset += 0.5;
                offset /= composite.length();
                Vec3 pos = composite.interpolate(offset);
                Vec3 dir = composite.direction(offset);
                dir = MathUtil.cross(dir, new Vec3(0, 1, 0)).normalize();

                slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, 0.2))));
                slaves.add(new BlockPos(pos.add(MathUtil.scale(dir, -0.2))));
            }
            slaves.remove(new BlockPos(composite.end().add(MathUtil.scale(composite.direction(1), 0.1))));

            slaveMap.put(state, ImmutableList.copyOf(slaves));
        }
    }

    // private static Vec3 perp(Vec3 vec) {
    // return new Vec3(-vec.zCoord, vec.yCoord, vec.xCoord);
    // }
    //
    // private static Vec3 scale(Vec3 vec, double scale) {
    // return new Vec3(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    // }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        setBlockBounds(0, 0, 0, 1, TRACK_HEIGHT, 1);
    }

    @Override
    protected TrackBehaviourCurvedHalfNative singleBehaviour(IBlockAccess access, BlockPos pos, IBlockState state) {
        return curve.halfNative;
    }

    @Deprecated
    public TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> path(boolean positive, EnumFacing mainDirection) {
        return curve.halfFactory.getPath(mainDirection, positive);
    }

    @Override
    public boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        return slaveMap.get(masterState).contains(slavePos.subtract(masterPos));
    }

    @Override
    public List<BlockPos> getSlaveOffsets(IBlockState state) {
        return slaveMap.get(state);
    }
}
