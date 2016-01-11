package alexiil.mods.traincraft.api.component;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.*;
import alexiil.mods.traincraft.api.IRollingStock.Face;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public abstract class ComponentTrackFollower implements IComponent {
    private static final int DATA_WATCHER_FLAGS = 0;
    private static final int DATA_WATCHER_PATH_INDEX = 1;
    private static final int DATA_WATCHER_PROGRESS = 2;
    private static final int DATA_WATCHER_TRACK_POS = 3;

    private static final int FLAG_HAS_PATH = 1;
    private static final int FLAG_PATH_REVERSED = 2;

    private final IRollingStock stock;

    private ITrackPath currentPath;
    /** The progress accross the current path, in meters. */
    private double progress = 0;
    private double lastRecievedProgress = 0;

    private Vec3 lookVec = new Vec3(0, 0, 1), lastPlace = new Vec3(0, 0, 0);
    protected final int dataWatcherOffset, componentIndex;
    protected final double constructorOffset;

    public ComponentTrackFollower(IRollingStock stock, double offset, int componentIndex) {
        this.stock = stock;
        constructorOffset = lastRecievedProgress = progress = offset;
        this.componentIndex = componentIndex;
        this.dataWatcherOffset = EntityRollingStockBase.DATA_WATCHER_COMPONENT_START + componentIndex
            * EntityRollingStockBase.DATA_WATCHER_COMPONENT_STRIDE;
    }

    @Override
    public double originOffset() {
        return constructorOffset;
    }

    @Override
    public IRollingStock stock() {
        return stock;
    }

    @Override
    public Vec3 getTrackPos(float partialTicks) {
        if (currentPath == null) return lastPlace;
        if (partialTicks == 0) return lastPlace = currentPath.interpolate(progress / currentPath.length());
        return lastPlace = currentPath.interpolate((progress + partialTicks * stock.speed(Face.FRONT) / 20) / currentPath.length());
    }

    @Override
    public Vec3 getTrackDirection(float partialTicks) {
        if (currentPath == null) return lookVec;
        if (partialTicks == 0) return lookVec = currentPath.direction(progress / currentPath.length());
        return lookVec = currentPath.direction((progress + partialTicks * stock.speed(Face.FRONT) / 20) / currentPath.length());
    }

    @Override
    public void alignTo(ITrackPath around, double meters) {
        around = stock.getTrain().offsetPath(around, meters);
        currentPath = around;
        meters = stock.getTrain().offsetMeters(around, meters);
        progress = meters;

        Entity ent = (Entity) stock;

        int index = TrackPathProvider.pathIndex(ent.getEntityWorld(), currentPath);
        boolean reversed = TrackPathProvider.isPathReversed(ent.getEntityWorld(), currentPath);

        DataWatcher dataWatcher = ent.getDataWatcher();
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, currentPath.creatingBlock());
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, FLAG_HAS_PATH + (reversed ? FLAG_PATH_REVERSED : 0));
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, index);
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, (float) progress);
    }

    @Override
    public void tick() {
        Entity ent = (Entity) stock;
        double distanceMoved = stock.speed(Face.FRONT) / 20.0;
        DataWatcher dataWatcher = ent.getDataWatcher();
        World world = ent.getEntityWorld();

        if (world.isRemote) {
            // double speed = dataWatcher.getWatchableObjectFloat(DATA_WATCHER_SPEED);
            // speedMPT = speed;

            int flags = dataWatcher.getWatchableObjectInt(dataWatcherOffset + DATA_WATCHER_FLAGS);
            if ((flags & FLAG_HAS_PATH) == FLAG_HAS_PATH) {
                BlockPos currentTrack = MCObjectUtils.getWatchableObjectBlockPos(dataWatcher, dataWatcherOffset + DATA_WATCHER_TRACK_POS);
                int index = dataWatcher.getWatchableObjectInt(dataWatcherOffset + DATA_WATCHER_PATH_INDEX);

                ITrackPath[] paths = TrackPathProvider.getPathsAsArray(world, currentTrack, world.getBlockState(currentTrack));
                if (index >= paths.length || index < 0) {
                    // Something went wrong...
                    currentPath = null;
                    currentTrack = null;
                } else {
                    currentPath = paths[index];
                    if ((flags & FLAG_PATH_REVERSED) == FLAG_PATH_REVERSED) {
                        currentPath = currentPath.reverse();
                    }
                }
            } else {
                currentPath = null;
            }
            // Update the server side progress seperatly from the client side progress
            double prog = dataWatcher.getWatchableObjectFloat(dataWatcherOffset + DATA_WATCHER_PROGRESS);
            if (prog != lastRecievedProgress) {
                progress = prog;
                lastRecievedProgress = prog;
            } else if (currentPath != null) {
                // Only update it ourselves if we have gone a tick without seeing the server data
                double length = currentPath.length();
                progress += distanceMoved;
                lastPlace = currentPath.interpolate(progress / length);
                lookVec = currentPath.direction(progress / length);
            }
        } else {// Else if its the server world
            // getTrain().tick(this);
            // Find the best track path
            if (currentPath == null) {
                requestNextPath(dataWatcher, world);
            }

            // Use the current track path
            if (currentPath != null) {
                progress += distanceMoved;
                ITrackPath old = currentPath;
                currentPath = stock().getTrain().offsetPath(currentPath, progress);
                if (old != currentPath) {
                    progress = stock().getTrain().offsetMeters(old, progress);
                    if (currentPath != null) {
                        stock.getTrain().usePath(currentPath);
                        stock.getTrain().releasePath(old);
                    }
                }
            }
            if (currentPath != null) {
                lastPlace = currentPath.interpolate(progress / currentPath.length());

                lookVec = currentPath.direction(progress / currentPath.length());
                int index = TrackPathProvider.pathIndex(ent.getEntityWorld(), currentPath);
                boolean reversed = TrackPathProvider.isPathReversed(ent.getEntityWorld(), currentPath);

                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, currentPath.creatingBlock());
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, FLAG_HAS_PATH + (reversed ? FLAG_PATH_REVERSED : 0));
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, index);
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, (float) progress);
            } else {
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, new BlockPos(0, 0, 0));
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, 0);
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, -1);
                dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, (float) progress);
            }
        }
    }

    private void requestNextPath(DataWatcher dataWatcher, World world) {
        currentPath = TrainCraftAPI.MOVEMENT_MANAGER.closest(this, Face.FRONT);
    }
}
