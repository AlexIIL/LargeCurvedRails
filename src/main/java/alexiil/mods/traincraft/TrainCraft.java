package alexiil.mods.traincraft;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.entity.EntityRollingStockPulled;
import alexiil.mods.traincraft.network.MessageHandler;

@Mod(modid = DefaultProps.MODID, name = "TrainCraft")
public class TrainCraft {

    public static Logger trainCraftLog;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        trainCraftLog = event.getModLog();

        TrainCraftAPI.WORLD_CACHE = TrainWorldCache.INSTANCE;
        TrainCraftAPI.MOVEMENT_MANAGER = TrainMovementManager.INSTANCE;
        TCBlocks.preInit(event);
        Proxy.proxy.preInit(event);
        MessageHandler.INSTANCE.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TrainRegistry.registerTrain(EntityRollingStockPulled.class, "rolling_stock_pulled");

        Proxy.proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        Proxy.proxy.postInit(event);
    }
}