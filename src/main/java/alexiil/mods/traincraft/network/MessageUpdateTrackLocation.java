package alexiil.mods.traincraft.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;

import io.netty.buffer.ByteBuf;

public class MessageUpdateTrackLocation implements IMessage, IMessageHandler<MessageUpdateTrackLocation, MessageUpdateTrackLocation> {
    private int entityID, componentID;
    private TrackIdentifier ident;
    private float progress;

    public MessageUpdateTrackLocation() {}

    public MessageUpdateTrackLocation(int entID, int componentID, TrackIdentifier ident, float progress) {
        this.entityID = entID;
        this.componentID = componentID;
        this.ident = ident;
        this.progress = progress;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        componentID = buf.readInt();
        if (buf.readBoolean()) ident = TrackIdentifier.deserialize(buf);
        progress = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
        buf.writeInt(componentID);
        buf.writeBoolean(ident != null);
        if (ident != null) ident.serializeBuf(buf);
        buf.writeFloat(progress);
    }

    @Override
    public MessageUpdateTrackLocation onMessage(MessageUpdateTrackLocation message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return null;
        Entity ent = world.getEntityByID(entityID);
        if (ent == null || !(ent instanceof EntityGenericRollingStock)) return null;
        EntityGenericRollingStock stock = (EntityGenericRollingStock) ent;
        stock.recieveMessageUpdateTrackLocation(message.componentID, message.ident, message.progress);
        return null;
    }
}
