package alexiil.mods.traincraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackPlacer;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.BlockTrackPointer;
import alexiil.mods.traincraft.block.BlockTrackPointer.EnumOffset;
import alexiil.mods.traincraft.block.BlockTrackPointerMultiple;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.item.ItemBlockSeperatedTrack;
import alexiil.mods.traincraft.tile.TileTrackMultiple;
import alexiil.mods.traincraft.tile.TileTrackMultiplePointer;
import alexiil.mods.traincraft.track.TrackBehaviourPointerNative;

public enum TrackPlacer implements ITrackPlacer {
    INSTANCE;

    @Override
    public boolean tryPlaceTrack(TrackBehaviourStateful behaviour, World world, BlockPos pos, IBlockState state) {
        Set<BlockPos> slaves = behaviour.getSlaveOffsets();
        // Look through all of the slaves
        // --Make sure space is available if its not a track
        // -- if it is a track make sure the converted form (if its not already a stateful version) can intersect with
        // the given one

        for (BlockPos offset : slaves) {
            BlockPos slave = pos.add(offset);
            IBlockState slaveState = world.getBlockState(slave);
            if (slaveState.getBlock().isReplaceable(world, slave)) continue;
            List<TrackBehaviour> behaviours = TrackPathProvider.INSTANCE.getTracksAsList(world, slave, slaveState);
            if (behaviours.isEmpty()) return false;
            for (TrackBehaviour behaviour2 : behaviours) {
                TrackBehaviourStateful stateful;
                if (behaviour2 instanceof TrackBehaviourStateful) {
                    stateful = (TrackBehaviourStateful) behaviour2;
                } else if (behaviour2 instanceof TrackBehaviourPointerNative) {
                    // We need to special case pointers as they work differently
                    TrackBehaviourPointerNative pointer = (TrackBehaviourPointerNative) behaviour2;
                    BehaviourWrapper target = pointer.getPointedTo(world);
                    if (target.behaviour() instanceof TrackBehaviourStateful) {
                        stateful = (TrackBehaviourStateful) target.behaviour();
                    } else {
                        stateful = ((TrackBehaviourNative) target.behaviour()).convertToStateful(world, target.pos(), target.state());
                    }
                } else if (behaviour2 instanceof TrackBehaviourNative) {
                    stateful = ((TrackBehaviourNative) behaviour2).convertToStateful(world, slave, slaveState);
                } else return false;
                if (stateful == null) return false;
                if (!stateful.canOverlap(behaviour)) return false;
                if (!behaviour.canOverlap(stateful)) return false;
            }
        }

        // If above then:
        // --Place down our stateful version
        // --Replace all overlapping tracks with the stateful version
        
        IBlockState current = world.getBlockState(pos);
        if (current.getBlock() instanceof BlockTrackPointerMultiple || current.getBlock() instanceof BlockTrackPointer) {
            world.setBlockToAir(pos);// TODO: Actually add the behaviour
        }
        
        List<TrackBehaviourStateful> stateful = new ArrayList<>();
        for (BlockPos offset : slaves) {
            BlockPos slave = pos.add(offset);
            IBlockState slaveState = world.getBlockState(slave);
            stateful.addAll(convertToTile(world, slave, slaveState));
        }

        for (TrackBehaviourStateful behaviourStateful : stateful) {
            createPointers(world, behaviourStateful);
        }

        return true;
    }

    /** Converts all of the tracks in the given block to either a multi-tile or (if it was a pointer) just air.
     * 
     * @return A list of all the stateful tracks that (might) need pointers creating. */
    private static List<TrackBehaviourStateful> convertToTile(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockTrackPointerMultiple || state.getBlock() instanceof BlockTrackPointer) {
            // Just set it to air and we will reset the pointers back up afterwards
            world.setBlockToAir(pos);
            return Collections.emptyList();
        } else if (state.getBlock() instanceof ITileEntityProvider) {
            // It must already be converted
            return Collections.emptyList();
        }
        List<TrackBehaviour> behaviours = TrackPathProvider.INSTANCE.getTracksAsList(world, pos, state);
        List<TrackBehaviourStateful> stateful = new ArrayList<>();
        TileTrackMultiple mult = new TileTrackMultiple();
        mult.setWorldObj(world);
        for (TrackBehaviour b : behaviours) {
            TrackBehaviourNative behaviourNative = (TrackBehaviourNative) b;
            TrackBehaviourStateful s = behaviourNative.convertToStateful(world, pos, state);
            /* We checked for this in the function above, and if its null here then either our logic is borked or we
             * have a strange behaviour object. Either way we don't want to continue */
            if (s == null) throw new IllegalStateException("Thats.. not possible.");
            stateful.add(s);
            mult.addTrack(s);
        }
        // Set the block and then the tile.
        world.setBlockState(pos, TCBlocks.TRACK_MULTIPLE.getBlock().getDefaultState());
        world.setTileEntity(pos, mult);
        return stateful;
    }

    /** Creates all of the pointers necessary for this behaviour. All pointers will be converted to
     * {@link TileTrackMultiplePointer}, so they (might) need to be down-converted back to native pointers if they only
     * point to a single behaviour.
     * 
     * @return A list of all the pointer block positions created. */
    private static void createPointers(World world, TrackBehaviourStateful behaviourStateful) {
        BlockPos p = behaviourStateful.getIdentifier().pos();
        Set<BlockPos> slaves = behaviourStateful.getSlaveOffsets();

        for (BlockPos slave : slaves) {
            BlockPos offset = p.add(slave);
            IBlockState current = world.getBlockState(offset);
            if (current.getBlock().isReplaceable(world, offset)) {
                if (!current.getBlock().isAir(world, offset)) world.setBlockToAir(offset);
                // We only need to create it
                EnumOffset pointerOffset = ItemBlockSeperatedTrack.calculateOffsetTo(slave, slaves, BlockPos.ORIGIN);
                IBlockState pointerState = TCBlocks.TRACK_POINTER.getBlock().getDefaultState();
                pointerState = pointerState.withProperty(BlockTrackPointer.PROP_OFFSET, pointerOffset);
                world.setBlockState(offset, pointerState);
            } else if (current.getBlock() instanceof BlockTrackPointer) {
                // We need to up-convert it
                BlockTrackPointer currentPointer = (BlockTrackPointer) current.getBlock();
                BlockPos currentMaster = currentPointer.master(world, offset, current);
                TrackBehaviour behaviour = currentPointer.singleBehaviour(world, offset, current);
                if (behaviour == null) {
                    // Odd. But thats easy to deal with, just make our own version (as if this was an air block)
                    EnumOffset pointerOffset = ItemBlockSeperatedTrack.calculateOffsetTo(slave, slaves, BlockPos.ORIGIN);
                    IBlockState pointerState = TCBlocks.TRACK_POINTER.getBlock().getDefaultState();
                    pointerState = pointerState.withProperty(BlockTrackPointer.PROP_OFFSET, pointerOffset);
                    world.setBlockState(offset, pointerState);
                } else {
                    // We ACTUALLY need to add both together
                    TileTrackMultiplePointer multi = new TileTrackMultiplePointer();
                    multi.setWorldObj(world);
                    world.setBlockState(offset, TCBlocks.TRACK_MULTIPLE.getBlock().getDefaultState());
                    world.setTileEntity(offset, multi);
                    multi.addPointer(behaviourStateful);
                    if (behaviour instanceof TrackBehaviourStateful) {
                        multi.addPointer((TrackBehaviourStateful) behaviour);
                    } else {
                        TrackBehaviourNative behaviourNative = (TrackBehaviourNative) behaviour;
                        TrackBehaviourStateful stateful = behaviourNative.convertToStateful(world, currentMaster, world.getBlockState(currentMaster));
                        if (stateful == null) {// That should NEVER happen. We have an invalid track.
                            throw new IllegalStateException("The track " + behaviourNative.getClass() + " did not return a stateful instance!");
                        }
                        multi.addPointer(stateful);
                        world.setBlockState(currentMaster, TCBlocks.TRACK_MULTIPLE.getBlock().getDefaultState());
                        TileTrackMultiple replacement = new TileTrackMultiple();
                        replacement.setWorldObj(world);
                        replacement.addTrack(stateful);
                        world.setTileEntity(currentMaster, replacement);
                        // Icky. We MIGHT set off a chain recation and overflow the stack.
                        createPointers(world, stateful);
                    }
                }
            } else if (current.getBlock() instanceof BlockTrackPointerMultiple) {
                // We only need to add the given behaviour to the existing tile
                TileTrackMultiplePointer multi = (TileTrackMultiplePointer) world.getTileEntity(offset);
                multi.addPointer(behaviourStateful);
            } else {
                /* We should only have one of the above cases, so if we don't then either our logic is borked or we have
                 * some other odd senario. Either way, lets bail early */
                throw new IllegalStateException("Thats... not possible." + current);
            }
        }
    }

    @Override
    public boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state) {
        throw new AbstractMethodError("Implement this!");
    }
}
