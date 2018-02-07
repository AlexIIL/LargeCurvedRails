package alexiil.mc.mod.traincraft.block;

import java.util.Locale;

import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Vec3;

import io.netty.buffer.ByteBuf;

public enum EnumDirection implements IStringSerializable {
    // AXIS ALIGNED SECTIONS
    NORTH_SOUTH(EnumFacing.NORTH, EnumFacing.SOUTH),
    EAST_WEST(EnumFacing.EAST, EnumFacing.WEST),
    // DIAGONAL DIRECTIONS
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST);

    public final EnumFacing from, to;
    public final Vec3 vecFrom, vecTo;
    public final TrackPathStraight path;

    private EnumDirection(EnumFacing from, EnumFacing to) {
        this.from = from;
        this.to = to;
        // @formatter:off
        vecFrom = new Vec3(
                from.getAxis() == Axis.X ? (from.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5, BlockAbstractTrack.TRACK_HEIGHT, 
                from.getAxis() == Axis.Z ? (from.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5);
        vecTo = new Vec3(
                to.getAxis() == Axis.X ? (to.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5, BlockAbstractTrack.TRACK_HEIGHT, 
                to.getAxis() == Axis.Z ? (to.getAxisDirection().getOffset() * .5 + 0.5) : 0.5);
        // @formatter:on
        path = new TrackPathStraight(vecFrom, vecTo, BlockPos.ORIGIN);
    }

    static {
        BlockPos creator = new BlockPos(0, 0, 0);

        Vec3 north = new Vec3(0.5, BlockAbstractTrack.TRACK_HEIGHT, 0);
        Vec3 south = new Vec3(0.5, BlockAbstractTrack.TRACK_HEIGHT, 1);
        Vec3 west = new Vec3(0, BlockAbstractTrack.TRACK_HEIGHT, 0.5);
        Vec3 east = new Vec3(1, BlockAbstractTrack.TRACK_HEIGHT, 0.5);

        // Testing assertions. Actually very useful.
        assert NORTH_SOUTH.path.equals(new TrackPathStraight(north, south, creator));
        assert EAST_WEST.path.equals(new TrackPathStraight(east, west, creator));

        assert NORTH_EAST.path.equals(new TrackPathStraight(north, east, creator));
        assert NORTH_WEST.path.equals(new TrackPathStraight(north, west, creator));
        assert SOUTH_EAST.path.equals(new TrackPathStraight(south, east, creator));
        assert SOUTH_WEST.path.equals(new TrackPathStraight(south, west, creator));
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public void serializeBuf(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        packet.writeEnumValue(this);
    }

    public static EnumDirection deserializeBuf(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        return packet.readEnumValue(EnumDirection.class);
    }
}