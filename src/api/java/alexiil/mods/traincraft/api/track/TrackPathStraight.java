package alexiil.mods.traincraft.api.track;

import java.util.Objects;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.MCObjectUtils;

/** Designates a path between any number of points. */
public class TrackPathStraight implements ITrackPath {
    private final BlockPos creator;
    private final Vec3 start, end, direction;
    private final double length;

    public TrackPathStraight(Vec3 start, Vec3 end, BlockPos creator) {
        this.creator = creator;
        this.start = start;
        this.end = end;
        this.direction = end.subtract(start).normalize();
        this.length = start.distanceTo(end);
    }

    @Override
    public Vec3 interpolate(double position) {
        return ITrackPath.interpolate(start, end, position);
    }

    @Override
    public Vec3 direction(double position) {
        return direction;
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(BlockPos by) {
        Vec3 vec3 = new Vec3(by);
        return new TrackPathStraight(start.add(vec3), end.add(vec3), creator.add(by));
    }

    @Override
    public BlockPos creatingBlock() {
        return creator;
    }

    @Override
    public int hashCode() {
        // Vec3 does not define hashCode and equals so we must use our own.
        return MCObjectUtils.hash(creator, start, end, direction, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TrackPathStraight other = (TrackPathStraight) obj;
        // @formatter:off
        return length == other.length &&
                Objects.equals(creator, other.creator) &&
                // Vec3 does not define hashCode and equals so we must use our own.
                MCObjectUtils.equals(start, other.start) &&
                MCObjectUtils.equals(end, other.end) &&
                MCObjectUtils.equals(direction, other.direction);
        // @formatter:on
    }

    @Override
    public String toString() {
        return "TrackPathStraight[" + creator + ", " + start + ", " + end + ", " + direction + ", " + length + "]";
    }
}
