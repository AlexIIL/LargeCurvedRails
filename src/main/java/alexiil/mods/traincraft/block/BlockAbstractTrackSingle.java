package alexiil.mods.traincraft.block;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.lib.MCObjectUtils;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;

/** Denotes a track that only contains a single behaviour. */
public abstract class BlockAbstractTrackSingle extends BlockAbstractTrack {
    public BlockAbstractTrackSingle(IProperty<?>... properties) {
        super(properties);
    }

    @Override
    public Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state) {
        TrackBehaviour behaviour = singleBehaviour(access, pos, state);
        if (behaviour == null) return ImmutableList.of();
        return ImmutableList.of(behaviour);
    }

    @Override
    public TrackBehaviour currentBehaviour(IBlockAccess access, BlockPos pos, IBlockState state, Vec3 from) {
        TrackBehaviour single = singleBehaviour(access, pos, state);
        if (single == null) return null;
        ITrackPath p = single.getPath(access, pos, state);
        if (MCObjectUtils.equals(from, p.start())) return single;
        if (MCObjectUtils.equals(from, p.end())) return single;
        return null;
    }

    @Nullable
    public abstract TrackBehaviour singleBehaviour(IBlockAccess access, BlockPos pos, IBlockState state);
}
