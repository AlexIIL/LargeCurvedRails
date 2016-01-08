package alexiil.mods.traincraft.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum MessageHandler {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    public void preInit() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("TrainCraft");
        wrapper.registerMessage(MessageDeleteTrain.class, MessageDeleteTrain.class, 0, Side.CLIENT);
        wrapper.registerMessage(MessageCreateTrain.class, MessageCreateTrain.class, 1, Side.CLIENT);
//        wrapper.registerMessage(MessageUpdateTrain.class, MessageUpdateTrain.class, 2, Side.CLIENT);
    }
}
