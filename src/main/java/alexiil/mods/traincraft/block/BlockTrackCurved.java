package alexiil.mods.traincraft.block;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPath2DArc;
import alexiil.mods.traincraft.api.TrackPathCurved;

public class BlockTrackCurved extends BlockTrackSeperated {
    public static final PropertyEnum<EnumFacing> PROPERTY_FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    private final Table<EnumFacing, Boolean, ITrackPath> trackPaths;

    @SuppressWarnings("incomplete-switch")
    public BlockTrackCurved(int width) {
        super(PROPERTY_FACING, PROPERTY_DIRECTION);
        if (width < 2) throw new IllegalArgumentException("Must be at least 2 wide!");
        trackPaths = HashBasedTable.create();
        int w = width - 1;
        int ww = w * w;
        double sq = Math.sqrt(ww + ww);
        int diagonalLength = MathHelper.floor_double(sq);
        TrainCraft.trainCraftLog.info("Curved track with a width of " + width);

        BlockPos creator = new BlockPos(0, 0, 0);
        for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
            //
            // float angle;
            // switch (horizontal) {
//                // @formatter:off
//                case NORTH: angle =   0f;
//                case EAST:  angle =  90f;
//                case SOUTH: angle = 180f;
//                case WEST:  angle = 270f;
//                // @formatter:on
            // }

            Axis axis = horizontal.getAxis();
            int thing = (int) (horizontal.getAxisDirection().getOffset() * -0.5 + 0.5);
            Vec3 startPoint = new Vec3(axis == Axis.Z ? 0.5 : thing, TRACK_HEIGHT, axis == Axis.X ? 0.5 : thing);

            double diff = diagonalLength * horizontal.getAxisDirection().getOffset();
            Vec3 bezPoint = startPoint.addVector(axis == Axis.X ? diff : 0, 0, axis == Axis.Z ? diff : 0);

            Vec3 offset = new Vec3(axis == Axis.X ? diff : 0, 0, axis == Axis.Z ? diff : 0);
            for (boolean positive : new boolean[] { false, true }) {
                // BEZ
                EnumFacing other = getOther(horizontal, positive);
                axis = other.getAxis();
                double actualDiff = diff * other.getAxisDirection().getOffset();
                Vec3 otherOffset = new Vec3(axis == Axis.X ? actualDiff : 0, 0, axis == Axis.Z ? actualDiff : 0);

                Vec3 endPoint = bezPoint.add(offset).add(otherOffset);
                TrackPathCurved curved = new TrackPathCurved(creator, startPoint, bezPoint, endPoint);

                // ARC

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
                
                // WARNING: Some of these are off by one!

                TrackPath2DArc arc = new TrackPath2DArc(creator, center, radius, start * Math.PI / 180, end * Math.PI / 180);

                TrainCraft.trainCraftLog.info("\t" + horizontal + ", " + positive);
                for (int i = 0; i < 10; i++) {
                    double pos = i / 9.0;
                    Vec3 point = arc.interpolate(pos);
                    Vec3 dir = arc.direction(pos);
                    TrainCraft.trainCraftLog.info("\t\t" + i + " = " + point + " -> " + dir);
                }

                trackPaths.put(horizontal, positive, arc);
            }
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
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        setBlockBounds(0, 0, 0, 1, TRACK_HEIGHT, 1);
    }

    private static EnumFacing getOther(EnumFacing mainDirection, boolean positive) {
        EnumFacing toUse = EnumFacing.EAST;
        if (mainDirection.getAxis() == toUse.getAxis()) toUse = EnumFacing.SOUTH;
        if (!positive) toUse = toUse.getOpposite();
        return toUse;
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        boolean positive = state.getValue(PROPERTY_DIRECTION);
        EnumFacing mainDirection = state.getValue(PROPERTY_FACING);
        return new ITrackPath[] { path(positive, mainDirection).offset(pos) };
    }

    public ITrackPath path(boolean positive, EnumFacing mainDirection) {
        return trackPaths.get(mainDirection, positive);
    }

    @Override
    public boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        /* FIXME: Do a better check to actually make sure that the given block really is included in the path! */
        return true;
    }
}
