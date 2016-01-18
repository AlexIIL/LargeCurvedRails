package alexiil.mods.traincraft.api;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.IRollingStock.Face;

public class Train {
    public final UUID uuid;
    public final Map<ITrackPath, PathNode> paths = new HashMap<>();
    public final ImmutableList<IRollingStock> parts;
    private long lastTick = -1;

    public Train(IRollingStock stock) {
        this(stock, getTrack(stock));
    }

    private static ITrackPath getTrack(IRollingStock stock) {
        // Get the best path for the stock
        return null;
    }

    public Train(IRollingStock stock, ITrackPath path) {
        if (stock == null) throw new NullPointerException("stock");
        if (!(stock instanceof Entity)) throw new IllegalArgumentException(stock.getClass() + " was not an instanceof Entity!");
        uuid = UUID.randomUUID();
        parts = ImmutableList.of(stock);
        parts.forEach(p -> p.setTrain(this));
        paths.put(path, new PathNode(path));
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
    public void applyMomentum(double newtons) {
        double totalMomentum = parts().stream().mapToDouble(s -> s.momentum()).sum();
        totalMomentum += newtons;
        int totalWeight = parts().stream().mapToInt(s -> s.weight()).sum();
        double speed = totalMomentum / totalWeight;
        parts().forEach(s -> s.setSpeed(speed));
    }

    public void applyBrakes(double maxNewtons) {
        double momentum = parts.stream().mapToDouble(p -> p.momentum()).sum();
        double speed = parts.get(0).speed();
        maxNewtons = Math.abs(maxNewtons);
        if (Math.abs(momentum) >= maxNewtons) {
            applyMomentum(speed < 0 ? maxNewtons : -maxNewtons);
        } else {
            applyMomentum(-momentum);
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
        // First rebalance all the momentum around if not all of the speeds are equal
        double speed = caller.speed();
        for (int i = 0; i < parts.size(); i++)
            if (parts.get(i).speed() != speed) {
                // Applying 0 newtons just rebalances everything
                applyMomentum(0);
                speed = caller.speed();
                break;
            }

        // Sum up all forces by gravity
        double gravityForce = parts.stream().mapToDouble(p -> p.inclination() * p.weight()).sum();
        // If we are going forwards then make gravity go the other way around
        // if (speed > 0) gravityForce *= -1;

        // Sum up all power going into motion (but ignore the engine if it is braking)
        double engineForce = parts.stream().mapToDouble(p -> p.isBraking() ? 0 : p.engineOutput()).sum();

        double totalForce = engineForce - gravityForce;

        applyMomentum(totalForce / 20.0);

        double resistance = parts.stream().mapToDouble(p -> p.resistance()).sum();

        double brakes = parts.stream().mapToDouble(p -> (p.isBraking()) ? p.maxBrakingForce() + Math.abs(p.engineOutput()) : 0).sum();

        double totalResistance = resistance + brakes;

        // We only apply one twentieth of the resistance as we are applying it per tick rather than per second.
        applyBrakes(totalResistance / 20.0);

        // TrainCraft.trainCraftLog.info("S = " + (int) speed + ", G = " + (int) gravityForce + ", E = " + (int)
        // engineForce + ", TF = "
        // + (int) totalForce + ", R = " + (int) resistance + ", B = " + (int) brakes + ", TR =" + (int)
        // totalResistance);
    }

    // ###########################
    //
    // Path changes
    //
    // ###########################

    private World world() {
        return ((Entity) parts.get(0)).getEntityWorld();
    }

    private Stream<ITrackPath> findPaths(Vec3 attachPoint, int pos, Vec3 direction) {
        Builder<ITrackPath> paths = Stream.builder();
        World world = world();
        BlockPos toTry = new BlockPos(attachPoint);

        TrackPathProvider.getPathsAsStream(world, toTry, world.getBlockState(toTry)).forEach(p -> paths.add(p));
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos offset = toTry.offset(face);
            TrackPathProvider.getPathsAsStream(world, offset, world.getBlockState(offset)).forEach(p -> paths.add(p));
            for (EnumFacing face2 : EnumFacing.values()) {
                BlockPos offset2 = offset.offset(face2);
                TrackPathProvider.getPathsAsStream(world, offset2, world.getBlockState(offset2)).forEach(p -> paths.add(p));
            }
        }
        // @formatter:off
        return paths.build()
                .flatMap(p -> Arrays.asList(p, p.reverse()).stream())
                .filter(p -> p.interpolate(pos).distanceTo(attachPoint) == 0)
                .filter(p -> p.direction(pos).distanceTo(direction) <= 1.2);
    }

    private Stream<ITrackPath> findPathsForward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 0, direction);
    }

    private Stream<ITrackPath> findPathsBackward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 1, direction);
    }

    private ITrackPath findNextForward(ITrackPath path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.forward != null) return node.forward;
        Vec3 nextStart = path.end();
        Vec3 direction = path.direction(1);
        ITrackPath next = findPathsForward(nextStart, direction).findFirst().orElse(null);
        if (next == null) return null;
        node.forward = next;
        PathNode nextNode = new PathNode(next);
        nextNode.back = path;
        paths.put(next, nextNode);
        return next;
    }

    private ITrackPath findNextBackwards(ITrackPath path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.back != null) return node.back;
        Vec3 nextEnd = path.start();
        Vec3 direction = path.direction(0);
        ITrackPath next = findPathsBackward(nextEnd, direction).findFirst().orElse(null);
        if (next == null) return null;
        node.back = next;
        PathNode nextNode = new PathNode(next);
        nextNode.forward = path;
        paths.put(next, nextNode);
        return next;
    }

    public ITrackPath offsetPath(ITrackPath from, double meters) {
        if (meters >= 0 && meters <= from.length()) return from;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return null;
            meters += from.length();
        }
        while (meters > 1) {
            meters -= from.length();
            from = findNextForward(from);
            if (from == null) return null;
        }
        return from;
    }

    public double offsetMeters(ITrackPath from, double meters) {
        if (meters >= 0 && meters <= from.length()) return meters;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return meters;
            meters += from.length();
        }
        while (meters > from.length()) {
            meters -= from.length();
            from = findNextForward(from);
            if (from == null) return meters;
        }
        return meters;
    }

    public void usePath(ITrackPath path) {
        paths.get(path).uses++;
    }

    public void releasePath(ITrackPath path) {
        PathNode node = paths.get(path);
        node.uses--;
        while (node.forward == null && node.back != null) {
            if (node.uses > 0) break;
            paths.get(node.back).forward = null;
            paths.remove(path);
            node = paths.get(node.back);
        }
        while (node.back == null && node.forward != null) {
            if (node.uses > 0) break;
            paths.get(node.forward).back = null;
            paths.remove(path);
            node = paths.get(node.forward);
        }
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return uuid + ", " + (parts != null ? parts.subList(0, Math.min(parts.size(), maxLen)) : null) + ", " + lastTick;
    }

    public static class PathNode {
        public final ITrackPath thisPath;
        public ITrackPath forward, back;
        public int uses = 0;

        public PathNode(ITrackPath thisPath) {
            this.thisPath = thisPath;
        }
    }
}
