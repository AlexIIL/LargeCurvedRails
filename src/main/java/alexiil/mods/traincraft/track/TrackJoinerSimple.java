package alexiil.mods.traincraft.track;

import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.track.TrackJoiner.ITrackJoiner;

public abstract class TrackJoinerSimple implements ITrackJoiner {
    private final Map<BlockPos, WorldBlockState> stateMap;

    public TrackJoinerSimple(Map<BlockPos, WorldBlockState> map) {
        stateMap = map;
    }

    @Override
    public boolean tryJoin(World world, BlockPos pos) {
        return false;
    }

    @Override
    public void split(World world, BlockPos pos) {

    }

    public static class WorldBlockState {
        public final IBlockState from, to;

        public WorldBlockState(IBlockState from, IBlockState to) {
            this.from = from;
            this.to = to;
        }
    }
}
