package alexiil.mods.traincraft.track;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.block.TCBlocks;

public enum TrackJoiner {
    INSTANCE;

    private List<ITrackJoiner> joiners = new ArrayList<>();

    public void init() {
        joiners.addAll(CurvedTrackJoiner.create(TCBlocks.TRACK_CURVED_HALF_3_RADIUS, TCBlocks.TRACK_CURVED_FULL_3_RADIUS));
    }

    public void tryJoin(World world, BlockPos pos) {
        for (ITrackJoiner joiner : joiners) {
            if (joiner.tryJoin(world, pos)) return;
        }
    }

    public void split(World world, BlockPos pos) {
        for (ITrackJoiner joiner : joiners) {
            joiner.split(world, pos);
        }
    }

    public interface ITrackJoiner {
        boolean tryJoin(World world, BlockPos pos);

        void split(World world, BlockPos pos);
    }
}
