package alexiil.mods.traincraft;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import alexiil.mods.traincraft.api.AddonManager;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.compat.TCCompat;
import alexiil.mods.traincraft.entity.EntityRollingStockCart;
import alexiil.mods.traincraft.entity.EntitySmallSteamLocomotive;
import alexiil.mods.traincraft.item.TCItems;
import alexiil.mods.traincraft.network.MessageHandler;

@Mod(modid = DefaultProps.MODID, name = "TrainCraft")
public class TrainCraft {
    public static Logger trainCraftLog;
    public static Configuration cfg;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        trainCraftLog = event.getModLog();
        cfg = new Configuration(event.getSuggestedConfigurationFile());

        TrainCraftAPI.MOVEMENT_MANAGER = TrainMovementManager.INSTANCE;

        TCBlocks.preInit();
        TCItems.preInit();
        TCTabs.preInit();
        TCCompat.preInit();

        AddonManager.INSTANCE.preInit();

        Proxy.proxy.preInit(event);
        MessageHandler.INSTANCE.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TrainRegistry.registerTrain(EntityRollingStockCart.class, "minecart_wooden", 0);
        TrainRegistry.registerTrain(EntitySmallSteamLocomotive.class, "stream_locomotive_small", 1);

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
