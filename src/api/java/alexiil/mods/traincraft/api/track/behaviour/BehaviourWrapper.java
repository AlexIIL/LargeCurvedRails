package alexiil.mods.traincraft.api.track.behaviour;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
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
        this.behaviour = behaviour;
        this.world = world;
        this.pos = pos;
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
}
