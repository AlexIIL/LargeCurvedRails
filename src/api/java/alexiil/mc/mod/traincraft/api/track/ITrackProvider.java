package alexiil.mc.mod.traincraft.api.track;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackIdentifier;

public interface ITrackProvider {
    BehaviourWrapper getTrackFromPoint(World accces, BlockPos pos, IBlockState state, Vec3 from);

    BehaviourWrapper[] getTracksAsArray(World world, BlockPos pos, IBlockState state);

    List<BehaviourWrapper> getTracksAsList(World world, BlockPos pos, IBlockState state);

    Stream<BehaviourWrapper> getTracksAsStream(World world, BlockPos pos, IBlockState state);

    default BehaviourWrapper getTrackForIdent(World world, TrackIdentifier ident) {
        return getTracksAsStream(world, ident.pos(), world.getBlockState(ident.pos())).filter(b -> b.getIdentifier().equals(ident)).findAny().orElse(
                null);
    }

    ITrackBlock getBlockFor(IBlockState state);

    void registerBlock(Block block, ITrackBlock track);

    void unregister(Block block);
}
