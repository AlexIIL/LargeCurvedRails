package alexiil.mods.traincraft.api.track;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public interface ITrackPlacer {
    /** Attempts to place the given track down in the world, adding it to surrounding tracks if necessary.
     * 
     * @return True if the track was successfully placed (so you should use up the item from the players inventory),
     *         false otherwise. */
    boolean tryPlaceTrack(TrackBehaviourStateful behaviour, World world, BlockPos pos, IBlockState state);

    /** Removes the given behaviour from the world. If the given behaviour doesn not exist at the given position this
     * returns false without doing anything.
     * 
     * @return True if the behaviour was removed from the world. */
    boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state);
}
