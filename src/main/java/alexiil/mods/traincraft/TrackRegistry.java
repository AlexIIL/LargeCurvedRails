package alexiil.mods.traincraft;

import java.util.HashMap;
import java.util.Map;

import alexiil.mods.traincraft.api.track.ITrackRegistry;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

public enum TrackRegistry implements ITrackRegistry {
    INSTANCE;

    private final Map<String, StatefulFactory> registry = new HashMap<>();

    @Override
    public void register(StatefulFactory statefulTrack) {
        registry.put(statefulTrack.identifier(), statefulTrack);
    }

    @Override
    public void unregister(StatefulFactory statefulTrack) {
        registry.remove(statefulTrack.identifier(), statefulTrack);
    }
}
