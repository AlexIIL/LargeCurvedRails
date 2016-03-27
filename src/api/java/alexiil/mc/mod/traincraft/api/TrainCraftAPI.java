package alexiil.mc.mod.traincraft.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.ITrackPlacer;
import alexiil.mc.mod.traincraft.api.track.ITrackProvider;
import alexiil.mc.mod.traincraft.api.track.ITrackRegistry;
import alexiil.mc.mod.traincraft.api.track.model.IModelSpriteGetter;
import alexiil.mc.mod.traincraft.api.train.ITrainRegistry;

public class TrainCraftAPI {
    public static final Logger apiLog = LogManager.getLogger("traincraft.api");
    public static ITrackProvider TRACK_PROVIDER;
    public static ITrackRegistry TRACK_STATE_REGISTRY;
    public static ITrackPlacer TRACK_PLACER;
    public static ITrainRegistry TRAIN_REGISTRY;

    @SideOnly(Side.CLIENT)
    public static IModelSpriteGetter SPRITE_GETTER;
}
