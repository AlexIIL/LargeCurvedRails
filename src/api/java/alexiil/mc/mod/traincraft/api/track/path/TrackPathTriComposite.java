package alexiil.mc.mod.traincraft.api.track.path;

public class TrackPathTriComposite<A extends ITrackPath, B extends ITrackPath, C extends ITrackPath> extends TrackPathCompositeBase {
    public final A pathA;
    public final B pathB;
    public final C pathC;

    public TrackPathTriComposite(A pathA, B pathB, C pathC) {
        super(pathA, pathB, pathC);
        this.pathA = pathA;
        this.pathB = pathB;
        this.pathC = pathC;
    }
}
