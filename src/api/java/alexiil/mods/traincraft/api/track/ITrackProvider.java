package alexiil.mods.traincraft.api.track;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;

public interface ITrackProvider {
    TrackBehaviour getTrackFromPoint(IBlockAccess accces, BlockPos pos, IBlockState state, Vec3 from);

    TrackBehaviour[] getTracksAsArray(IBlockAccess access, BlockPos pos, IBlockState state);

    List<TrackBehaviour> getTracksAsList(IBlockAccess access, BlockPos pos, IBlockState state);

    Stream<TrackBehaviour> getTracksAsStream(IBlockAccess access, BlockPos pos, IBlockState state);

    default BehaviourWrapper wrap(TrackBehaviour behaviour, World world, BlockPos pos) {
        return new BehaviourWrapper(behaviour, world, pos);
    }

    ITrackBlock getBlockFor(IBlockState state);

    void registerBlock(Block block, ITrackBlock track);

    void unregister(Block block);
}