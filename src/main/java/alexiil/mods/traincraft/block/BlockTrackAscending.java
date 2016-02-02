package alexiil.mods.traincraft.block;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathStraight;

public class BlockTrackAscending extends BlockTrackSeperated {
    /** Designates whether this track ascends towards the first or second axis given by {@link EnumDirection}. */
    public static final PropertyBool ASCEND_DIRECTION = PropertyBool.create("ascend_direction");

    private final Table<EnumDirection, Boolean, ITrackPath> pathTable = HashBasedTable.create();

    public BlockTrackAscending(int blocksLong) {
        super(TRACK_DIRECTION, ASCEND_DIRECTION);
        if (blocksLong <= 2) throw new IllegalArgumentException("Must be at least 3 long!");
        BlockPos creator = BlockPos.ORIGIN;
        for (EnumDirection dir : EnumDirection.values()) {
            if (dir == EnumDirection.NORTH_SOUTH || dir == EnumDirection.EAST_WEST) {
                // Its nice and simple

            } else {
                // Its a bit more complex

            }
        }

        // NORTH_SOUTH
        TrackPathStraight straight = new TrackPathStraight(new Vec3(0.5, TRACK_HEIGHT, 0), new Vec3(0.5, TRACK_HEIGHT + 1, 3), creator);
        pathTable.put(EnumDirection.NORTH_SOUTH, true, straight);

        straight = new TrackPathStraight(new Vec3(0.5, TRACK_HEIGHT, 1), new Vec3(0.5, TRACK_HEIGHT + 1, -2), creator);
        pathTable.put(EnumDirection.NORTH_SOUTH, false, straight);

        // EAST_WEST
        straight = new TrackPathStraight(new Vec3(0, TRACK_HEIGHT, 0.5), new Vec3(3, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, true, straight);

        straight = new TrackPathStraight(new Vec3(1, TRACK_HEIGHT, 0.5), new Vec3(-2, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, false, straight);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        EnumDirection direction = state.getValue(TRACK_DIRECTION);
        boolean ascending = state.getValue(ASCEND_DIRECTION);
        ITrackPath path = pathTable.get(direction, ascending);
        if (path == null) return new ITrackPath[0];
        return new ITrackPath[] { path.offset(pos) };
    }

    @Override
    public boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        /* FIXME: Do a better check to actually make sure that the given block really is included in the path! */
        return true;
    }
}
