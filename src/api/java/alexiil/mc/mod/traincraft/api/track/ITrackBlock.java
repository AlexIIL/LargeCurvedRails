package alexiil.mc.mod.traincraft.api.track;

import java.util.stream.Stream;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.lib.MCObjectUtils;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

/** Designates that a train can go from one section to another of this track block. Used to handle positioning trains on
 * tracks. */
public interface ITrackBlock {
    /** Gets all of the behaviours that are contained by this block */
    Stream<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state);

    /** Gets all of the behaviours that start or end at the given point. This is generally only useful if this is a
     * point and the train can take more than one route (is either being driven by a player or a logistical network). */
    default Stream<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state, Vec3d from) {
        return behaviours(world, pos, state).filter((b) -> {
            ITrackPath p = b.getPath();
            return MCObjectUtils.equals(p.start(), from) || MCObjectUtils.equals(p.end(), from);
        });
    }

    /** Gets the currently active behaviour for the given point, or null if no behaviours exist from the point. This is
     * useful if the train just needs to go somewhere. */
    default BehaviourWrapper currentBehaviour(World world, BlockPos pos, IBlockState state, Vec3d from) {
        return behaviours(world, pos, state, from).findAny().orElse(null);
    }
}
