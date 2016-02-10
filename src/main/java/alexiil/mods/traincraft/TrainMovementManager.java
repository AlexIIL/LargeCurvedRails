package alexiil.mods.traincraft;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.IRollingStock.Face;
import alexiil.mods.traincraft.api.ITrainMovementManager;
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.track.ITrackBlock;
import alexiil.mods.traincraft.api.track.ITrackPath;
import alexiil.mods.traincraft.api.track.TrackPathProvider;

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
            if (goodPath != null) {
                TrainCraft.trainCraftLog.info("TrainMovementManager::next | Returning the good path " + goodPath.start() + " -> " + goodPath.end());
                return goodPath;
            }
        }
        return null;
    }

    private ITrackPath next(IBlockAccess access, BlockPos toTry, ITrackPath from) {
        IBlockState state = access.getBlockState(toTry);
        ITrackBlock block = TrackPathProvider.getBlockFor(state);
        if (block == null) return null;
        ITrackPath[] paths = block.paths(access, toTry, state);
        if (paths.length == 0) return null;
        TrainCraft.trainCraftLog.info("TrainMovementManager::next | Comparing " + paths.length + " possible paths");
        for (ITrackPath path : paths) {
            for (ITrackPath p2 : new ITrackPath[] { path, path.reverse() }) {
                /* If the ends and starts of our path and the gotten path are both the same then we have the reverse of
                 * our own path */
                if (p2.end().distanceTo(from.start()) == 0 && p2.start().distanceTo(from.end()) == 0) continue;
                // If the new path is further away than it should be then try a different path
                if (p2.start().distanceTo(from.end()) != 0) continue;
                // Strange behaviour with corners?
                Vec3 direction = from.direction(1);
                Vec3 nextDirection = p2.direction(0);
                TrainCraft.trainCraftLog.info("TrainMovementManager::next | Comparing two paths with directions of " + direction + " and "
                    + nextDirection);
                /* If the directions of both paths are roughly equal (and this is VERY rough) then we have found a good
                 * path */
                if (direction.distanceTo(nextDirection) <= 1.2) return p2;
            }
        }// TODO: IS ITrackPath (And Train) a good abstraction? Its not working very well atm... :(
        TrainCraft.trainCraftLog.info("TrainMovementManager::next | None of the paths were good!");
        return null;
    }

    @Override
    public ITrackPath closest(ComponentTrackFollower caller, Face direction) {
        Vec3 current = caller.getTrackPos();
        BlockPos toTry = new BlockPos(current);
        Vec3 entDir = caller.getTrackDirection();
        ITrackPath goodPath = closest(((Entity) caller.stock()).getEntityWorld(), toTry, current, entDir);
        if (goodPath != null) return goodPath;
        // Next try all the blocks around the end...
        for (EnumFacing face : EnumFacing.VALUES) {
            BlockPos nextTry = toTry.offset(face);
            goodPath = closest(((Entity) caller.stock()).getEntityWorld(), nextTry, current, entDir);
            if (goodPath != null) return goodPath;
        }
        return null;
    }

    private ITrackPath closest(IBlockAccess access, BlockPos toTry, Vec3 current, Vec3 direction) {
        IBlockState state = access.getBlockState(toTry);
        ITrackBlock block = TrackPathProvider.getBlockFor(state);
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
