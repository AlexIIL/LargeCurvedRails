package alexiil.mods.traincraft.api.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.lib.MCObjectUtils;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;

/** Designates that a train can go from one section to another of this track block. Used to handle positioning trains on
 * tracks. */
public interface ITrackBlock {
    /** Gets all of the behaviours that are contained by this block */
    Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state);

    /** Gets all of the behaviours that start or end at the given point. This is generally only useful if this is a
     * point and the train can take more than one route (is either being driven by a player or a logistical network). */
    default Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state, Vec3 from) {
        return behaviours(access, pos, state).stream().filter((b) -> {
            ITrackPath p = b.getPath(access, pos, state);
            return MCObjectUtils.equals(p.start(), from) || MCObjectUtils.equals(p.end(), from);
        }).collect(Collectors.toCollection(() -> new ArrayList<>()));
    }

    /** Gets the currently active behaviour for the given point, or null if no behaviours exist from the point. This is
     * useful if the train just needs to go somewhere */
    TrackBehaviour currentBehaviour(IBlockAccess access, BlockPos pos, IBlockState state, Vec3 from);
}
