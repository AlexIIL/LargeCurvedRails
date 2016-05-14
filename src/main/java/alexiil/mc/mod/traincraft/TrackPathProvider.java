package alexiil.mc.mod.traincraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.ITrackBlock;
import alexiil.mc.mod.traincraft.api.track.ITrackProvider;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;

public enum TrackPathProvider implements ITrackProvider {
    INSTANCE;
    private Map<Block, ITrackBlock> registeredBlocks = new HashMap<>();

    @Override
    public BehaviourWrapper getTrackFromPoint(World accces, BlockPos pos, IBlockState state, Vec3d from) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return null;
        return block.currentBehaviour(accces, pos, state, from);
    }

    @Override
    public BehaviourWrapper[] getTracksAsArray(World world, BlockPos pos, IBlockState state) {
        List<BehaviourWrapper> lst = getTracksAsList(world, pos, state);
        return lst.toArray(new BehaviourWrapper[lst.size()]);
    }

    @Override
    public List<BehaviourWrapper> getTracksAsList(World world, BlockPos pos, IBlockState state) {

        Stream<BehaviourWrapper> stream = getTracksAsStream(world, pos, state);
        return stream.collect(Collectors.toList());
    }

    @Override
    public Stream<BehaviourWrapper> getTracksAsStream(World world, BlockPos pos, IBlockState state) {
        ITrackBlock block = getBlockFor(state);
        if (block == null) return Stream.empty();
        return block.behaviours(world, pos, state);
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
