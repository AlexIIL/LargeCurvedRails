package alexiil.mods.traincraft.track;

import alexiil.mods.traincraft.TrackRegistry;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

public enum TCTracks {
    TRACK_STRAIGHT(null),;

    // TODO: Possibly other useful variables?
    private final StatefulFactory factory;

    private TCTracks(StatefulFactory factory) {
        this.factory = factory;
    }

    public static void preInit() {
        for (TCTracks tcTrack : values()) {
            if (tcTrack.factory != null) TrackRegistry.INSTANCE.register(tcTrack.factory);
        }
    }

    public StatefulFactory factory() {
        return factory;
    }
}
