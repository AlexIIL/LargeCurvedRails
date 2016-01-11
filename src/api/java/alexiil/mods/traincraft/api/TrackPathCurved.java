package alexiil.mods.traincraft.api;

import java.util.Objects;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

/** Implements a Bézier curve (quadratic) to create a path that curves infinitly. */
public class TrackPathCurved implements ITrackPath {
    private final BlockPos creator;
    private final Vec3 start, bezOffset, end;
    private final double length;

    public TrackPathCurved(BlockPos creator, Vec3 start, Vec3 bezOffset, Vec3 end) {
        this.creator = creator;
        this.start = start;
        this.bezOffset = bezOffset;
        this.end = end;
        this.length = approximateLength();
    }

    private TrackPathCurved(BlockPos creator, Vec3 start, Vec3 bezOffset, Vec3 end, double length) {
        this.creator = creator;
        this.start = start;
        this.bezOffset = bezOffset;
        this.end = end;
        this.length = length;
    }

    @Override
    public Vec3 interpolate(double position) {
        Vec3 p0 = ITrackPath.interpolate(start, bezOffset, position);
        Vec3 p1 = ITrackPath.interpolate(bezOffset, end, position);
        return ITrackPath.interpolate(p0, p1, position);
    }

    @Override
    public Vec3 direction(double position) {
        Vec3 p0 = ITrackPath.interpolate(start, bezOffset, position);
        Vec3 p1 = ITrackPath.interpolate(bezOffset, end, position);
        return p1.subtract(p0).normalize();
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(BlockPos by) {
        Vec3 vec = new Vec3(by);
        // Pass the length to the offset version as its quite expensive to compute
        return new TrackPathCurved(creator.add(by), start.add(vec), bezOffset.add(vec), end.add(vec), length);
    }

    private double approximateLength() {
        /* Approximation of the length of any curve. Larger values of "num" will produce more accurate results. Because
         * this is not called very often (the main curves are cached by the correct classes, so each unique curve
         * computes this once) this is set to a large value.. */
        double length = 0;
        int num = 1024;
        for (int i = 1; i < num; i++) {
            Vec3 p0 = interpolate((i - 1) / (double) num);
            Vec3 p1 = interpolate(i / (double) num);
            length += p0.distanceTo(p1);
        }
        return length;
    }

    @Override
    public BlockPos creatingBlock() {
        return creator;
    }

    @Override
    public int hashCode() {
        // Vec3 does not define hashCode and equals so we must use our own.
        return MCObjectUtils.hash(bezOffset, creator, end, length, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TrackPathCurved other = (TrackPathCurved) obj;
        // @formatter:off
        return length == other.length &&
                Objects.equals(creator, other.creator) &&
                // Vec3 does not define hashCode and equals so we must use our own.
                MCObjectUtils.equals(start, other.start) &&
                MCObjectUtils.equals(bezOffset, other.bezOffset) &&
                MCObjectUtils.equals(end, other.end);
        // @formatter:on
    }

    @Override
    public String toString() {
        return "TrackPathCurved[" + creator + ", " + start + ", " + bezOffset + ", " + end + ", " + length + "]";
    }
}
