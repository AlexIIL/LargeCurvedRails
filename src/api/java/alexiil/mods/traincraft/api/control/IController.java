package alexiil.mods.traincraft.api.control;

import java.util.stream.Stream;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;

/** A generic controller. Will control where a train goes. */
public interface IController {
    BehaviourWrapper findBehaviour(Stream<BehaviourWrapper> possibilities);
}
