package alexiil.mc.mod.traincraft.api.track.behaviour;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;

import net.minecraftforge.common.util.INBTSerializable;

import alexiil.mc.mod.traincraft.api.INetSerialisable;

import io.netty.buffer.ByteBuf;

public final class TrackIdentifier implements INBTSerializable<NBTTagCompound>, INetSerialisable {
    private int worldDimension;
    private BlockPos pos;
    private String trackIdentifier;
    private boolean reversed;

    private TrackIdentifier() {}

    public TrackIdentifier(int dim, BlockPos pos, String identifier) {
        this.worldDimension = dim;
        this.pos = pos;
        this.trackIdentifier = identifier;
    }

    public TrackIdentifier(TrackIdentifier old, String newName) {
        this(old.worldDimension, old.pos, newName);
    }

    public TrackIdentifier reverse() {
        reversed = !reversed;
        return this;
    }

    public static TrackIdentifier deserialize(ByteBuf buf) {
        TrackIdentifier ident = new TrackIdentifier();
        ident.deserializeBuf(buf);
        return ident;
    }

    @Override
    public void serializeBuf(ByteBuf buffer) {
        PacketBuffer buf = new PacketBuffer(buffer);
        buffer.writeInt(worldDimension);
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
        buffer.writeByte(Math.max(trackIdentifier.length(), 127));
        buf.writeString(trackIdentifier);
        buf.writeBoolean(reversed);
    }

    @Override
    public void deserializeBuf(ByteBuf buffer) {
        worldDimension = buffer.readInt();
        pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        int l = buffer.readUnsignedByte();
        trackIdentifier = new PacketBuffer(buffer).readStringFromBuffer(l);
        reversed = buffer.readBoolean();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("dim", worldDimension);
        nbt.setIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.setString("identifier", trackIdentifier);
        nbt.setBoolean("reversed", reversed);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        worldDimension = nbt.getInteger("dim");
        int[] arr = nbt.getIntArray("pos");
        if (arr.length == 3) {
            pos = new BlockPos(arr[0], arr[1], arr[2]);
        }
        trackIdentifier = nbt.getString("identifier");
        reversed = nbt.getBoolean("reversed");
    }

    public int worldDim() {
        return worldDimension;
    }

    public BlockPos pos() {
        return pos;
    }

    public String identifier() {
        return trackIdentifier;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldDimension, pos, trackIdentifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        TrackIdentifier ident = (TrackIdentifier) obj;
        if (worldDimension != ident.worldDimension) return false;
        if (!pos.equals(pos)) return false;
        if (!trackIdentifier.equals(ident.trackIdentifier)) return false;
        return true;
    }
}
