package alexiil.mods.traincraft.block;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.api.*;

public class BlockTrackCurved extends BlockTrackSeperated {
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    // private final Table<EnumFacing, Boolean, TrackPathComposite<TrackPath2DArc, TrackPathStraight>> trackPaths;
    private final Table<EnumFacing, Boolean, ITrackPath> trackPaths;

    public BlockTrackCurved(int width) {
        super(PROPERTY_FACING, PROPERTY_DIRECTION);
        if (width < 2) throw new IllegalArgumentException("Must be at least 2 wide!");
        trackPaths = HashBasedTable.create();
        TrainCraft.trainCraftLog.info("Curved track with a width of " + width);

        BlockPos creator = new BlockPos(0, 0, 0);
        for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
            Axis axis = horizontal.getAxis();
            int thing = (int) (horizontal.getAxisDirection().getOffset() * -0.5 + 0.5);
            Vec3 startPoint = new Vec3(axis == Axis.Z ? 0.5 : thing, TRACK_HEIGHT, axis == Axis.X ? 0.5 : thing);

            for (boolean positive : new boolean[] { false, true }) {
                int multiplier = positive ? 1 : -1;
                double radius = width * 3 - 1;

                double radiusMult = radius * multiplier;

                final Vec3 center;
                // @formatter:off
                      if (horizontal == EnumFacing.NORTH) center = startPoint.addVector(+radiusMult, 0,           0);
                 else if (horizontal == EnumFacing.EAST ) center = startPoint.addVector(          0, 0, -radiusMult);
                 else if (horizontal == EnumFacing.SOUTH) center = startPoint.addVector(-radiusMult, 0,           0);
                 else /* (horizontal == EnumFacing.WEST */center = startPoint.addVector(          0, 0, +radiusMult);
                 // @formatter:on

                double start = horizontal.getAxis() == Axis.X ? 270 : 180;
                start += horizontal.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 180;
                start -= positive ? 0 : 180;
                if (start > 360) start -= 360;
                if (start < 0) start += 360;

                double ang = 45 * multiplier * (horizontal.getAxis() == Axis.X ? -1 : 1);
                double end = start + ang;

                double radiusStart = radius;
                double triDist = width * 2 - 0.5;
                double radiusEnd = Math.pow((2 * triDist * triDist), 0.5);

                boolean reverse = (horizontal.getAxis() == Axis.Z) != positive;
                if (reverse) {
                    double holder = start;
                    start = end;
                    end = holder;
                    holder = radiusStart;
                    radiusStart = radiusEnd;
                    radiusEnd = holder;
                }

                ITrackPath arc = new TrackPath2DArc(creator, center, radiusStart, start * Math.PI / 180, end * Math.PI / 180);
                if (reverse) arc = arc.reverse();
                trackPaths.put(horizontal, positive, arc);
            }
        }

        // hard-coded
        if (width == 2) {
            Vec3 startDirection = new Vec3(0, 0, -1);
            Vec3 endDirection = new Vec3(-1, 0, -1);// .normalize();

            Vec3 start = new Vec3(0.5, TRACK_HEIGHT, 1);
            Vec3 end = new Vec3(-0.75, TRACK_HEIGHT, -1.25);
            Vec3 endReal = new Vec3(-1, TRACK_HEIGHT, -1.5);

            // formula for *a* position:
            // double t = *position*
            // Vec3 pos = start + startDirection * t;
            // so...
            // start + startDirection * a == end + endDirection * b
            // startDirection * a == end - start + endDirection * b
            // a = (end - start + endDirection * b) / startDirection

            Vec3 joinPos = new Vec3(0.5, TRACK_HEIGHT, 0);
            Vec3 commonPoint = findCommonPoint(start, startDirection, endReal, endDirection);
            if (!MCObjectUtils.equals(joinPos, commonPoint)) {
                throw new IllegalStateException(joinPos + " did not equal the computed " + commonPoint);
            }
            TrainCraft.trainCraftLog.info("join=" + joinPos);

            double distStart = joinPos.distanceTo(start);
            double distEnd = joinPos.distanceTo(end);

            TrainCraft.trainCraftLog.info("dS = " + distStart + ", dE=" + distEnd);

            double dist = Math.min(distStart, distEnd);

            Vec3 scaled = scale(startDirection, -dist);
            TrainCraft.trainCraftLog.info("startDir=" + startDirection);

            Vec3 startArc = joinPos.add(scaled);
            TrainCraft.trainCraftLog.info("startArc=" + startArc + ",scaled=" + scaled);
            Vec3 endArc = joinPos.add(scaled = scale(endDirection, dist));
            TrainCraft.trainCraftLog.info("endArc=" + endArc + ",scaled=" + scaled);

            Vec3 centerArc = findCommonPoint(startArc, perp(startDirection), endArc, perp(endDirection));
            TrainCraft.trainCraftLog.info("centerArc=" + centerArc);
            double radius = centerArc.distanceTo(endArc);
            int startAngle = 0;
            int endAngle = -45;

            TrackPathStraight startPath = new TrackPathStraight(start, startArc, creator);
            TrackPath2DArc arc = TrackPath2DArc.createDegrees(creator, centerArc, radius, startAngle, endAngle);
            TrackPathStraight endPath = new TrackPathStraight(endArc, endReal, creator);
            TrackPathComposite<TrackPathStraight, TrackPath2DArc, TrackPathStraight> composite;
            composite = new TrackPathComposite<>(creator, startPath, arc, endPath);
            trackPaths.put(EnumFacing.NORTH, false, composite);

        }
        // @formatter:off
//        if (width == 3)
//            trackPaths.put(EnumFacing.NORTH, false, new TrackPathComposite<TrackPath2DArc, TrackPathStraight>(creator,
//                            TrackPath2DArc.createDegrees(creator, new Vec3(3.5 - width * 3, TRACK_HEIGHT, 1), width * 3 - 3, Math.sqrt((width * 2 - 2)* (width * 2 - 2) * 2), 0, -45),
//                            new TrackPathStraight(new Vec3(-2.5, TRACK_HEIGHT, -2), new Vec3(-3, TRACK_HEIGHT, -2.5), creator)
//                        ));
//        if (width == 4)
//            trackPaths.put(EnumFacing.NORTH, false, new TrackPathComposite<TrackPath2DArc, TrackPathStraight>(creator,
//                            TrackPath2DArc.createDegrees(creator, new Vec3(3.5 - width * 3, TRACK_HEIGHT, 1), width * 3 - 3, Math.sqrt((width * 2 - 2)* (width * 2 - 2) * 2), 0, -45),
//                            new TrackPathStraight(new Vec3(-4.5, TRACK_HEIGHT, -3), new Vec3(-5, TRACK_HEIGHT, -3.5), creator)
//                        ));
        // @formatter:on
    }

    private static Vec3 findCommonPoint(Vec3 a, Vec3 deltaA, Vec3 b, Vec3 deltaB) {
        TrainCraft.trainCraftLog.info("findCommonPoint(" + a + ", " + deltaA + ", " + b + ", " + deltaB + ")");
        if (deltaA.xCoord == 0 && deltaB.xCoord != 0) {
            TrainCraft.trainCraftLog.info("Computing Z coords using the known deltaA.x = 0!");
            // We know that it will be on the same value as X
            double x = a.xCoord;
            double tb = (x - b.xCoord) / deltaB.xCoord;
            double z = b.zCoord + deltaB.zCoord * tb;
            return new Vec3(x, a.yCoord, z);
        } else if (deltaA.xCoord != 0 && deltaB.xCoord == 0) {
            TrainCraft.trainCraftLog.info("Computing Z coords using the known deltaB.x = 0!");
            // We know that it will be on the same value as X
            double x = b.xCoord;
            double ta = (x - a.xCoord) / deltaA.xCoord;
            double z = a.zCoord + deltaA.zCoord * ta;
            return new Vec3(x, a.yCoord, z);
        } else if (deltaA.xCoord != 0 && deltaB.xCoord != 0) {
            // z = mx + c
            TrainCraft.trainCraftLog.info("Computing X coords!");
            double ma = deltaA.zCoord / deltaA.xCoord;
            double ca = a.zCoord - ma * a.xCoord;

            double mb = deltaB.zCoord / deltaB.xCoord;
            double cb = b.zCoord - mb * b.zCoord;

            if (ma == mb) throw new IllegalArgumentException("The lines never meet! (a=" + a + ",dA=" + deltaA + ",b=" + b + ",dB=" + deltaB + ")");
            double x = (cb - ca) / (ma - mb);
            double z = a.zCoord + deltaA.zCoord * (x - a.xCoord) / deltaA.xCoord;
            return new Vec3(x, a.yCoord, z);
        } else if (deltaA.zCoord != 0 && deltaB.zCoord != 0) {
            // z = mx + c

            // THIS IS WRONG!
            TrainCraft.trainCraftLog.info("Computing Z coords!");
            double ma = deltaA.xCoord / deltaA.zCoord;
            double ca = a.xCoord - ma * a.zCoord;

            double mb = deltaB.xCoord / deltaB.zCoord;
            double cb = b.zCoord - mb * b.xCoord;

            if (ma == mb) throw new IllegalArgumentException("The lines never meet! (a=" + a + ",dA=" + deltaA + ",b=" + b + ",dB=" + deltaB + ")");
            double z = (cb - ca) / (ma - mb);
            double x = a.xCoord + deltaA.xCoord * (z - a.zCoord) / deltaA.zCoord;
            return new Vec3(x, a.yCoord, z);
        } else throw new IllegalArgumentException("The lines never meet! (a=" + a + ",dA=" + deltaA + ",b=" + b + ",dB=" + deltaB + ")");
    }

    private static Vec3 perp(Vec3 vec) {
        return new Vec3(-vec.zCoord, vec.yCoord, vec.xCoord);
    }

    private static Vec3 scale(Vec3 vec, double scale) {
        return new Vec3(vec.xCoord * scale, vec.yCoord, vec.zCoord * scale);
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
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        setBlockBounds(0, 0, 0, 1, TRACK_HEIGHT, 1);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        boolean positive = state.getValue(PROPERTY_DIRECTION);
        EnumFacing mainDirection = state.getValue(PROPERTY_FACING);
        return new ITrackPath[] { path(positive, mainDirection).offset(pos) };
    }

    public /* TrackPathComposite<TrackPath2DArc, TrackPathStraight> */ITrackPath path(boolean positive, EnumFacing mainDirection) {
        return trackPaths.get(mainDirection, positive);
    }

    @Override
    public boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        /* FIXME: Do a better check to actually make sure that the given block really is included in the path! */
        return true;
    }
}
