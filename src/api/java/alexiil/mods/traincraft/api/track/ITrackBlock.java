package alexiil.mods.traincraft.api.track;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;

/** Designates that a train can go from one section to another of this track block. Used to handle positioning trains on
 * tracks. */
public interface ITrackBlock {
    /** Gets all of the paths that can be taken by trains over this block. The array must never be null or empty.
     * 
     * @return A path that starts at the middle of the bottom of the track and ends at the middle of the bottom of the
     *         track */
    ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state);

    TrackBehaviour[] behaviours(IBlockAccess access, BlockPos pos, IBlockState state);
}
