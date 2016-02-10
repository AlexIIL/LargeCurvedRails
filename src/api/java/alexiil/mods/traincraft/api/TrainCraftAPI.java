package alexiil.mods.traincraft.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import alexiil.mods.traincraft.api.train.ITrainMovementManager;
import alexiil.mods.traincraft.api.train.ITrainWorldCache;

public class TrainCraftAPI {
    public static final Logger apiLog = LogManager.getLogger("traincraft.api");
    public static ITrainMovementManager MOVEMENT_MANAGER;
    public static ITrainWorldCache WORLD_CACHE;
}
