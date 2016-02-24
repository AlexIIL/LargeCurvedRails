package alexiil.mods.traincraft;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackPlacer;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public enum TrackPlacer implements ITrackPlacer {
    INSTANCE;

    @Override
    public boolean tryPlaceTrack(TrackBehaviourStateful behaviour, World world, BlockPos pos, IBlockState state) {
        return false;
    }

    @Override
    public boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state) {
        throw new AbstractMethodError("Implement this!");
    }
}
