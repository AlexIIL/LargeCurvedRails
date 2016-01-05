package alexiil.mods.traincraft.api;

import net.minecraft.util.Vec3;

/** Designates a path between any number of points. */
public class TrackPathStraight implements ITrackPath {
    private final Vec3 start, end, direction;
    private final double length;

    public TrackPathStraight(Vec3 start, Vec3 end) {
        this.start = start;
        this.end = end;
        this.direction = end.subtract(start).normalize();
        this.length = start.distanceTo(end);
    }

    @Override
    public Vec3 interpolate(double position) {
        ITrackPath.checkInterp(position);
        return ITrackPath.interpolate(start, end, position);
    }

    @Override
    public Vec3 direction(double position) {
        // We won't bother to throw here because its a straight line
        return direction;
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(Vec3 by) {
        return new TrackPathStraight(start.add(by), end.add(by));
    }
}
