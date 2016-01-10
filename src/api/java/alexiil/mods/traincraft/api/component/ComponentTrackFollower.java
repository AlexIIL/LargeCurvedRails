package alexiil.mods.traincraft.api.component;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.IRollingStock.Face;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.MCObjectUtils;
import alexiil.mods.traincraft.api.TrackPathProvider;
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
        if (partialTicks == 0) return lastPlace = currentPath.interpolate(progress);
        return lastPlace = currentPath.interpolate(progress + partialTicks * stock.speed(Face.FRONT) / currentPath.length());
    }

    @Override
    public Vec3 getTrackDirection(float partialTicks) {
        if (currentPath == null) return lookVec;
        if (partialTicks == 0) return lookVec = currentPath.direction(progress);
        return lookVec = currentPath.direction(progress + partialTicks * stock.speed(Face.FRONT) / currentPath.length());
    }

    @Override
    public void alignTo(Vec3 position, Vec3 direction, ITrackPath path) {
        lastPlace = path.interpolate(0);
        lookVec = path.direction(0);
        currentPath = stock.getTrain().requestNextTrackPath(this, null, Face.FRONT);
        lastPlace = position;
        progress = currentPath.progress(lastPlace);
        Entity ent = (Entity) stock;
        DataWatcher dataWatcher = ent.getDataWatcher();
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, currentPath.creatingBlock());
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, FLAG_HAS_PATH);
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, 0);
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

            // Update the server side progress seperatly from the client side progress
            double prog = dataWatcher.getWatchableObjectFloat(dataWatcherOffset + DATA_WATCHER_PROGRESS);
            if (prog != lastRecievedProgress) {
                progress = prog;
                lastRecievedProgress = prog;
            }

            int flags = dataWatcher.getWatchableObjectInt(dataWatcherOffset + DATA_WATCHER_FLAGS);
            if ((flags & FLAG_HAS_PATH) == FLAG_HAS_PATH) {
                BlockPos currentTrack = MCObjectUtils.getWatchableObjectBlockPos(dataWatcher, dataWatcherOffset + DATA_WATCHER_TRACK_POS);
                int index = dataWatcher.getWatchableObjectInt(dataWatcherOffset + DATA_WATCHER_PATH_INDEX);

                ITrackPath[] paths = TrackPathProvider.getPathsFor(world, currentTrack, world.getBlockState(currentTrack));
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

            if (currentPath != null) {
                double length = currentPath.length();
                progress += distanceMoved / length;
                lastPlace = currentPath.interpolate(progress);
                lookVec = currentPath.direction(progress);
            }
        } else {// Else if its the server world
            // getTrain().tick(this);
            // Find the best track path
            if (currentPath == null) {
                requestNextPath(dataWatcher, world);
            }

            // Use the current track path
            if (currentPath != null) {
                double length = currentPath.length();
                progress += distanceMoved / length;
                if (progress > 1) {
                    Vec3 newPos = currentPath.interpolate(1.001);
                    lastPlace = newPos;
                    // Because we are off the path, lets just reset it to 0 and pretend nothing happened
                    // TODO: Past paths! And future paths!
                    progress = progress - 1;
                    requestNextPath(dataWatcher, world);
                } else {
                    lastPlace = currentPath.interpolate(progress);

                    lookVec = currentPath.direction(progress);
                    dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, (float) progress);
                }
            }
        }
    }

    private void requestNextPath(DataWatcher dataWatcher, World world) {
        currentPath = stock.getTrain().requestNextTrackPath(this, currentPath, Face.FRONT);

        if (currentPath == null) {
            dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, 0);
        } else {
            int index = -1;
            boolean reversed = false;

            BlockPos pos = currentPath.creatingBlock();
            ITrackPath[] arr = TrackPathProvider.getPathsFor(world, pos, world.getBlockState(pos));
            for (int i = 0; i < arr.length; i++) {
                if (currentPath.equals(arr[i])) {
                    index = i;
                    break;
                } else if (currentPath.equals(arr[i].reverse())) {
                    index = i;
                    reversed = true;
                    break;
                }
            }
            dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, currentPath.creatingBlock());
            dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, FLAG_HAS_PATH + (reversed ? FLAG_PATH_REVERSED : 0));
            dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, index);
            dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, 0f);
        }
    }
}
