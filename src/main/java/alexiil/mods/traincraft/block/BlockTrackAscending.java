package alexiil.mods.traincraft.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathStraight;
import alexiil.mods.traincraft.property.BlockStatePropWrapper;
import alexiil.mods.traincraft.property.TrainCraftExtendedProperty;

public class BlockTrackAscending extends BlockTrackSeperated {
    /** Designates whether this track ascends towards the first or second axis given by {@link EnumDirection}. */
    public static final PropertyBool ASCEND_DIRECTION = PropertyBool.create("ascend_direction");
    public static final TrainCraftExtendedProperty<BlockStatePropWrapper> MATERIAL_TYPE = new TrainCraftExtendedProperty<>("material_type",
            BlockStatePropWrapper.class);

    public final int length;
    private final Table<EnumDirection, Boolean, ITrackPath> pathTable = HashBasedTable.create();
    private final Map<ITrackPath, List<BlockPos>> slaveOffsets = new HashMap<>();

    public BlockTrackAscending(int blocksLong) {
        super(TRACK_DIRECTION, ASCEND_DIRECTION, MATERIAL_TYPE);
        this.length = blocksLong;
        if (blocksLong <= 2) throw new IllegalArgumentException("Must be at least 3 long!");
        BlockPos creator = BlockPos.ORIGIN;
        for (EnumDirection dir : EnumDirection.values()) {
            if (dir != EnumDirection.NORTH_SOUTH && dir != EnumDirection.EAST_WEST) {
                // Its a bit more complex

            }
        }

        // NORTH_SOUTH
        TrackPathStraight straight = new TrackPathStraight(new Vec3(0.5, TRACK_HEIGHT, 0), new Vec3(0.5, TRACK_HEIGHT + 1, length), creator);
        pathTable.put(EnumDirection.NORTH_SOUTH, true, straight);
        List<BlockPos> positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(0, 0, i)).collect(Collectors.toList());
        slaveOffsets.put(straight, positions);

        straight = new TrackPathStraight(new Vec3(0.5, TRACK_HEIGHT, 1), new Vec3(0.5, TRACK_HEIGHT + 1, 1 - length), creator);
        pathTable.put(EnumDirection.NORTH_SOUTH, false, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(0, 0, -i)).collect(Collectors.toList());
        slaveOffsets.put(straight, positions);

        // EAST_WEST
        straight = new TrackPathStraight(new Vec3(0, TRACK_HEIGHT, 0.5), new Vec3(length, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, true, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(i, 0, 0)).collect(Collectors.toList());
        slaveOffsets.put(straight, positions);

        straight = new TrackPathStraight(new Vec3(1, TRACK_HEIGHT, 0.5), new Vec3(1 - length, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, false, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(-i, 0, 0)).collect(Collectors.toList());
        slaveOffsets.put(straight, positions);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        ITrackPath path = path(state);
        if (path == null) return new ITrackPath[0];
        return new ITrackPath[] { path.offset(pos) };
    }

    public ITrackPath path(IBlockState state) {
        EnumDirection direction = state.getValue(TRACK_DIRECTION);
        boolean ascending = state.getValue(ASCEND_DIRECTION);
        return pathTable.get(direction, ascending);
    }

    @Override
    public boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        BlockPos slaveOffset = slavePos.subtract(masterPos);
        return slaveOffsets(path(masterState)).contains(slaveOffset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState material = getSupportingMaterial(state, world, pos);
        if (material == null) return state;
        IExtendedBlockState extended = (IExtendedBlockState) state;
        return extended.withProperty((IUnlistedProperty<BlockStatePropWrapper>) MATERIAL_TYPE, new BlockStatePropWrapper(material));
    }

    private static IBlockState getSupportingMaterial(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            IBlockState offset = world.getBlockState(pos.offset(face));
            if (offset.getBlock() instanceof BlockTrackPointerAscending) {
                BlockTrackPointerAscending pointer = (BlockTrackPointerAscending) offset.getBlock();
                return pointer.getSupportingMaterial(world, pos, offset);
            }
        }
        return null;
    }

    public List<BlockPos> slaveOffsets(ITrackPath path) {
        return slaveOffsets.get(path) == null ? ImmutableList.of(BlockPos.ORIGIN) : slaveOffsets.get(path);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public List<BlockPos> getSlaveOffsets(IBlockState state) {
        return slaveOffsets(path(state));
    }
}
