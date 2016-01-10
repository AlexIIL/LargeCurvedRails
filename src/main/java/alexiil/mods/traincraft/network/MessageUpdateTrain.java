package alexiil.mods.traincraft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.TrainWorldCache;
import alexiil.mods.traincraft.api.Train;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageUpdateTrain implements IMessage, IMessageHandler<MessageUpdateTrain, IMessage> {
    private ByteBuf data;
    private int dimId = -1, trainId = -1;

    public MessageUpdateTrain() {}

    public MessageUpdateTrain(Train train) {
        this.data = Unpooled.buffer();
        train.writeToByteBuf(data);
        Entity ent = (Entity) train.parts.get(0);
        World world = ent.getEntityWorld();
        dimId = world.provider.getDimensionId();
        trainId = train.id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimId = buf.readInt();
        trainId = buf.readInt();
        int length = buf.readInt();
        data = buf.readBytes(length);
        TrainCraft.trainCraftLog.info("MessageUpdateTrain::fromBytes | Read train " + trainId + " and " + length + " bytes of payload");
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimId);
        buf.writeInt(trainId);
        buf.writeInt(data.readableBytes());
        buf.writeBytes(data);
        TrainCraft.trainCraftLog.info("MessageUpdateTrain::toBytes | Wrote train " + trainId + " and " + data.writerIndex() + " bytes of payload");
    }

    @Override
    public IMessage onMessage(MessageUpdateTrain message, MessageContext ctx) {
        /* Run this on the main thread otherwise the world will not have synchronized with the netty thread */
        Minecraft.getMinecraft().addScheduledTask(() -> {
            TrainCraft.trainCraftLog.info("MessageUpdateTrain::onMessage | Recieved an update for train " + message.trainId + " in " + message.data
                    .readableBytes() + " bytes");
            TrainWorldCache.INSTANCE.recieveUpdateMessage(message.dimId, message.trainId, message.data);
        });
        return null;
    }
}
