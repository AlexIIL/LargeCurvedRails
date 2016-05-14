package alexiil.mc.mod.traincraft.block;

import java.util.Locale;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.Vec3d;

import alexiil.mc.mod.traincraft.api.track.path.TrackPathStraight;

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
    public final Vec3d vecFrom, vecTo;
    public final TrackPathStraight path;

    public static final EnumDirection[] VALUES = values();

    private EnumDirection(EnumFacing from, EnumFacing to) {
        this.from = from;
        this.to = to;
        // @formatter:off
        vecFrom = new Vec3d(
                from.getAxis() == Axis.X ? (from.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5, BlockAbstractTrack.TRACK_HEIGHT, 
                from.getAxis() == Axis.Z ? (from.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5);
        vecTo = new Vec3d(
                to.getAxis() == Axis.X ? (to.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5, BlockAbstractTrack.TRACK_HEIGHT, 
                to.getAxis() == Axis.Z ? (to.getAxisDirection().getOffset() * 0.5 + 0.5) : 0.5);
        // @formatter:on
        path = new TrackPathStraight(vecFrom, vecTo);
    }

    static {
        Vec3d north = new Vec3d(0.5, BlockAbstractTrack.TRACK_HEIGHT, 0);
        Vec3d south = new Vec3d(0.5, BlockAbstractTrack.TRACK_HEIGHT, 1);
        Vec3d west = new Vec3d(0, BlockAbstractTrack.TRACK_HEIGHT, 0.5);
        Vec3d east = new Vec3d(1, BlockAbstractTrack.TRACK_HEIGHT, 0.5);

        // Testing assertions. Actually very useful.
        assert NORTH_SOUTH.path.equals(new TrackPathStraight(north, south));
        assert EAST_WEST.path.equals(new TrackPathStraight(east, west));

        assert NORTH_EAST.path.equals(new TrackPathStraight(north, east));
        assert NORTH_WEST.path.equals(new TrackPathStraight(north, west));
        assert SOUTH_EAST.path.equals(new TrackPathStraight(south, east));
        assert SOUTH_WEST.path.equals(new TrackPathStraight(south, west));
    }

    public static EnumDirection fromMeta(int meta) {
        return VALUES[((meta % 6) + 6) % 6];
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
