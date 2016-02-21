package alexiil.mods.traincraft.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import alexiil.mods.traincraft.api.track.ITrackProvider;
import alexiil.mods.traincraft.api.track.ITrackRegistry;
import alexiil.mods.traincraft.api.train.ITrainMovementManager;

public class TrainCraftAPI {
    public static final Logger apiLog = LogManager.getLogger("traincraft.api");
    @Deprecated
    public static ITrainMovementManager MOVEMENT_MANAGER;
    public static ITrackProvider TRACK_PROVIDER;
    public static ITrackRegistry TRACK_STATE_REGISTRY;
}
