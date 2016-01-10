package alexiil.mods.traincraft.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.IRollingStock.Face;
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;

public class Train {
    public final UUID uuid;
    public final ImmutableList<IRollingStock> parts;
    private long lastTick = -1;

    public Train(IRollingStock stock) {
        if (stock == null) throw new NullPointerException("stock");
        if (!(stock instanceof Entity)) throw new IllegalArgumentException(stock.getClass() + " was not an instanceof Entity!");
        uuid = UUID.randomUUID();
        parts = ImmutableList.of(stock);
        parts.forEach(p -> p.setTrain(this));
    }

    private Train(List<IRollingStock> stocks) {
        uuid = UUID.randomUUID();
        parts = ImmutableList.copyOf(stocks);
        parts.forEach(p -> p.setTrain(this));
    }

    /** Constructor used on the server (or integrated server) for loading trains from a save file. */
    Train(UUID uuid, List<IRollingStock> stock) {
        this.uuid = uuid;
        parts = ImmutableList.copyOf(stock);
        parts.forEach(p -> p.setTrain(this));
    }

    public static Train readFromNBT(NBTTagCompound nbt, World world) {
        long uuidMost = nbt.getLong("UUIDMost");
        long uuidLeast = nbt.getLong("UUIDLeast");
        UUID uuid = new UUID(uuidMost, uuidLeast);

        List<IRollingStock> stock = new ArrayList<>();

        Train t = new Train(uuid, stock);

        return t;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("UUIDMost", uuid.getMostSignificantBits());
        tag.setLong("UUIDLeast", uuid.getLeastSignificantBits());

        NBTTagList list = new NBTTagList();
        for (IRollingStock stock : parts) {
            NBTTagCompound comp = new NBTTagCompound();
            UUID uuid = ((Entity) stock).getUniqueID();
            tag.setLong("UUIDMost", uuid.getMostSignificantBits());
            tag.setLong("UUIDLeast", uuid.getLeastSignificantBits());
            list.appendTag(comp);
        }
        tag.setTag("stock", list);

        // World world = ((Entity) parts.get(0)).getEntityWorld();

        // NBTTagList paths = new NBTTagList();
        // for (int i = 0; i < this.trackPaths.size(); i++) {
        // BlockPos pos = trackPositions.get(i);
        // ITrackPath path = trackPaths.get(i);
        // IBlockState state = world.getBlockState(pos);
        // ITrackPath[] trackPaths = TrackPathProvider.getPathsFor(world, pos, state);
        // for (int j = 0; j < trackPaths.length; j++) {
        // if (trackPaths.equals(path)) {
        //
        // break;
        // }
        // }
        //
        // NBTTagCompound comp = new NBTTagCompound();
        // comp.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        // }
        // tag.setTag("paths", paths);

        return tag;
    }

    public List<IRollingStock> parts() {
        return parts;
    }

    // ###########################
    //
    // Mutations
    //
    // ###########################

    public void addInFront(Train train) {
        List<IRollingStock> whole = new ArrayList<>();
        whole.addAll(train.parts());
        whole.addAll(parts);
        Train newTrain = new Train(whole);
        TrainCraftAPI.WORLD_CACHE.createTrain(newTrain);

        TrainCraftAPI.WORLD_CACHE.deleteTrainIfUnused(train);
        TrainCraftAPI.WORLD_CACHE.deleteTrainIfUnused(this);
    }

    public void addInFront(IRollingStock stock) {
        addInFront(stock.getTrain());
    }

    public void addBehind(Train train) {
        train.addInFront(this);
    }

    public void addBehind(IRollingStock stock) {
        addBehind(stock.getTrain());
    }

    public void split(IRollingStock splitter, Face faceOfSplit) {
        int index = parts.indexOf(splitter);
        if (faceOfSplit == Face.FRONT) index++;
        if (index == 0 || index >= parts.size()) return;

        List<IRollingStock> before = parts.subList(0, index);
        List<IRollingStock> after = parts.subList(index, 0);
        TrainCraftAPI.WORLD_CACHE.createTrain(new Train(before));
        TrainCraftAPI.WORLD_CACHE.createTrain(new Train(after));
        TrainCraftAPI.WORLD_CACHE.deleteTrainIfUnused(this);
    }

    /** Disconnects all rolling stock from this train. */
    public void disband() {
        parts.forEach(p -> {
            Train t = new Train(p);
            p.setTrain(t);
            TrainCraftAPI.WORLD_CACHE.createTrain(t);
        });
        TrainCraftAPI.WORLD_CACHE.deleteTrainIfUnused(this);
    }

    // ###########################
    //
    // Speed changes
    //
    // ###########################

    /** Applies an amount of momentum to all components of the train. Also rebances all momentum around to make the
     * speeds equal. */
    public void applyMomentum(double newtons, Face face) {
        double totalMomentum = parts().stream().mapToDouble(s -> s.momentum(face)).sum();
        totalMomentum += newtons;
        int totalWeight = parts().stream().mapToInt(s -> s.weight()).sum();
        double speed = totalMomentum / totalWeight;
        parts().forEach(s -> s.setSpeed(speed, face));
    }

    public void applyBrakes(double maxNewtons) {
        Face face = Face.FRONT;
        double momentum = parts.stream().mapToDouble(p -> p.momentum(face)).sum();
        Face oppositeFace = Face.BACK;
        if (momentum < 0) {
            momentum = -momentum;
            oppositeFace = Face.FRONT;
        }
        if (momentum >= maxNewtons) {
            applyMomentum(maxNewtons, oppositeFace);
        } else {
            applyMomentum(momentum, oppositeFace);
        }
    }

    /** Ticks this train. For simplicity this is called by all of the {@link IRollingStock} stocks that exist
     * 
     * @param caller */
    public void tick(IRollingStock caller) {
        /* This ensures that this only ticks once per tick, and the first rolling stock to call this will have it ready
         * for them to use. */
        World world = ((Entity) caller).getEntityWorld();
        long worldTick = world.getTotalWorldTime();
        if (worldTick == lastTick) return;
        lastTick = worldTick;

        if (world.isRemote) {

        } else {
            computeMomentumChanges(caller);
        }
    }

    private void computeMomentumChanges(IRollingStock caller) {
        /* Work everything out related to the front of the train. */
        Face face = Face.FRONT;

        // First rebalance all the momentum around if not all of the speeds are equal
        double speed = caller.speed(face);
        for (int i = 0; i < parts.size(); i++)
            if (parts.get(i).speed(face) != speed) {
                // Applying no newtons just rebalances everything
                applyMomentum(0, face);
                speed = caller.speed(face);
                break;
            }

        // Sum up all forces by gravity
        double gravityForce = parts.stream().mapToDouble(p -> p.inclination(face) * p.weight()).sum();

        // Sum up all power going into motion (but ignore the engine if it is braking)
        double engineForce = parts.stream().mapToDouble(p -> p.isBraking() ? 0 : p.engineOutput(face)).sum();

        double totalForce = engineForce - gravityForce;

        applyMomentum(totalForce / 20.0, face);

        double resistance = parts.stream().mapToDouble(p -> p.resistance()).sum();
        double brakes = parts.stream().mapToDouble(p -> (p.isBraking()) ? p.maxBrakingForce() + Math.abs(p.engineOutput(face)) : 0).sum();

        double totalResistance = resistance + brakes;

        // We only apply one twentieth of the resistance as we are applying it per tick rather than per second.
        applyBrakes(totalResistance / 20.0);
    }

    // ###########################
    //
    // Path changes
    //
    // ###########################

    /** Computes the next path along from the given path in the given direction. For simplicities sake you can call this
     * from either the server or the client to get a path. */
    public ITrackPath requestNextTrackPath(ComponentTrackFollower caller, ITrackPath currentPath, Face direction) {
        if (currentPath != null) TrainCraftAPI.MOVEMENT_MANAGER.next(((Entity) caller.stock()).getEntityWorld(), currentPath);
        return TrainCraftAPI.MOVEMENT_MANAGER.closest(caller, direction);
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return uuid + ", " + (parts != null ? parts.subList(0, Math.min(parts.size(), maxLen)) : null) + ", " + lastTick;
    }
}
