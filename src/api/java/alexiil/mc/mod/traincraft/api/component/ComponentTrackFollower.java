package alexiil.mc.mod.traincraft.api.component;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import alexiil.mc.mod.traincraft.TrackPathProvider;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.train.AlignmentFailureException;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;
import alexiil.mc.mod.traincraft.network.MessageHandler;
import alexiil.mc.mod.traincraft.network.MessageUpdateTrackLocation;

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

    private BehaviourWrapper currentTrack;
    private ITrackPath currentPath;
    /** The progress accross the current path, in meters. */
    private double progress = 0;
    private boolean usingReverse = false, justUpdated = false;

    private Vec3d lookVec = new Vec3d(0, 0, 1), lastPlace = new Vec3d(0, 0, 0);
    public final int componentIndex;
    protected final double constructorOffset;

    public ComponentTrackFollower(IRollingStock stock, double offset, int componentIndex) {
        this.stock = stock;
        constructorOffset = progress = offset;
        this.componentIndex = componentIndex;
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
    public Vec3d getTrackPos(float partialTicks) {
        if (currentPath == null) return lastPlace;
        if (partialTicks == 0) return lastPlace = currentPath.interpolate(progress / currentPath.length());
        return lastPlace = currentPath.interpolate((progress + partialTicks * stock.speed() / 20) / currentPath.length());
    }

    @Override
    public Vec3d getTrackDirection(float partialTicks) {
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
        currentTrack = around;
        stock().pathFinder().usePath(currentTrack, this);
        meters = stock.pathFinder().offsetMeters(around, meters);
        progress = meters;
        currentPath = currentTrack.getPath();

        Entity ent = (Entity) stock;

        TrackIdentifier ident = currentTrack.getIdentifier();

        if (usingReverse != ident.isReversed()) ident = ident.reverse();
        MessageUpdateTrackLocation update = new MessageUpdateTrackLocation(ent.getEntityId(), componentIndex, ident, 0);
        TargetPoint point = new TargetPoint(ent.getEntityWorld().provider.getDimensionId(), (int) ent.posX, (int) ent.posY, (int) ent.posZ, 9 * 16);
        MessageHandler.INSTANCE.getWrapper().sendToAllAround(update, point);
    }

    public void receiveMessageUpdateTrackLocation(TrackIdentifier ident, float progress) {
        currentTrack = TrackPathProvider.INSTANCE.getTrackForIdent(((Entity) stock()).getEntityWorld(), ident);
        this.progress = progress;
        justUpdated = true;
    }

    @Override
    public void tick() {
        Entity ent = (Entity) stock;
        double distanceMoved = stock.speed() / 20.0;
        DataWatcher dataWatcher = ent.getDataWatcher();
        World world = ent.getEntityWorld();

        if (world.isRemote) {
            // Update the server side progress seperatly from the client side progress
            if (justUpdated) {
                justUpdated = false;
            } else if (currentTrack != null) {
                // Only update it ourselves if we have gone a tick without seeing the server data
                progress += distanceMoved;
                currentTrack = stock().pathFinder().offsetPath(currentTrack, progress);
                if (currentTrack != null) {
                    progress = stock().pathFinder().offsetMeters(currentTrack, progress);
                    currentPath = currentTrack.getPath();

                    double length = currentPath.length();
                    lastPlace = currentPath.interpolate(progress / length);
                    lookVec = currentPath.direction(progress / length);
                }
            }
        } else {// Else if its the server world
                // getTrain().tick(this);
                // Find the best track path
            if (currentTrack == null) {
                requestNextPath(dataWatcher, world);
            }

            // Use the current track path
            if (currentTrack != null) {
                progress += distanceMoved;
                BehaviourWrapper old = currentTrack;
                currentTrack = stock().pathFinder().offsetPath(currentTrack, progress);
                if (old != currentTrack) {
                    progress = stock().pathFinder().offsetMeters(old, progress);
                    if (currentTrack != null) {
                        stock.pathFinder().usePath(currentTrack, this);
                        stock.pathFinder().releasePath(old, this);
                    }
                }
            }
            if (currentTrack != null) {
                currentPath = currentTrack.getPath();
                lastPlace = currentPath.interpolate(progress / currentPath.length());

                lookVec = currentPath.direction(progress / currentPath.length());

                TrackIdentifier ident = currentTrack.getIdentifier();
                if (usingReverse != ident.isReversed()) ident = ident.reverse();
                MessageUpdateTrackLocation update = new MessageUpdateTrackLocation(ent.getEntityId(), componentIndex, ident, 0);
                TargetPoint point = new TargetPoint(world.provider.getDimensionId(), (int) ent.posX, (int) ent.posY, (int) ent.posZ, 9 * 16);
                MessageHandler.INSTANCE.getWrapper().sendToAllAround(update, point);
            } else {
                MessageUpdateTrackLocation update = new MessageUpdateTrackLocation(ent.getEntityId(), componentIndex, null, 0);
                TargetPoint point = new TargetPoint(world.provider.getDimensionId(), (int) ent.posX, (int) ent.posY, (int) ent.posZ, 9 * 16);
                MessageHandler.INSTANCE.getWrapper().sendToAllAround(update, point);
            }
        }

    }

    private void requestNextPath(DataWatcher dataWatcher, World world) {
        // FIXME: either fix this or destroy the train.
        // currentPath = TrainCraftAPI.MOVEMENT_MANAGER.closest(this, Face.FRONT);
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
