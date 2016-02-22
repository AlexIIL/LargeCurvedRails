package alexiil.mods.traincraft.api.track.behaviour;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;

/** This wraps a {@link TrackBehaviour} object so you don't need to know the block, world or state that the behaviour
 * originated from. This should be garbage collected quickly, so don't keep this around for longer than the world will
 * exist. If you keep this for extended periods of time then beware that the track might have been changed in the world,
 * and is no longer valid. */
public final class BehaviourWrapper {
    private final TrackBehaviour behaviour;
    private final World world;
    private final BlockPos pos;

    public BehaviourWrapper(TrackBehaviour behaviour, World world, BlockPos pos) {
        if (behaviour == null) throw new NullPointerException("behaviour");
        if (world == null) throw new NullPointerException("world");
        if (pos == null || pos instanceof MutableBlockPos) throw new NullPointerException("pos");
        this.behaviour = behaviour;
        this.world = world;
        this.pos = pos;
        if (getPath() == null) throw new IllegalArgumentException("Null path! " + behaviour.getClass());
        if (getIdentifier() == null) throw new IllegalArgumentException("Null identifier! " + behaviour.getClass());
        if (!isValid()) throw new IllegalArgumentException("Invalid behaviour! " + behaviour.getClass());
    }

    // Delegate methods
    public ITrackPath getPath() {
        return behaviour.getPath(world, pos, state());
    }

    public TrackIdentifier getIdentifier() {
        return behaviour.getIdentifier(world, pos, state());
    }

    public void onStockPass(IRollingStock stock) {
        behaviour.onStockPass(world, pos, state(), stock);
    }

    public boolean isValid() {
        return behaviour.isValid(world, pos, state());
    }

    // Getters
    public TrackBehaviour behaviour() {
        return behaviour;
    }

    public World world() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public IBlockState state() {
        return world.getBlockState(pos);
    }

    // Internal
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((behaviour == null) ? 0 : behaviour.hashCode());
        result = prime * result + ((pos == null) ? 0 : pos.hashCode());
        // We don't need to compute the entire WORLD's hash (and it changes)
        result = prime * result + ((world == null) ? 0 : world.provider.getDimensionId());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BehaviourWrapper other = (BehaviourWrapper) obj;
        if (behaviour == null) {
            if (other.behaviour != null) return false;
        } else if (!behaviour.equals(other.behaviour)) return false;
        if (pos == null) {
            if (other.pos != null) return false;
        } else if (!pos.equals(other.pos)) return false;
        return world == other.world;
    }
}
