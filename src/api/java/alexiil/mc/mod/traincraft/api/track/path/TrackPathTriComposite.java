package alexiil.mc.mod.traincraft.api.track.path;

import net.minecraft.util.BlockPos;

public class TrackPathTriComposite<A extends ITrackPath, B extends ITrackPath, C extends ITrackPath> extends TrackPathCompositeBase {
    private final A pathA;
    private final B pathB;
    private final C pathC;

    public TrackPathTriComposite(BlockPos center, A pathA, B pathB, C pathC) {
        super(center, pathA, pathB, pathC);
        this.pathA = pathA;
        this.pathB = pathB;
        this.pathC = pathC;
    }

    @Override
    public TrackPathTriComposite<A, B, C> offset(BlockPos pos) {
        return new TrackPathTriComposite<>(pos.add(creatingBlock()), (A) pathA.offset(pos), (B) pathB.offset(pos), (C) pathC.offset(pos));
    }
}
