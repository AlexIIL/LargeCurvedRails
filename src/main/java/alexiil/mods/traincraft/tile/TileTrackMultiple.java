package alexiil.mods.traincraft.tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;

import alexiil.mods.traincraft.api.lib.MCObjectUtils.Vec3Key;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackMultiple;

public class TileTrackMultiple extends TileAbstractTrack {
    protected final List<TrackBehaviourStateful> tracks = new ArrayList<>(), unmodifiable = Collections.unmodifiableList(tracks);
    private final List<TrackBehaviour> nonStateful = new ArrayList<>(), nsUnmodifiable = Collections.unmodifiableList(nonStateful);
    protected final Multimap<Vec3Key, TrackBehaviour> joinMap = HashMultimap.create();

    @Override
    public List<TrackBehaviourStateful> getBehaviours() {
        return unmodifiable;
    }

    public Collection<TrackBehaviour> getBehavioursNonStateful() {
        return nsUnmodifiable;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (TrackBehaviourStateful track : tracks) {
            NBTTagCompound comp = new NBTTagCompound();
            comp.setTag("data", track.serializeNBT());
            comp.setString("type", track.factory().identifier());
            list.appendTag(comp);
        }
        nbt.setTag("tracks", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tracks.clear();
        NBTTagList list = (NBTTagList) nbt.getTag("tracks");
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound comp = list.getCompoundTagAt(i);
            String type = comp.getString("type");
            NBTTagCompound data = comp.getCompoundTag("data");
            StatefulFactory factory = null;// FIXME
            TrackBehaviourStateful behavior = factory.create(getWorld(), getPos());
            behavior.deserializeNBT(data);
            tracks.add(behavior);
            nonStateful.add(behavior);
        }
    }

    /** Repopulates the {@link #joinMap} */
    protected void regenJoinMap() {
        joinMap.clear();
        for (TrackBehaviourStateful track : tracks) {
            ITrackPath path = track.getPath(getWorld(), getPos(), getWorld().getBlockState(getPos()));
            joinMap.put(new Vec3Key(path.start()), track);
            joinMap.put(new Vec3Key(path.end()), track);
        }
    }

    public void addTrack(TrackBehaviourStateful behaviour) {
        tracks.add(behaviour);
        nonStateful.add(behaviour);

        boolean stateTickable = this instanceof ITickable | behaviour instanceof ITickable;
        boolean statePoints = this instanceof TileTrackMultiplePoints && hasPoints();

        convert(forState(stateTickable, statePoints));
    }

    protected boolean hasPoints() {
        for (Collection<TrackBehaviour> key : joinMap.asMap().values()) {
            if (key.size() > 1) return true;
        }
        return false;
    }

    public void removeTrack(TrackBehaviourStateful behaviour) {
        // No point in doing anything if the behaviour given didn't actually exist
        if (!tracks.remove(behaviour)) return;
        nonStateful.remove(behaviour);

        boolean stateTickable = this instanceof ITickable && tracks.stream().anyMatch(t -> t instanceof ITickable);
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
        mult.tracks.clear();
        mult.nonStateful.clear();

        mult.tracks.addAll(tracks);
        mult.nonStateful.addAll(nonStateful);

        IBlockState state = worldObj.getBlockState(getPos());
        state = state.withProperty(BlockTrackMultiple.TICKABLE, mult instanceof ITickable);
        state = state.withProperty(BlockTrackMultiple.POINTS, mult instanceof TileTrackMultiplePoints);
        worldObj.setBlockState(getPos(), state);
        worldObj.setTileEntity(getPos(), mult);
    }

    public static class Tickable extends TileTrackMultiple implements ITickable {
        @Override
        public void update() {
            for (TrackBehaviourStateful track : tracks) {
                if (track instanceof ITickable) {
                    ((ITickable) track).update();
                }
            }
        }
    }
}
