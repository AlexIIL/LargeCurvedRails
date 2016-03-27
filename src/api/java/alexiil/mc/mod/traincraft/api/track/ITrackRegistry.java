package alexiil.mc.mod.traincraft.api.track;

import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.StatefulFactory;

public interface ITrackRegistry {
    void register(StatefulFactory statefulTrack);

    void unregister(StatefulFactory statefulTrack);

    StatefulFactory getFactory(String type);
}
