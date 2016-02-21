package alexiil.mods.traincraft.tile;

import java.util.Collections;
import java.util.List;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public class TileTrackMultiplePointer extends TileAbstractTrack {
    @Override
    public List<TrackBehaviourStateful> getBehaviours() {
        return Collections.emptyList();
    }
}
