package alexiil.mc.mod.traincraft.track;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.track.path.TrackPath2DArc;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathTriComposite;
import alexiil.mc.mod.traincraft.block.BlockAbstractTrack;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedFull;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedHalf;

public enum Curve {
    RADIUS_3("3", 3.5, 1, 2),
    RADIUS_5("5", 5.5, 1.5, 3.5),
    RADIUS_7("7", 7.5, 2, 5),
    RADIUS_9("9", 9.5, 2.5, 6.5),
    RADIUS_11("11", 11.5, 3, 8),;

    public final String halfIdentifier, fullIdentifier;

    public final HalfFactory halfFactory;
    public final FullFactory fullFactory;

    public final TrackBehaviourCurvedHalfNative halfNative;
    // public final TrackBehaviourCurvedFullNative fullNative;

    public BlockTrackCurvedHalf halfBlock;
    public BlockTrackCurvedFull fullBlock;

    private final String radiusReadable;

    private Curve(String radiusReadable, double radius, double width, double length) {
        this.radiusReadable = radiusReadable;
        halfIdentifier = "traincraft:curve_half_" + radiusReadable + "::";
        fullIdentifier = "traincraft:curve_full_" + radiusReadable + "::";

        halfFactory = new HalfFactory(width, length);
        fullFactory = new FullFactory(radius);

        halfNative = new TrackBehaviourCurvedHalfNative(this);
        // fullNative = new TrackBehaviourCurvedFullNative(/* this */);
    }

    private static Vec3d perp(Vec3d vec) {
        return MathUtil.perp(vec);
    }

    private static Vec3d scale(Vec3d vec, double scale) {
        return MathUtil.scale(vec, scale);
    }

    public class HalfFactory implements StatefulFactory {
        private final String identifier = "traincraft:curved_half_" + parent().radiusReadable;
        private final double width, length;
        private final Table<EnumFacing, Boolean, TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight>> trackPaths;
        private final Table<EnumFacing, Boolean, Set<BlockPos>> slaveOffsets = HashBasedTable.create();

        public HalfFactory(double width, double length) {
            this.width = width;
            this.length = length;
            trackPaths = HashBasedTable.create();

            int[][][] angles = {//
                { { 360, 315 }, { 180, 225 } },// North
                { { 0, 45 }, { 180, 135 } },// South
                { { 90, 135 }, { 270, 225 } },// West
                { { 90, 45 }, { 270, 315 } },// East
            };

            for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {

                Axis axis = horizontal.getAxis();
                int thing = (int) (horizontal.getAxisDirection().getOffset() * -0.5 + 0.5);
                Vec3d startPoint = new Vec3d(axis == Axis.Z ? 0.5 : thing, BlockAbstractTrack.TRACK_HEIGHT, axis == Axis.X ? 0.5 : thing);

                for (boolean positive : new boolean[] { false, true }) {
                    Vec3d A = startPoint;
                    Vec3d aDir = new Vec3d(horizontal.getFrontOffsetX(), 0, horizontal.getFrontOffsetZ());

                    double offset = width * (positive ? 1 : -1);
                    Vec3d B = A.addVector(//
                            axis == Axis.X ? horizontal.getFrontOffsetX() * length : offset,//
                            0,// Never change the Y
                            axis == Axis.Z ? horizontal.getFrontOffsetZ() * length : offset //
                    );

                    double pOffset = positive ? 1 : -1;
                    Vec3d bDir = aDir.addVector(//
                            axis == Axis.X ? 0 : pOffset,//
                            0,//
                            axis == Axis.Z ? 0 : pOffset //
                    );

                    /* Calculate a path from A to B, where A is the start point heading in ^A, and B is the end point
                     * heading in ^B.
                     * 
                     * Calculate C as the join of the lines ^A from A and ^B from B.
                     * 
                     * Calculate distance d from the minumum of (AC, BC)
                     * 
                     * Calculate D (on AC) as d distance from C towards A
                     * 
                     * Calculate E (on BC) as d distance from C towards B
                     * 
                     * Calculate F as the join of the lines perpendicular from ^A from D and perpendicular from ^B from
                     * E
                     * 
                     * Calculate r as the distance FD */
                    Vec3d C = MathUtil.findCommonPoint(A, aDir, B, bDir);

                    double ac = A.distanceTo(C);
                    double bc = B.distanceTo(C);
                    double d = Math.min(ac, bc);

                    Vec3d D = d == ac ? A : C.add(scale(A.subtract(C), d / ac));

                    Vec3d E = d == bc ? B : C.add(scale(B.subtract(C), d / bc));

                    Vec3d F = MathUtil.findCommonPoint(D, perp(aDir), E, perp(bDir));

                    double r = F.distanceTo(D);

                    int angD = angles[horizontal.getIndex() - 2][positive ? 1 : 0][0];
                    int angE = angles[horizontal.getIndex() - 2][positive ? 1 : 0][1];

                    TrackPathStraight pathAD = new TrackPathStraight(A, D);
                    TrackPath2DArc pathDE = TrackPath2DArc.createDegrees(F, r, angD, angE);
                    TrackPathStraight pathEB = new TrackPathStraight(E, B);

                    TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> composite;
                    composite = new TrackPathTriComposite<>(pathAD, pathDE, pathEB);
                    trackPaths.put(horizontal, positive, composite);

                    slaveOffsets.put(horizontal, positive, TrackBehaviourStateful.createSlaveOffsets(composite));
                }
            }
        }

        @Override
        public String identifier() {
            return identifier;
        }

        @Override
        public TrackBehaviourCurvedHalfState create(World world, BlockPos pos) {
            return new TrackBehaviourCurvedHalfState(world, pos, this);
        }

        public Curve parent() {
            return Curve.this;
        }

        public FullFactory other() {
            return fullFactory;
        }

        public TrackPathTriComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> getPath(EnumFacing face, boolean positive) {
            return trackPaths.get(face, positive);
        }

        public Set<BlockPos> getSlaves(EnumFacing face, boolean positive) {
            return slaveOffsets.get(face, positive);
        }
    }

    public class FullFactory implements StatefulFactory {
        private final String identifier = "traincraft:curved_full_" + parent().radiusReadable;
        private final double radius;
        private final Map<EnumFacing, TrackPath2DArc> trackPaths = new EnumMap<>(EnumFacing.class);
        private final Map<EnumFacing, Set<BlockPos>> slaveOffsets = new EnumMap<>(EnumFacing.class);

        public FullFactory(double radius) {
            this.radius = radius;

            int[][] angles = {//
                { 180, 270 },// North
                { 0, 90 },// South
                { 90, 180 },// West
                { 270, 360 },// East
            };

            Vec3d[] centers = {//
                new Vec3d(0.5 + radius, 0.125, 1),// North
                new Vec3d(0.5 - radius, 0.125, 0),// South
                new Vec3d(1, 0.125, 0.5 - radius),// West
                new Vec3d(0, 0.125, 0.5 + radius),// East
            };

            for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
                int[] ang = angles[horizontal.getIndex() - 2];
                Vec3d center = centers[horizontal.getIndex() - 2];

                TrackPath2DArc path = TrackPath2DArc.createDegrees(center, radius, ang[0], ang[1]);
                trackPaths.put(horizontal, path);
                slaveOffsets.put(horizontal, TrackBehaviourNative.createSlaveOffsets(path));
            }
        }

        @Override
        public String identifier() {
            return identifier;
        }

        @Override
        public TrackBehaviourStateful create(World world, BlockPos pos) {
            // TODO Auto-generated method stub
            return null;
        }

        public Curve parent() {
            return Curve.this;
        }

        public HalfFactory other() {
            return halfFactory;
        }

        public ITrackPath getPath(EnumFacing face) {
            return trackPaths.get(face);
        }

        public Set<BlockPos> getSLaveOffsets(EnumFacing face) {
            return slaveOffsets.get(face);
        }
    }
}
