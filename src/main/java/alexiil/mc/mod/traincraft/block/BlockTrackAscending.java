package alexiil.mc.mod.traincraft.block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;
import alexiil.mc.mod.traincraft.property.BlockStatePropWrapper;
import alexiil.mc.mod.traincraft.property.TrainCraftExtendedProperty;

public class BlockTrackAscending extends BlockTrackSeperated {
    /** Designates whether this track ascends towards the first or second axis given by {@link EnumDirection}. */
    public static final PropertyBool ASCEND_DIRECTION = PropertyBool.create("ascend_direction");
    public static final TrainCraftExtendedProperty<BlockStatePropWrapper> MATERIAL_TYPE = new TrainCraftExtendedProperty<>("material_type",
            BlockStatePropWrapper.class);

    public final int length;
    private final Table<EnumDirection, Boolean, ITrackPath> pathTable = HashBasedTable.create();
    private final Map<ITrackPath, Set<BlockPos>> slaveOffsets = new HashMap<>();

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
        Set<BlockPos> positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(0, 0, i)).collect(Collectors.toSet());
        slaveOffsets.put(straight, positions);

        straight = new TrackPathStraight(new Vec3(0.5, TRACK_HEIGHT, 1), new Vec3(0.5, TRACK_HEIGHT + 1, 1 - length), creator);
        pathTable.put(EnumDirection.NORTH_SOUTH, false, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(0, 0, -i)).collect(Collectors.toSet());
        slaveOffsets.put(straight, positions);

        // EAST_WEST
        straight = new TrackPathStraight(new Vec3(0, TRACK_HEIGHT, 0.5), new Vec3(length, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, true, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(i, 0, 0)).collect(Collectors.toSet());
        slaveOffsets.put(straight, positions);

        straight = new TrackPathStraight(new Vec3(1, TRACK_HEIGHT, 0.5), new Vec3(1 - length, TRACK_HEIGHT + 1, 0.5), creator);
        pathTable.put(EnumDirection.EAST_WEST, false, straight);
        positions = IntStream.range(0, length).mapToObj((i) -> new BlockPos(-i, 0, 0)).collect(Collectors.toSet());
        slaveOffsets.put(straight, positions);
    }

    // @Override
    // public ITrackPath[] paths(World world, BlockPos pos, IBlockState state) {
    // ITrackPath path = path(state);
    // if (path == null) return new ITrackPath[0];
    // return new ITrackPath[] { path.offset(pos) };
    // }

    @Override
    public BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state) {
        // FIXME!
        return null;
    }

    public ITrackPath path(IBlockState state) {
        EnumDirection direction = state.getValue(TRACK_DIRECTION);
        boolean ascending = state.getValue(ASCEND_DIRECTION);
        return pathTable.get(direction, ascending);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
        IBlockState material = getSupportingMaterial(access, pos);
        if (material == null) return state;
        IExtendedBlockState extended = (IExtendedBlockState) state;
        return extended.withProperty((IUnlistedProperty<BlockStatePropWrapper>) MATERIAL_TYPE, new BlockStatePropWrapper(material));
    }

    private static IBlockState getSupportingMaterial(IBlockAccess access, BlockPos pos) {
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            IBlockState offset = access.getBlockState(pos.offset(face));
            if (offset.getBlock() instanceof BlockTrackPointerAscending) {
                BlockTrackPointerAscending pointer = (BlockTrackPointerAscending) offset.getBlock();
                return pointer.getSupportingMaterial(access, pos, offset);
            }
        }
        return null;
    }

    public Set<BlockPos> slaveOffsets(ITrackPath path) {
        return slaveOffsets.get(path) == null ? ImmutableSet.of(BlockPos.ORIGIN) : slaveOffsets.get(path);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess access, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(IBlockState state) {
        return slaveOffsets(path(state));
    }
}
