package alexiil.mc.mod.traincraft.api;

import io.netty.buffer.ByteBuf;

public interface INetSerialisable {
    void serializeBuf(ByteBuf buffer);

    void deserializeBuf(ByteBuf buffer);
}
