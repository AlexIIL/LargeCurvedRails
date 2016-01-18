package alexiil.mods.traincraft.block;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathCurved;

public class BlockCurvedTrack extends BlockSeperatedTrack {
    public static final PropertyEnum<EnumFacing> PROPERTY_FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    private final Table<EnumFacing, Boolean, ITrackPath> trackPaths;
    private final int length, width;

    public BlockCurvedTrack(int width) {
        super(PROPERTY_FACING, PROPERTY_MASTER, PROPERTY_DIRECTION);
        if (width < 2) throw new IllegalArgumentException("Must be at least 2 wide!");
        trackPaths = HashBasedTable.create();
        int w = width - 1;
        int ww = w * w;
        double sq = Math.sqrt(ww + ww);
        int diagonalLength = MathHelper.floor_double(sq);
        this.length = diagonalLength * 2;
        this.width = width;

        TrainCraft.trainCraftLog.info("Curved track with a width of " + width);

        BlockPos creator = new BlockPos(0, 0, 0);
        for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
            Axis axis = horizontal.getAxis();
            int thing = (int) (horizontal.getAxisDirection().getOffset() * -0.5 + 0.5);
            Vec3 startPoint = new Vec3(axis == Axis.Z ? 0.5 : thing, TRACK_HEIGHT, axis == Axis.X ? 0.5 : thing);

            double diff = diagonalLength * horizontal.getAxisDirection().getOffset();
            Vec3 bezPoint = startPoint.addVector(axis == Axis.X ? diff : 0, 0, axis == Axis.Z ? diff : 0);

            Vec3 offset = new Vec3(axis == Axis.X ? diff : 0, 0, axis == Axis.Z ? diff : 0);
            for (boolean positive : new boolean[] { false, true }) {
                EnumFacing other = getOther(horizontal, positive);
                axis = other.getAxis();
                Vec3 otherOffset = new Vec3(axis == Axis.X ? diff : 0, 0, axis == Axis.Z ? diff : 0);

                Vec3 endPoint = bezPoint.add(offset).add(otherOffset);
                TrackPathCurved curved = new TrackPathCurved(creator, startPoint, bezPoint, endPoint);

                TrainCraft.trainCraftLog.info("\t" + horizontal + ", " + positive);
                for (int i = 0; i < 10; i++) {
                    double pos = i / 9.0;
                    Vec3 point = curved.interpolate(pos);
                    Vec3 dir = curved.direction(pos);
                    TrainCraft.trainCraftLog.info("\t\t" + i + " = " + point + " -> " + dir);
                }

                trackPaths.put(horizontal, positive, curved);
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
        EnumFacing other = getOther(mainDirection, positive);

        BlockPos coordinate = findMaster(access, pos, length + width, (tryState) -> {
            if (tryState.getValue(PROPERTY_FACING) != mainDirection) return false;
            return tryState.getValue(PROPERTY_DIRECTION) == positive;
        } , mainDirection, other);

        if (coordinate == null) return new ITrackPath[0];
        return new ITrackPath[] { trackPaths.get(mainDirection, positive).offset(coordinate) };
    }
}
