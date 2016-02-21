package alexiil.mods.traincraft;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import alexiil.mods.traincraft.api.AddonManager;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.compat.TCCompat;
import alexiil.mods.traincraft.component.TypeCartSmall;
import alexiil.mods.traincraft.component.TypeSteamSmall;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;
import alexiil.mods.traincraft.item.TCItems;
import alexiil.mods.traincraft.network.MessageHandler;
import alexiil.mods.traincraft.track.TCTracks;

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

        TrainCraftAPI.MOVEMENT_MANAGER = TrainMovementManager.INSTANCE;

        TCTracks.preInit();
        TCBlocks.preInit();
        TCItems.preInit();
        TCTabs.preInit();
        TCCompat.preInit();

        EntityRegistry.registerModEntity(EntityGenericRollingStock.class, "", 0, instance, 60, 64, false);

        TrainRegistry.INSTANCE.registerTrain(TypeCartSmall.INSTANCE);
        TrainRegistry.INSTANCE.registerTrain(TypeSteamSmall.INSTANCE);

        AddonManager.INSTANCE.preInit();

        Proxy.proxy.preInit(event);
        MessageHandler.INSTANCE.preInit();
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
