package alexiil.mods.traincraft.tile;

import java.util.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrackRegistry;
import alexiil.mods.traincraft.api.lib.MCObjectUtils.Vec3Key;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackMultiple;

public class TileTrackMultiple extends TileAbstractTrack {
    protected final List<BehaviourWrapper> pointingTo = new ArrayList<>(), umPointingTo = Collections.unmodifiableList(pointingTo);
    protected final List<BehaviourWrapper> containing = new ArrayList<>(), umContaining = Collections.unmodifiableList(containing);
    protected final List<BehaviourWrapper> allWrapped = new ArrayList<>(), umAllWrapped = Collections.unmodifiableList(allWrapped);
    protected final Multimap<Vec3Key, BehaviourWrapper> joinMap = HashMultimap.create();

    private NBTTagCompound postLoad;

    @Override
    public List<BehaviourWrapper> getWrappedBehaviours() {
        return umAllWrapped;
    }

    @Override
    public void setWorldObj(World world) {
        super.setWorldObj(world);
        if (world != null) {
            if (postLoad == null) return;
            readFromNBT(postLoad);
            postLoad = null;
        }
    }

    public BehaviourWrapper currentBehaviour(Vec3 from) {
        Collection<BehaviourWrapper> behaviours = joinMap.get(new Vec3Key(from));
        Iterator<BehaviourWrapper> it = behaviours.iterator();
        if (!it.hasNext()) return null;
        return it.next();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (BehaviourWrapper wrapped : containing) {
            TrackBehaviourStateful track = (TrackBehaviourStateful) wrapped.behaviour();
            NBTTagCompound comp = new NBTTagCompound();
            comp.setString("type", track.factory().identifier());
            comp.setTag("data", track.serializeNBT());
            list.appendTag(comp);
        }
        nbt.setTag("tracks", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!hasWorldObj()) {
            // Loading depends on having the world, so we will load later if we don't actually have the world right now
            postLoad = nbt;
            return;
        }
        containing.clear();
        NBTTagList list = (NBTTagList) nbt.getTag("tracks");
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound comp = list.getCompoundTagAt(i);
            String type = comp.getString("type");
            NBTTagCompound data = comp.getCompoundTag("data");
            StatefulFactory factory = TrackRegistry.INSTANCE.getFactory(type);
            TrackBehaviourStateful behavior = factory.create(getWorld(), getPos());
            behavior.deserializeNBT(data);
            BehaviourWrapper wrapped = new BehaviourWrapper(behavior, getWorld(), behavior.getIdentifier().pos());
            containing.add(wrapped);
            allWrapped.add(wrapped);
        }
    }

    /** Repopulates the {@link #joinMap} */
    protected void regenJoinMap() {
        joinMap.clear();
        for (BehaviourWrapper track : allWrapped) {
            ITrackPath path = track.getPath();
            joinMap.put(new Vec3Key(path.start()), track);
            joinMap.put(new Vec3Key(path.end()), track);
        }
    }

    public boolean addTrack(TrackBehaviourStateful behaviour) {
        if (!getPos().equals(behaviour.getIdentifier().pos())) throw new IllegalArgumentException("Different positions!");
        BehaviourWrapper wrapped = new BehaviourWrapper(behaviour, getWorld(), getPos());

        for (BehaviourWrapper w : allWrapped) {
            if (!w.behaviour().canOverlap(behaviour)) return false;
        }

        containing.add(wrapped);
        allWrapped.add(wrapped);

        boolean stateTickable = this instanceof ITickable || behaviour instanceof ITickable;
        boolean statePoints = this instanceof TileTrackMultiplePoints || hasPoints();

        convert(forState(stateTickable, statePoints));
        return true;
    }

    protected boolean hasPoints() {
        for (Collection<BehaviourWrapper> key : joinMap.asMap().values()) {
            if (key.size() > 1) return true;
        }
        return false;
    }

    public void removeTrack(TrackBehaviourStateful behaviour) {
        BehaviourWrapper wrapped = new BehaviourWrapper(behaviour, getWorld(), behaviour.getIdentifier().pos());
        // No point in doing anything if the behaviour given didn't actually exist
        if (!containing.remove(wrapped)) return;
        allWrapped.remove(wrapped);

        boolean stateTickable = this instanceof ITickable && containing.stream().anyMatch(t -> t.behaviour() instanceof ITickable);
        boolean statePoints = this instanceof TileTrackMultiplePoints && hasPoints();

        convert(forState(stateTickable, statePoints));
    }

    protected final TileTrackMultiple forState(boolean tickable, boolean points) {
        if (tickable == this instanceof ITickable && points == this instanceof TileTrackMultiplePoints) return this;
        TileTrackMultiple tile;
        if (!tickable && !points) return new TileTrackMultiple();
        else if (tickable && !points) tile = new Tickable();
        else if (!tickable) tile = new TileTrackMultiplePoints();
        else /* (tickable && points) */ tile = new TileTrackMultiplePoints.Tickable();
        return tile;
    }

    protected final void convert(TileTrackMultiple mult) {
        if (mult == this || mult.getClass() == this.getClass()) return;
        mult.containing.clear();
        mult.pointingTo.clear();
        mult.allWrapped.clear();

        mult.containing.addAll(containing);
        mult.pointingTo.addAll(pointingTo);
        mult.allWrapped.addAll(allWrapped);

        IBlockState state = worldObj.getBlockState(getPos());
        state = state.withProperty(BlockTrackMultiple.TICKABLE, mult instanceof ITickable);
        state = state.withProperty(BlockTrackMultiple.POINTS, mult instanceof TileTrackMultiplePoints);
        worldObj.setBlockState(getPos(), state);
        worldObj.setTileEntity(getPos(), mult);
    }

    public static class Tickable extends TileTrackMultiple implements ITickable {
        @Override
        public void update() {
            for (BehaviourWrapper track : containing) {
                if (track.behaviour() instanceof ITickable) {
                    ((ITickable) track.behaviour()).update();
                }
            }
        }
    }
}
