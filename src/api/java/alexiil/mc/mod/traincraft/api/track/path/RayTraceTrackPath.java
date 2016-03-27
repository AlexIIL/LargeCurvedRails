package alexiil.mc.mod.traincraft.api.track.path;

import net.minecraft.util.Vec3;

public class RayTraceTrackPath {
    /** The original path that the ray trace was perfomed on. */
    public final ITrackPath path;
    public final double interp;
    public final Vec3 closestPoint;
    public final double distance;

    public RayTraceTrackPath(ITrackPath path, double interp, Vec3 closestPoint, double distance) {
        this.path = path;
        this.interp = interp;
        this.closestPoint = closestPoint;
        this.distance = distance;
    }
}
