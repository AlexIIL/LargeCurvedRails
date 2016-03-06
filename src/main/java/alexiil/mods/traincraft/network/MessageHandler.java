package alexiil.mods.traincraft.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum MessageHandler {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    public void preInit() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("TrainCraft");
        wrapper.registerMessage(messageHandler, requestMessageType, 0, Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }
}
