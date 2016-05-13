package alexiil.mc.mod.traincraft;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.ITrackPlacer;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer.EnumOffset;
import alexiil.mc.mod.traincraft.block.TCBlocks;
import alexiil.mc.mod.traincraft.tile.TileTrackMultiple;

public enum TrackPlacer implements ITrackPlacer {
    INSTANCE;

    @Override
    public boolean tryPlaceTrack(TrackBehaviourStateful behaviour, World world, BlockPos pos) {
        if (isMultiTile(world, pos)) return addToMultiTile(behaviour, world, pos);
        else if (isReplacable(world, pos)) return setMultiTile(behaviour, world, pos);
        else if (isUpgradableTrack(world, pos)) {
            if (!upgradeToMultiTile(world, pos)) return false;
            return addToMultiTile(behaviour, world, pos);
        }
        return false;
    }

    private static boolean isMultiTile(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != TCBlocks.TRACK_MULTIPLE.getBlock()) return false;
        return world.getTileEntity(pos) instanceof TileTrackMultiple;
    }

    /** Upgrades the block from a native track to a multi-track tile entity. If their were no tracks in the block space
     * then this will do {@link #createMultiTile(World, BlockPos)} inefficiently. */
    private static boolean upgradeToMultiTile(World world, BlockPos pos) {
        // This might lose existing pointers?
        List<TrackBehaviourStateful> current = TrackPathProvider.INSTANCE.getTracksAsStream(world, pos, world.getBlockState(pos))
                // Make sure we only get the behaviors that originate from this block
                .filter(t -> t.getIdentifier().pos().equals(pos))
                // We only want stateful versions
                .map(t -> {
                    if (t.behaviour() instanceof TrackBehaviourStateful) return (TrackBehaviourStateful) t.behaviour();
                    return ((TrackBehaviourNative) t.behaviour()).convertToStateful(world, pos, world.getBlockState(pos));
                })
                // Coolect them as a list rather than a set as its simpler.
                .collect(Collectors.toList());
        if (current.contains(null)) return false;
        TileTrackMultiple multi = createMultiTile(world, pos);
        current.forEach(t -> multi.addTrack(t));
        return true;
    }

    /** @return True if the block at the position could be replaced with a set of tracks. */
    private static boolean isReplacable(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    private static boolean setMultiTile(TrackBehaviourStateful behaviour, World world, BlockPos pos) {
        TileTrackMultiple multi = createMultiTile(world, pos);
        if (multi == null) return false;
        multi.addTrack(behaviour);
        return true;
    }

    @Override
    public boolean isUpgradableTrack(World world, BlockPos pos) {
        List<TrackBehaviourStateful> current = TrackPathProvider.INSTANCE.getTracksAsStream(world, pos, world.getBlockState(pos))
                // Make sure we only get the behaviors that originate from this block
                .filter(t -> t.getIdentifier().pos().equals(pos))
                // We only want stateful versions
                .map(t -> {
                    if (t.behaviour() instanceof TrackBehaviourStateful) return (TrackBehaviourStateful) t.behaviour();
                    return ((TrackBehaviourNative) t.behaviour()).convertToStateful(world, pos, world.getBlockState(pos));
                })
                // Collect them as a list rather than a set as its simpler.
                .collect(Collectors.toList());
        if (current.contains(null)) return false;
        if (current.size() == 0) return false;
        return true;
    }

    private static TileTrackMultiple createMultiTile(World world, BlockPos pos) {
        TileTrackMultiple multi = new TileTrackMultiple();
        multi.setWorldObj(world);
        multi.setPos(pos);
        world.setBlockState(pos, TCBlocks.TRACK_MULTIPLE.getBlock().getDefaultState(), 2);
        world.setTileEntity(pos, multi);
        return multi;
    }

    private static boolean addToMultiTile(TrackBehaviourStateful behaviour, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple multi = (TileTrackMultiple) tile;
            return multi.addTrack(behaviour);
        }
        return false;
    }

    private static void addPointerToMultiTile(BehaviourWrapper wrapped, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple multi = (TileTrackMultiple) tile;
            multi.addPointerToTrack(wrapped);
        }
    }

    @Override
    public void placeSlaves(TrackBehaviour behaviour, World world, BlockPos pos) {
        Set<BlockPos> slaves = behaviour.getSlaveOffsets(world, pos, world.getBlockState(pos));
        Set<BlockPos> slavesCopy = ImmutableSet.copyOf(slaves);

        for (BlockPos slave : slaves) {
            if (slave.equals(BlockPos.ORIGIN)) continue;
            if (canReplaceSlave(world, pos.add(slave)) == null) {
                replaceSlave(behaviour, world, pos, pos.add(slave), slavesCopy);
            }
        }
    }

    @Override
    public EnumTrackRequirement checkSlaves(Set<BlockPos> slaves, World world, BlockPos pos) {
        for (BlockPos slave : slaves) {
            if (slave.equals(BlockPos.ORIGIN)) continue;
            EnumTrackRequirement req = canReplaceSlave(world, pos.add(slave));
            if (req != null) return req;
        }
        return null;
    }

    private static EnumTrackRequirement canReplaceSlave(World world, BlockPos slavePos) {
        if (!world.isAirBlock(slavePos.up())) return EnumTrackRequirement.SPACE_ABOVE;
        if (!world.isSideSolid(slavePos.down(), EnumFacing.UP)) return EnumTrackRequirement.GROUND_BELOW;
        if (isUpgradableSlaveTile(world, slavePos)) return null;
        if (isMultiTile(world, slavePos)) return null;
        if (world.getBlockState(slavePos).getBlock().isReplaceable(world, slavePos)) return null;
        return EnumTrackRequirement.OTHER;
    }

    private static void replaceSlave(TrackBehaviour behaviour, World world, BlockPos behaviourPos, BlockPos slavePos, Set<BlockPos> allSlaves) {
        if (isUpgradableSlaveTile(world, slavePos)) {
            upgradeSlaveTile(world, slavePos);
        }
        if (isMultiTile(world, slavePos)) {
            addPointerToMultiTile(new BehaviourWrapper(behaviour, world, behaviourPos), world, slavePos);
        } else if (world.getBlockState(slavePos).getBlock().isReplaceable(world, slavePos)) {
            EnumOffset offset = calculateOffsetTo(slavePos, allSlaves, behaviourPos);
            createPointerBlock(offset, world, slavePos);
        }
    }

    private static boolean isUpgradableSlaveTile(World world, BlockPos slavePos) {
        Block block = world.getBlockState(slavePos).getBlock();
        return block == TCBlocks.TRACK_POINTER.getBlock();
    }

    private static void upgradeSlaveTile(World world, BlockPos slavePos) {
        IBlockState state = world.getBlockState(slavePos);
        BlockTrackPointer pointer = (BlockTrackPointer) state.getBlock();
        BehaviourWrapper wrapper = pointer.singleBehaviour(world, slavePos, state);
        createMultiTile(world, slavePos);
        if (wrapper != null) {
            // Odd.
            addPointerToMultiTile(wrapper, world, slavePos);
        }
    }

    private static void createPointerBlock(EnumOffset offset, World world, BlockPos slavePos) {
        IBlockState state = TCBlocks.TRACK_POINTER.getBlock().getDefaultState();
        state = state.withProperty(BlockTrackPointer.PROP_OFFSET, offset);
        world.setBlockState(slavePos, state, 2);
    }

    private static EnumOffset calculateOffsetTo(BlockPos from, Collection<BlockPos> via, BlockPos to) {
        from = from.subtract(to);
        double bestOffsetL = Double.MAX_VALUE;
        EnumOffset bestOffset = null;

        for (BlockPos p : via) {
            for (EnumOffset o : EnumOffset.values()) {
                BlockPos offset = from.add(o.offset);
                if (!offset.equals(p)) continue;
                double dist = offset.distanceSq(BlockPos.ORIGIN);
                if (dist < bestOffsetL) {
                    bestOffset = o;
                    bestOffsetL = dist;
                }
            }
        }
        if (bestOffset == null) throw new IllegalStateException("Did not find a good offset for " + from + " via " + via);
        return bestOffset;
    }

    @Override
    public boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }
}
