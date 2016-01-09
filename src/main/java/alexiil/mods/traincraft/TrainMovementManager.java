package alexiil.mods.traincraft;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.*;
import alexiil.mods.traincraft.api.IRollingStock.Face;

public enum TrainMovementManager implements ITrainMovementManager {
    INSTANCE;

    @Override
    public ITrackPath next(IBlockAccess access, ITrackPath from) {
        Vec3 end = from.end();
        BlockPos toTry = new BlockPos(end);
        ITrackPath goodPath = next(access, toTry, from);
        if (goodPath != null) return goodPath;
        // Next try all the blocks around the end...
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos nextTry = toTry.offset(face);
            goodPath = next(access, nextTry, from);
            if (goodPath != null) return goodPath;
        }
        return null;
    }

    private ITrackPath next(IBlockAccess access, BlockPos toTry, ITrackPath from) {
        IBlockState state = access.getBlockState(toTry);
        ITrackBlock block = TrackPathProvider.getBlockFor(access, toTry, state);
        if (block == null) return null;
        ITrackPath[] paths = block.paths(access, toTry, state);
        if (paths.length == 0) return null;
        for (ITrackPath path : paths) {
            /* If the end of the gotten path is the same as our end then try reversing it? */
            if (path.end().distanceTo(from.end()) == 0) path = path.reverse();
            /* If the ends and starts of our path and the gotten path are both the same then we have the reverse of our
             * own path */
            if (path.end().distanceTo(from.start()) == 0 && path.start().distanceTo(from.end()) == 0) return null;
            Vec3 direction = from.direction(1);
            Vec3 nextDirection = path.direction(0);
            // If the directions of both paths are roughly equal then we have found a good path
            if (direction.distanceTo(nextDirection) < 1e-3) return path;
        }
        return null;
    }

    @Override
    public ITrackPath closest(IRollingStock caller, Face direction) {
        Vec3 current = caller.getPathPosition();
        BlockPos toTry = new BlockPos(current);
        Vec3 entDir = caller.getPathDirection(direction);
        ITrackPath goodPath = closest(((Entity) caller).getEntityWorld(), toTry, current, entDir);
        if (goodPath != null) return goodPath;
        // Next try all the blocks around the end...
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos nextTry = toTry.offset(face);
            goodPath = closest(((Entity) caller).getEntityWorld(), nextTry, current, entDir);
            if (goodPath != null) return goodPath;
        }
        return null;
    }

    private ITrackPath closest(IBlockAccess access, BlockPos toTry, Vec3 current, Vec3 direction) {
        IBlockState state = access.getBlockState(toTry);
        ITrackBlock block = TrackPathProvider.getBlockFor(access, toTry, state);
        if (block == null) return null;
        ITrackPath[] paths = block.paths(access, toTry, state);
        if (paths.length == 0) return null;
        for (ITrackPath path : paths) {

            // TODO: Make this find *a* path. I don't care right now, we can put more effort into this search later
            if (path.start().distanceTo(current) < 0.1) {
                if (path.direction(0).distanceTo(direction) < 1) return path;
            }
            if (path.end().distanceTo(current) < 0.1) {
                if (path.reverse().direction(0).distanceTo(direction) < 1) return path.reverse();
            }
        }
        return null;
    }
}
