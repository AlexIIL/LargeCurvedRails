package alexiil.mods.traincraft;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.render.RenderRollingStockBase;

public class ProxyClient extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityRollingStockBase.class, RenderRollingStockBase.Factory.INSTANCE);
    }

    @SubscribeEvent
    public void modelBake(ModelBakeEvent bake) {
        RenderRollingStockBase.clearModelMap();
    }
}
