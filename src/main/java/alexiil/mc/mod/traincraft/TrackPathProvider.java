package alexiil.mc.mod.traincraft;

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
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.ITrackBlock;
import alexiil.mc.mod.traincraft.api.track.ITrackProvider;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;

public enum TrackPathProvider implements ITrackProvider {
    INSTANCE;
    private Map<Block, ITrackBlock> registeredBlocks = new HashMap<>();

    @Override
    public BehaviourWrapper getTrackFromPoint(World accces, BlockPos pos, IBlockState state, Vec3 from) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return null;
        return block.currentBehaviour(accces, pos, state, from);
    }

    @Override
    public BehaviourWrapper[] getTracksAsArray(World world, BlockPos pos, IBlockState state) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return new BehaviourWrapper[0];
        Collection<BehaviourWrapper> collection = block.behaviours(world, pos, state);
        return collection.toArray(new BehaviourWrapper[collection.size()]);
    }

    @Override
    public List<BehaviourWrapper> getTracksAsList(World world, BlockPos pos, IBlockState state) {
        return ImmutableList.copyOf(getTracksAsArray(world, pos, state));
    }

    @Override
    public Stream<BehaviourWrapper> getTracksAsStream(World world, BlockPos pos, IBlockState state) {
        return Stream.of(getTracksAsArray(world, pos, state));
    }

    @Override
    public ITrackBlock getBlockFor(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof ITrackBlock) return (ITrackBlock) block;
        return registeredBlocks.get(block);
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
