package alexiil.mods.traincraft.tile;

import net.minecraft.util.ITickable;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;

public class TileTrackMultiplePoints extends TileTrackMultiple {

    public static class Tickable extends TileTrackMultiplePoints implements ITickable {
        @Override
        public void update() {
            for (BehaviourWrapper track : containing) {
                if (track.behaviour() instanceof ITickable) {
                    ((ITickable) track.behaviour()).update();
                }
            }
        }
    }
}
