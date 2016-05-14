package alexiil.mc.mod.traincraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import alexiil.mc.mod.traincraft.tile.TileTrackAscendingPointer;

public abstract class Proxy {
    @SidedProxy(clientSide = "alexiil.mc.mod.traincraft.ProxyClient", serverSide = "alexiil.mc.mod.traincraft.ProxyServer")
    public static Proxy proxy;

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.registerTileEntity(TileTrackAscendingPointer.class, "traincraft:tile.track.ascending_pointer");
        // GameRegistry.registerTileEntity(TileTrackMultiple.class, "traincraft:tile.track.multiple");
        // GameRegistry.registerTileEntity(TileTrackMultiplePointer.class, "traincraft:tile.track.multiple_pointer");
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}
}
