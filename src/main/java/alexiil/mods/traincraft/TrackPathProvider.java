package alexiil.mods.traincraft;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackBlock;
import alexiil.mods.traincraft.api.track.ITrackProvider;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.compat.vanilla.VanillaAddon;

public enum TrackPathProvider implements ITrackProvider {
    INSTANCE;
    private Map<Block, ITrackBlock> registeredBlocks = new HashMap<>();

    @Override
    public TrackBehaviour getTrackFromPoint(IBlockAccess accces, BlockPos pos, IBlockState state, Vec3 from) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return null;
        return block.currentBehaviour(accces, pos, state, from);
    }

    @Override
    public TrackBehaviour[] getTracksAsArray(IBlockAccess access, BlockPos pos, IBlockState state) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return new TrackBehaviour[0];
        Collection<TrackBehaviour> collection = block.behaviours(access, pos, state);
        return collection.toArray(new TrackBehaviour[collection.size()]);
    }

    @Override
    public List<TrackBehaviour> getTracksAsList(IBlockAccess access, BlockPos pos, IBlockState state) {
        return ImmutableList.copyOf(getTracksAsArray(access, pos, state));
    }

    @Override
    public Stream<TrackBehaviour> getTracksAsStream(IBlockAccess access, BlockPos pos, IBlockState state) {
        return Stream.of(getTracksAsArray(access, pos, state));
    }

    @Override
    public ITrackBlock getBlockFor(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof ITrackBlock) return (ITrackBlock) block;
        return registeredBlocks.get(block);
    }

    /** Replaced with {@link TrackBehaviour#getIdentifier(World, BlockPos, IBlockState)} */
    @Deprecated
    public static int pathIndex(World world, ITrackPath path) {
        TrackBehaviour[] arr = INSTANCE.getTracksAsArray(world, path.creatingBlock(), world.getBlockState(path.creatingBlock()));
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(path)) return i;
            // if (arr[i].reverse().equals(path)) return i;
        }
        return -1;
    }

    /** Replaced with {@link TrackBehaviour#getIdentifier(World, BlockPos, IBlockState)} */
    @Deprecated
    public static boolean isPathReversed(World world, ITrackPath path) {
        TrackBehaviour[] arr = INSTANCE.getTracksAsArray(world, path.creatingBlock(), world.getBlockState(path.creatingBlock()));
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(path)) return false;
            // if (arr[i].reverse().equals(path)) return true;
        }
        return false;
    }

    @Override
    public void registerBlock(Block block, ITrackBlock track) {
        registeredBlocks.put(block, track);
    }

    @Override
    public void unregister(Block block) {
        registeredBlocks.remove(block);
    }
}
