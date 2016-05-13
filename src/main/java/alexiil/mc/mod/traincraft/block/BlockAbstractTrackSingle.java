package alexiil.mc.mod.traincraft.block;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.lib.MCObjectUtils;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

/** Denotes a track that only contains a single behaviour. */
public abstract class BlockAbstractTrackSingle extends BlockAbstractTrack {
    public BlockAbstractTrackSingle() {
        super();
    }

    @Override
    public Collection<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state) {
        BehaviourWrapper behaviour = singleBehaviour(world, pos, state);
        if (behaviour == null) return ImmutableList.of();
        return ImmutableList.of(behaviour);
    }

    @Override
    public BehaviourWrapper currentBehaviour(World world, BlockPos pos, IBlockState state, Vec3d from) {
        BehaviourWrapper single = singleBehaviour(world, pos, state);
        if (single == null) return null;
        ITrackPath p = single.getPath();
        if (MCObjectUtils.equals(from, p.start())) return single;
        if (MCObjectUtils.equals(from, p.end())) return single;
        return null;
    }

    @Nullable
    public abstract BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state);
}
