package alexiil.mods.traincraft.api;

import java.util.*;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock.Face;

import io.netty.buffer.ByteBuf;

public class Train {
    /* Ids are only used to communicate with clients- they are NOT used for saving and loading (We use a full UUID for
     * that) */
    private static volatile int nextId = 0;

    // Synchronized so that 1.9 world synch works fully.
    private static synchronized int nextId() {
        return nextId++;
    }

    public final int id;
    public final UUID uuid;
    public final ImmutableList<IRollingStock> parts;
    private final List<BlockPos> trackPositions = Collections.synchronizedList(new LinkedList<>());
    private final List<ITrackPath> trackPaths = Collections.synchronizedList(new LinkedList<>());
    private long lastTick = -1;

    public Train(IRollingStock stock) {
        if (stock == null) throw new NullPointerException("stock");
        if (!(stock instanceof Entity)) throw new IllegalArgumentException(stock.getClass() + " was not an instanceof Entity!");
        id = nextId();
        uuid = UUID.randomUUID();
        parts = ImmutableList.of(stock);
        parts.forEach(p -> p.setTrain(this));
    }

    private Train(List<IRollingStock> stocks) {
        id = nextId();
        uuid = UUID.randomUUID();
        parts = ImmutableList.copyOf(stocks);
        parts.forEach(p -> p.setTrain(this));
    }

    /** Constructor used on the client for recieving train objects from the server. The UUID is never sent from the
     * server so its fine */
    @SideOnly(Side.CLIENT)
    Train(int id, List<IRollingStock> stock) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        parts = ImmutableList.copyOf(stock);
        parts.forEach(p -> p.setTrain(this));
    }

    /** Constructor used on the server (or integrated server) for loading trains from a save file. */
    Train(UUID uuid, List<IRollingStock> stock) {
        this.id = nextId();
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

        World world = ((Entity) parts.get(0)).getEntityWorld();

        NBTTagList paths = new NBTTagList();
        for (int i = 0; i < this.trackPaths.size(); i++) {
            BlockPos pos = trackPositions.get(i);
            ITrackPath path = trackPaths.get(i);
            IBlockState state = world.getBlockState(pos);
            ITrackPath[] trackPaths = TrackPathProvider.getPathsFor(world, pos, state);
            for (int j = 0; j < trackPaths.length; j++) {
                if (trackPaths.equals(path)) {

                    break;
                }
            }

            NBTTagCompound comp = new NBTTagCompound();
            comp.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        }
        tag.setTag("paths", paths);

        return tag;
    }

    @SideOnly(Side.CLIENT)
    public static Train createFromByteBuf(ByteBuf buffer) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return null;

        int trainId = buffer.readInt();
        int dimId = buffer.readInt();

        // Something probably went wrong- we no longer have the world the train was in.
        if (world.provider.getDimensionId() != dimId) return null;

        int num = buffer.readInt();
        List<IRollingStock> stockList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            int entId = buffer.readInt();
            Entity ent = world.getEntityByID(dimId);
            if (ent instanceof IRollingStock) {
                IRollingStock stock = (IRollingStock) ent;
                stockList.add(stock);
            } else if (ent == null) {
                TrainCraftAPI.apiLog.warn("Train::readFromByteBuf | Tried to get the train entity for " + entId + " but found null");
            } else {
                TrainCraftAPI.apiLog.warn("Train::readFromByteBuf | Tried to get the train entity for " + entId + " but found " + ent.getClass());
            }
        }
        Train t = new Train(trainId, stockList);
        t.readFromByteBuf(buffer);
        return t;
    }

    @SideOnly(Side.CLIENT)
    public void readFromByteBuf(ByteBuf buffer) {
        World world = Minecraft.getMinecraft().theWorld;

        List<BlockPos> positions = new ArrayList<>();
        List<ITrackPath> paths = new ArrayList<>();
        int pathSize = buffer.readInt();
        for (int i = 0; i < pathSize; i++) {
            BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
            int index = buffer.readInt();
            if (index == -1) continue;
            boolean reversed = buffer.readBoolean();
            ITrackPath[] potential = TrackPathProvider.getPathsFor(world, pos, world.getBlockState(pos));
            if (potential.length <= index) continue;

            positions.add(pos);
            paths.add(reversed ? potential[index].reverse() : potential[index]);
        }
        trackPositions.clear();
        trackPaths.clear();
        trackPositions.addAll(positions);
        trackPaths.addAll(paths);
    }

    public void writeToByteBuf(ByteBuf buffer) {
        buffer.writeInt(id);

        // Write out the approximate location (world dimension id)
        Entity ent = (Entity) parts.get(0);
        World world = ent.getEntityWorld();
        buffer.writeInt(world.provider.getDimensionId());

        buffer.writeInt(parts.size());
        for (IRollingStock stock : parts) {
            Entity entity = (Entity) stock;
            buffer.writeInt(entity.getEntityId());
        }

        List<BlockPos> positions;
        List<ITrackPath> paths;
        synchronized (this) {
            /* Copy it out so we know that both lists are the same size- we don't want either of them to change size
             * after we copied one. */
            positions = new ArrayList<>(this.trackPositions);
            paths = new ArrayList<>(this.trackPaths);
        }

        buffer.writeInt(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            BlockPos pos = positions.get(i);
            buffer.writeInt(pos.getX());
            buffer.writeInt(pos.getY());
            buffer.writeInt(pos.getZ());
            IBlockState state = world.getBlockState(pos);
            ITrackBlock block = TrackPathProvider.getBlockFor(world, pos, state);
            if (block == null) {
                buffer.writeInt(-1);
            } else {
                ITrackPath[] blockPaths = block.paths(world, pos, state);
                ITrackPath path = paths.get(i);
                boolean reversed = false;
                int index = -1;

                for (int j = 0; j < blockPaths.length; j++) {
                    if (blockPaths[j].equals(path)) {
                        index = j;
                        break;
                    } else if (blockPaths[j].reverse().equals(path)) {
                        index = j;
                        reversed = true;
                        break;
                    }
                }
                buffer.writeInt(index);
                if (index != -1) buffer.writeBoolean(reversed);
            }
        }
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

    private ITrackPath computeNextPath(World world, Face direction) {
        ITrackPath end = direction == Face.FRONT ? trackPaths.get(0) : trackPaths.get(trackPaths.size() - 1);
        if (direction == Face.BACK) end = end.reverse();
        ITrackPath followOn = TrainCraftAPI.MOVEMENT_MANAGER.next(world, end);
        if (followOn != null) {
            synchronized (this) {
                if (direction == Face.FRONT) {
                    trackPaths.add(0, followOn);
                    trackPositions.add(0, followOn.creatingBlock());
                } else {
                    trackPaths.add(followOn);
                    trackPositions.add(followOn.creatingBlock());
                }
            }
            if (!world.isRemote) TrainCraftAPI.WORLD_CACHE.updateTrain(this);
        }
        return followOn;
    }

    /** Computes the next path along from the given path in the given direction. For simplicities sake you can call this
     * from either the server or the client to get a path. */
    public ITrackPath requestNextTrackPath(IRollingStock caller, ITrackPath currentPath, Face direction) {
        if (trackPaths.isEmpty()) {
            if (currentPath == null) trackPaths.add(TrainCraftAPI.MOVEMENT_MANAGER.closest(caller, direction));
            else trackPaths.add(currentPath);
        }
        if (trackPaths.contains(currentPath)) {
            int index = trackPaths.indexOf(currentPath) + (direction == Face.BACK ? 1 : -1);
            if (index == trackPaths.size() || index == -1) {
                return computeNextPath(((Entity) caller).getEntityWorld(), direction);
            } else return trackPaths.get(index);
        } else {
            Vec3 pos = caller.getPathPosition();
            for (ITrackPath path : trackPaths) {
                if (path.start().distanceTo(pos) < 0.1) return path;
                else if (path.end().distanceTo(pos) < 0.1) return path.reverse();
            }
            return direction == Face.FRONT ? trackPaths.get(0) : trackPaths.get(trackPaths.size() - 1);
        }
    }

    /** Disposes a path if the caller is the last train in the given direction.
     * 
     * @param face The current direction the train is headed. */
    public void disposePath(ITrackPath path, IRollingStock caller, Face face) {
        int index = parts.indexOf(caller);
        if (face == Face.FRONT && index != 0) return;
        if (face == Face.BACK && index != parts.size() - 1) return;
        synchronized (this) {
            trackPaths.remove(index);
            trackPositions.remove(index);
        }
    }
}
