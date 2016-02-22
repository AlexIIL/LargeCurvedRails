package alexiil.mods.traincraft.track;

import alexiil.mods.traincraft.TrackRegistry;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

/** Stores all single instances of tracks. This does NOT contain curves or ascending tracks as the permutations are delt
 * with in the appropriate enums. */
public enum TCTracks {
    TRACK_STRAIGHT(TrackBehaviourStraightState.Factory.INSTANCE);

    private final StatefulFactory factory;

    private TCTracks(StatefulFactory factory) {
        this.factory = factory;
    }

    public static void preInit() {
        for (TCTracks tcTrack : values()) {
            if (tcTrack.factory != null) TrackRegistry.INSTANCE.register(tcTrack.factory);
        }

        for (Curve c : Curve.values()) {
            TrackRegistry.INSTANCE.register(c.halfFactory);
            TrackRegistry.INSTANCE.register(c.fullFactory);
        }
    }

    public StatefulFactory factory() {
        return factory;
    }
}
