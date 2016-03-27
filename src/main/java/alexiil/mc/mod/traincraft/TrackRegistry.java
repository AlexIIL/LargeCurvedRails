package alexiil.mc.mod.traincraft;

import java.util.HashMap;
import java.util.Map;

import alexiil.mc.mod.traincraft.api.track.ITrackRegistry;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

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

    @Override
    public StatefulFactory getFactory(String type) {
        return registry.get(type);
    }
}
