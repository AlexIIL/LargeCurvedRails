package alexiil.mods.traincraft.api.component;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrackPathProvider;
import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.api.lib.MCObjectUtils;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.AlignmentFailureException;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.api.train.IRollingStock.Face;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class ComponentTrackFollower implements IComponentOuter {
    // Each component uses: [ int (flag), float (progress), blockpos (track), int (track index)]
    public static final int DATA_WATCHER_COMPONENT_START = 6;
    public static final int DATA_WATCHER_COMPONENT_STRIDE = 4;

    private static final int DATA_WATCHER_FLAGS = 0;
    private static final int DATA_WATCHER_PATH_INDEX = 1;
    private static final int DATA_WATCHER_PROGRESS = 2;
    private static final int DATA_WATCHER_TRACK_POS = 3;

    private static final int FLAG_HAS_PATH = 1;
    private static final int FLAG_PATH_REVERSED = 2;

    private final IRollingStock stock;
    private IComponentOuter parent;

    private BehaviourWrapper currentPath;
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
        this.dataWatcherOffset = DATA_WATCHER_COMPONENT_START + componentIndex * DATA_WATCHER_COMPONENT_STRIDE;
    }

    @Override
    public IComponentOuter parent() {
        return parent;
    }

    @Override
    public void setParent(IComponentOuter parent) {
        this.parent = parent;
    }

    @Override
    public List<IComponentOuter> children() {
        return Collections.emptyList();
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
        return lastPlace = currentPath.interpolate((progress + partialTicks * stock.speed() / 20) / currentPath.length());
    }

    @Override
    public Vec3 getTrackDirection(float partialTicks) {
        if (currentPath == null) return lookVec;
        if (partialTicks == 0) return lookVec = currentPath.direction(progress / currentPath.length());
        return lookVec = currentPath.direction((progress + partialTicks * stock.speed() / 20) / currentPath.length());
    }

    @Override
    public List<IComponentInner> innerComponents() {
        return Collections.emptyList();
    }

    @Override
    public void alignTo(BehaviourWrapper around, double meters, boolean simulate) throws AlignmentFailureException {
        around = stock.pathFinder().offsetPath(around, meters);
        if (around == null) throw new AlignmentFailureException();
        currentPath = around;
        stock().pathFinder().usePath(currentPath, this);
        meters = stock.pathFinder().offsetMeters(around, meters);
        progress = meters;

        Entity ent = (Entity) stock;

        TrackIdentifier ident = currentPath.getIdentifier();

        ByteBuf buffer = Unpooled.buffer();
        ident.serializeBuf(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append((char) b);
        }
        
        // FIMXE: OMFG THIS WILL BE SOOO HACKY!

        TrainCraft.trainCraftLog.info("STRING: \"" + builder.toString() + "\"");

        int index = 0;
        boolean reversed = false;

        DataWatcher dataWatcher = ent.getDataWatcher();
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_TRACK_POS, currentPath.creatingBlock());
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_FLAGS, FLAG_HAS_PATH + (reversed ? FLAG_PATH_REVERSED : 0));
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PATH_INDEX, index);
        dataWatcher.updateObject(dataWatcherOffset + DATA_WATCHER_PROGRESS, (float) progress);
    }

    @Override
    public void tick() {
        Entity ent = (Entity) stock;
        double distanceMoved = stock.speed() / 20.0;
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
                progress += distanceMoved;
                currentPath = stock().pathFinder().offsetPath(currentPath, progress);
                if (currentPath != null) {
                    progress = stock().pathFinder().offsetMeters(currentPath, progress);

                    double length = currentPath.length();
                    lastPlace = currentPath.interpolate(progress / length);
                    lookVec = currentPath.direction(progress / length);
                }
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
                currentPath = stock().pathFinder().offsetPath(currentPath, progress);
                if (old != currentPath) {
                    progress = stock().pathFinder().offsetMeters(old, progress);
                    if (currentPath != null) {
                        stock.pathFinder().usePath(currentPath, this);
                        stock.pathFinder().releasePath(old, this);
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

    public abstract double frictionCoefficient();

    @Override
    public double resistance() {
        double frictionCoefficient = frictionCoefficient();
        double groundFriction = frictionCoefficient * weight() * (1 - Math.abs(inclination()));
        double frontArea = frontArea();
        double airDrag = Math.abs(stock().speed());
        airDrag *= airDrag * frontArea;
        return groundFriction + airDrag;
    }
}
