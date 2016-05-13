package alexiil.mc.mod.traincraft.api.track;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public interface ITrackPlacer {
    /** Attempts to place the given track down in the world, adding it to surrounding tracks if necessary. You should
     * only call this if the native version didn't work.
     * 
     * @return True if the track was successfully placed (so you should use up the item from the players inventory),
     *         false otherwise. */
    boolean tryPlaceTrack(TrackBehaviourStateful behaviour, World world, BlockPos pos);

    /** Places all of the slave/pointer blocks down for the given behaviour. The behaviour is assumed to have already
     * been added to the world. */
    void placeSlaves(TrackBehaviour behaviour, World world, BlockPos pos);

    /** Tests to see if {@link #placeSlaves(TrackBehaviour, World, BlockPos)} would completly place down all the slaves
     * necessary for the given behaviour. */
    default boolean canPlaceSlaves(TrackBehaviour behaviour, World world, BlockPos pos) {
        return checkSlaves(behaviour, world, pos) == null;
    }

    default boolean canPlaceSlaves(Set<BlockPos> slaveOffsets, World world, BlockPos pos) {
        return checkSlaves(slaveOffsets, world, pos) == null;
    }

    default EnumTrackRequirement checkSlaves(TrackBehaviour behaviour, World world, BlockPos pos) {
        return checkSlaves(behaviour.getSlaveOffsets(world, pos, world.getBlockState(pos)), world, pos);
    }

    EnumTrackRequirement checkSlaves(Set<BlockPos> slaveOffsets, World world, BlockPos pos);

    default boolean tryPlaceTrackAndSlaves(TrackBehaviourStateful behaviour, World world, BlockPos pos) {
        if (!canPlaceSlaves(behaviour, world, pos)) return false;
        if (!tryPlaceTrack(behaviour, world, pos)) return false;
        placeSlaves(behaviour, world, pos);
        return true;
    }

    /** Removes the given behaviour from the world. If the given behaviour doesn not exist at the given position this
     * returns false without doing anything.
     * 
     * @return True if the behaviour was removed from the world. */
    boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state);

    /** Checks to see if the given block is upgradable to a multi-track tile entity. */
    boolean isUpgradableTrack(World world, BlockPos pos);

    public enum EnumTrackRequirement {
        GROUND_BELOW,
        SPACE_ABOVE,
        OTHER
    }
}
