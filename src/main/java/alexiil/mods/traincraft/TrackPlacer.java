package alexiil.mods.traincraft;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackPlacer;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.tile.TileTrackMultiple;

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

    private static boolean isUpgradableTrack(World world, BlockPos pos) {
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
        if (current.size() == 0) return false;
        return true;
    }

    private static TileTrackMultiple createMultiTile(World world, BlockPos pos) {
        TileTrackMultiple multi = new TileTrackMultiple();
        multi.setWorldObj(world);
        multi.setPos(pos);
        world.setBlockState(pos, TCBlocks.TRACK_MULTIPLE.getBlock().getDefaultState());
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

    @Override
    public void placeSlaves(TrackBehaviour behaviour, World world, BlockPos pos) {
        Set<BlockPos> slaves = behaviour.getSlaveOffsets(world, pos, world.getBlockState(pos));

        for (BlockPos slave : slaves) {
            if (canReplaceSlave(world, pos.add(slave))) replaceSlave(behaviour, world, pos.add(slave));
        }
    }

    @Override
    public boolean canPlaceSlaves(TrackBehaviour behaviour, World world, BlockPos pos) {
        Set<BlockPos> slaves = behaviour.getSlaveOffsets(world, pos, world.getBlockState(pos));

        for (BlockPos slave : slaves) {
            if (!canReplaceSlave(world, pos.add(slave))) return false;
        }
        return true;
    }

    private boolean canReplaceSlave(World world, BlockPos pos) {

        return true;
    }

    private void replaceSlave(TrackBehaviour behaviour, World world, BlockPos pos) {

    }

    @Override
    public boolean removeTrack(TrackBehaviour toRemove, World world, BlockPos pos, IBlockState state) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");
    }
}
