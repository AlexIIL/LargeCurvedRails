package alexiil.mc.mod.traincraft;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import alexiil.mc.mod.traincraft.api.AddonManager;
import alexiil.mc.mod.traincraft.api.TrainCraftAPI;
import alexiil.mc.mod.traincraft.block.TCBlocks;
import alexiil.mc.mod.traincraft.compat.TCCompat;
import alexiil.mc.mod.traincraft.component.TypeCartSmall;
import alexiil.mc.mod.traincraft.component.TypeSteamSmall;
import alexiil.mc.mod.traincraft.entity.EntityGenericRollingStock;
import alexiil.mc.mod.traincraft.item.TCItems;
import alexiil.mc.mod.traincraft.network.MessageHandler;
import alexiil.mc.mod.traincraft.track.TCTracks;

@Mod(modid = DefaultProps.MODID, name = "TrainCraft")
public class TrainCraft {
    @Instance
    public static TrainCraft instance;

    public static Logger trainCraftLog;
    public static Configuration cfg;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        trainCraftLog = event.getModLog();
        cfg = new Configuration(event.getSuggestedConfigurationFile());

        TrainCraftAPI.TRACK_PROVIDER = TrackPathProvider.INSTANCE;
        TrainCraftAPI.TRACK_STATE_REGISTRY = TrackRegistry.INSTANCE;
        TrainCraftAPI.TRACK_PLACER = TrackPlacer.INSTANCE;
        TrainCraftAPI.TRAIN_REGISTRY = TrainRegistry.INSTANCE;

        TCTracks.preInit();
        TCBlocks.preInit();
        TCItems.preInit();
        TCTabs.preInit();
        TCCompat.preInit();

        EntityRegistry.registerModEntity(EntityGenericRollingStock.class, "genericRollingStock", 0, instance, 60, 64, false);

        TrainCraftAPI.TRAIN_REGISTRY.registerTrain(TypeCartSmall.INSTANCE);
        TrainCraftAPI.TRAIN_REGISTRY.registerTrain(TypeSteamSmall.INSTANCE);

        AddonManager.INSTANCE.preInit();

        Proxy.proxy.preInit(event);
        MessageHandler.INSTANCE.preInit();

        MinecraftForge.EVENT_BUS.register(CartCompat.INSTANCE);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TCBlocks.init();
        TCItems.init();
        TCRecipies.init();
        TCCompat.init();
        AddonManager.INSTANCE.init();

        Proxy.proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        TCCompat.postInit();
        AddonManager.INSTANCE.postInit();
        Proxy.proxy.postInit(event);

        AddonManager.INSTANCE.enableAll();
    }
}
