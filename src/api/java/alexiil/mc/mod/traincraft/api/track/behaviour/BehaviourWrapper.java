package alexiil.mc.mod.traincraft.api.track.behaviour;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;

/** This wraps a {@link TrackBehaviour} object so you don't need to know the block, world or state that the behaviour
 * originated from. This should be garbage collected quickly, so don't keep this around for longer than the world will
 * exist. If you keep this for extended periods of time then beware that the track might have been changed in the world,
 * and is no longer valid. */
public final class BehaviourWrapper {
    private final TrackBehaviour behaviour;
    private final World world;
    private final BlockPos pos;
    private final boolean reversed;

    public BehaviourWrapper(TrackBehaviour behaviour, World world, BlockPos pos) {
        this(behaviour, world, pos, false);
    }

    public BehaviourWrapper(TrackBehaviour behaviour, World world, BlockPos pos, boolean reversed) {
        if (behaviour == null) throw new NullPointerException("behaviour");
        if (world == null) throw new NullPointerException("world");
        if (pos == null || pos instanceof MutableBlockPos) throw new NullPointerException("pos");
        this.behaviour = behaviour;
        this.world = world;
        this.pos = pos;
        this.reversed = reversed;
    }

    // Delegate methods
    public ITrackPath getPath() {
        if (reversed) return behaviour.getPath(world, pos, state()).reverse();
        return behaviour.getPath(world, pos, state());
    }

    public TrackIdentifier getIdentifier() {
        TrackIdentifier ident = behaviour.getIdentifier(world, pos, state());
        if (reversed) ident.reverse();
        return ident;
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
        result = prime * result + ((world == null) ? 0 : world.provider.getDimension());
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
