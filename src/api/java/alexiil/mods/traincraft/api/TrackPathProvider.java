package alexiil.mods.traincraft.api;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TrackPathProvider {
    private static Map<Block, ITrackBlock> registeredBlocks = new HashMap<>();

    public static ITrackPath[] getPathsAsArray(IBlockAccess access, BlockPos pos, IBlockState state) {
        ITrackBlock block = getBlockFor(access, pos, state);
        if (block == null) return new ITrackPath[0];
        return block.paths(access, pos, state);
    }

    public static List<ITrackPath> getPathsAsList(IBlockAccess access, BlockPos pos, IBlockState state) {
        return ImmutableList.copyOf(getPathsAsArray(access, pos, state));
    }

    public static Stream<ITrackPath> getPathsAsStream(IBlockAccess access, BlockPos pos, IBlockState state) {
        return Stream.of(getPathsAsArray(access, pos, state));
    }

    public static ITrackBlock getBlockFor(IBlockAccess access, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof ITrackBlock) return (ITrackBlock) block;
        return registeredBlocks.get(block);
    }

    public static int pathIndex(World world, ITrackPath path) {
        ITrackPath[] arr = getPathsAsArray(world, path.creatingBlock(), world.getBlockState(path.creatingBlock()));
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(path)) return i;
            if (arr[i].reverse().equals(path)) return i;
        }
        return -1;
    }

    public static boolean isPathReversed(World world, ITrackPath path) {
        ITrackPath[] arr = getPathsAsArray(world, path.creatingBlock(), world.getBlockState(path.creatingBlock()));
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(path)) return false;
            if (arr[i].reverse().equals(path)) return true;
        }
        return false;
    }

    public static void registerBlock(Block block, ITrackBlock track) {
        registeredBlocks.put(block, track);
    }

    public static void unregister(Block block) {
        registeredBlocks.remove(block);
    }

    static {
        // Vanilla tracks
        Map<EnumRailDirection, ITrackPath> map = new EnumMap<>(EnumRailDirection.class);

        double trackHeight = 2 / 16.0;

        Vec3 north = new Vec3(0.5, trackHeight, 0);
        Vec3 south = new Vec3(0.5, trackHeight, 1);
        Vec3 west = new Vec3(0, trackHeight, 0.5);
        Vec3 east = new Vec3(1, trackHeight, 0.5);

        Vec3 up = new Vec3(0, 1, 0);

        BlockPos from = new BlockPos(0, 0, 0);

        map.put(EnumRailDirection.NORTH_SOUTH, new TrackPathStraight(north, south, from));
        map.put(EnumRailDirection.EAST_WEST, new TrackPathStraight(east, west, from));
        map.put(EnumRailDirection.ASCENDING_EAST, new TrackPathStraight(west, east.add(up), from));
        map.put(EnumRailDirection.ASCENDING_WEST, new TrackPathStraight(east, west.add(up), from));
        map.put(EnumRailDirection.ASCENDING_NORTH, new TrackPathStraight(south, north.add(up), from));
        map.put(EnumRailDirection.ASCENDING_SOUTH, new TrackPathStraight(north, south.add(up), from));

        Vec3 bezPos = new Vec3(0.5, trackHeight, 0.5);// TODO: Change these to arcs!
        map.put(EnumRailDirection.SOUTH_EAST, new TrackPathCurved(from, south, bezPos, east));
        map.put(EnumRailDirection.SOUTH_WEST, new TrackPathCurved(from, south, bezPos, west));
        map.put(EnumRailDirection.NORTH_WEST, new TrackPathCurved(from, north, bezPos, west));
        map.put(EnumRailDirection.NORTH_EAST, new TrackPathCurved(from, north, bezPos, east));

        BlockRailBase[] rails = { (BlockRailBase) Blocks.rail, (BlockRailBase) Blocks.activator_rail, (BlockRailBase) Blocks.detector_rail,
            (BlockRailBase) Blocks.golden_rail };

        for (BlockRailBase rail : rails) {
            ITrackBlock trackBlock = new ITrackBlock() {
                @Override
                public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
                    EnumRailDirection dir = state.getValue(rail.getShapeProperty());
                    return new ITrackPath[] { map.get(dir).offset(pos) };
                }
            };
            registerBlock(rail, trackBlock);
        }
    }
}
