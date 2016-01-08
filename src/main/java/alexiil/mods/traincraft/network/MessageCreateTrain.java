package alexiil.mods.traincraft.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import alexiil.mods.traincraft.api.Train;
import alexiil.mods.traincraft.api.TrainCraftAPI;

import io.netty.buffer.ByteBuf;

public class MessageCreateTrain implements IMessage, IMessageHandler<MessageCreateTrain, IMessage> {
    private Train train;

    // Used for the handler and message creation
    public MessageCreateTrain() {}

    public MessageCreateTrain(Train train) {
        this.train = train;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        train = Train.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        train.writeToByteBuf(buf);
    }

    @Override
    public IMessage onMessage(MessageCreateTrain message, MessageContext ctx) {
        if (message.train != null) TrainCraftAPI.WORLD_CACHE.createTrain(message.train);
        return null;
    }
}
