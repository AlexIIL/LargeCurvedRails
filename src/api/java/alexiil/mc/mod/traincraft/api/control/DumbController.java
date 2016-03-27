package alexiil.mc.mod.traincraft.api.control;

import java.util.stream.Stream;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;

/** A dumb controller. Just goes to any of the possible tracks places. */
public enum DumbController implements IController {
    INSTANCE;

    @Override
    public BehaviourWrapper findBehaviour(Stream<BehaviourWrapper> possibilities) {
        return possibilities.findAny().orElse(null);
    }
}
