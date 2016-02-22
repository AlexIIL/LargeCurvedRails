package alexiil.mods.traincraft.compat.vanilla;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.lib.MCObjectUtils;
import alexiil.mods.traincraft.api.track.ITrackBlock;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;

public class VanillaTrackBlock implements ITrackBlock {
    private final BehaviourVanillaNative behaviour;

    public VanillaTrackBlock(BehaviourVanillaNative behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state) {
        return ImmutableList.of(behaviour);
    }

    @Override
    public TrackBehaviour currentBehaviour(IBlockAccess access, BlockPos pos, IBlockState state, Vec3 from) {
        ITrackPath path = behaviour.getPath(access, pos, state);
        if (MCObjectUtils.equals(from, path.start())) return behaviour;
        if (MCObjectUtils.equals(from, path.end())) return behaviour;
        return null;
    }

}
