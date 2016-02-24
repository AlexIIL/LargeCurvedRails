package alexiil.mods.traincraft.compat.vanilla;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackBlock;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;

public class VanillaTrackBlock implements ITrackBlock {
    private final BehaviourVanillaNative behaviour;

    public VanillaTrackBlock(BehaviourVanillaNative behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public Collection<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state) {
        return ImmutableList.of(new BehaviourWrapper(behaviour, world, pos));
    }
}
