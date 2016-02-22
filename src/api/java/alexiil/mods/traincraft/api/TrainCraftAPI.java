package alexiil.mods.traincraft.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import alexiil.mods.traincraft.api.track.ITrackPlacer;
import alexiil.mods.traincraft.api.track.ITrackProvider;
import alexiil.mods.traincraft.api.track.ITrackRegistry;

public class TrainCraftAPI {
    public static final Logger apiLog = LogManager.getLogger("traincraft.api");
    public static ITrackProvider TRACK_PROVIDER;
    public static ITrackRegistry TRACK_STATE_REGISTRY;
    public static ITrackPlacer TRACK_PLACER;
}
