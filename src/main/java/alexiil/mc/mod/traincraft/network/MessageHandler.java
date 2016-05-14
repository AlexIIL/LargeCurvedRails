package alexiil.mc.mod.traincraft.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public enum MessageHandler {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    public void preInit() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("TrainCraft");
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }
}
