package alexiil.mods.traincraft.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import alexiil.mods.traincraft.TrainWorldCache;

import io.netty.buffer.ByteBuf;

public class MessageDeleteTrain implements IMessage, IMessageHandler<MessageDeleteTrain, IMessage> {
    private int worldDimId, trainId;

    // Used for the handler and message creation
    public MessageDeleteTrain() {}

    public MessageDeleteTrain(int worldDimId, int trainId) {
        this.worldDimId = worldDimId;
        this.trainId = trainId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        worldDimId = buf.readInt();
        trainId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(worldDimId);
        buf.writeInt(trainId);
    }

    @Override
    public IMessage onMessage(MessageDeleteTrain message, MessageContext ctx) {
        TrainWorldCache.INSTANCE.deleteTrainIfUnused(message.worldDimId, message.trainId);
        return null;
    }
}
