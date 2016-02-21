package alexiil.mods.traincraft.api.track;

import java.util.HashMap;
import java.util.Map;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

public enum TrackRegistry {
    INSTANCE;

    private final Map<String, StatefulFactory> registry = new HashMap<>();

    public void register(StatefulFactory statefulTrack) {
        registry.put(statefulTrack.identifier(), statefulTrack);
    }

    public void unregister(StatefulFactory statefulTrack) {
        registry.remove(statefulTrack.identifier(), statefulTrack);
    }
}
