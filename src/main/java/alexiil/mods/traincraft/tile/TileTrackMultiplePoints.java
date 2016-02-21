package alexiil.mods.traincraft.tile;

import net.minecraft.util.ITickable;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;

public class TileTrackMultiplePoints extends TileTrackMultiple {

    public static class Tickable extends TileTrackMultiplePoints implements ITickable {
        @Override
        public void update() {
            for (TrackBehaviourStateful track : tracks) {
                if (track instanceof ITickable) {
                    ((ITickable) track).update();
                }
            }
        }
    }
}
