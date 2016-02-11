package alexiil.mods.traincraft.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import alexiil.mods.traincraft.api.train.ITrainMovementManager;

public class TrainCraftAPI {
    public static final Logger apiLog = LogManager.getLogger("traincraft.api");
    public static ITrainMovementManager MOVEMENT_MANAGER;
}
