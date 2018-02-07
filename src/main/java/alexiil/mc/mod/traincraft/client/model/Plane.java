package alexiil.mc.mod.traincraft.client.model;

import javax.vecmath.Vector3d;

import net.minecraft.util.math.Vec3d;

public class Plane {
    public final Vec3d point, normal;

    public Plane(Vec3d onPlane, Vec3d normal) {
        point = onPlane;
        this.normal = normal.normalize();
    }

    public Split getSplit(Line line) {
        Face a = getSide(line.start);
        Face b = getSide(line.end);
        if (a == b) return a == Face.IN_PLANE ? Split.IN_PLANE : Split.NOT_TOUCHING;
        if (a == Face.IN_PLANE || b == Face.IN_PLANE) return Split.TOUCHES_PLANE;
        return Split.PASSES_THROUGH_PLANE;
    }

    public Face getSide(Vec3d point) {
        Vector3d pMinusPlaneP = convertToMutable(point);
        pMinusPlaneP.sub(convertToMutable(this.point));
        double dot = pMinusPlaneP.dot(convertToMutable(normal));
        if (Math.abs(dot) < 1E-4) return Face.IN_PLANE;
        if (dot > 0) return Face.TOWARDS;
        return Face.AWAY;
    }

    private static Vector3d convertToMutable(Vec3d p) {
        return new Vector3d(p.x, p.y, p.z);
    }

    /** Gets a point that is contained by the line, but only if the line returns {@link Split#PASSES_THROUGH_PLANE} from
     * {@link #getSplit(Line)}
     * 
     * @return a point that returns {@value Face#IN_PLANE} when passed to {@link #getSide(Vec3d)} */
    public Interpolation getOnPlane(Line line) {
        if (getSplit(line) != Split.PASSES_THROUGH_PLANE) throw new IllegalArgumentException("The line did not pass through the plane!");
        Vector3d linePoint = convertToMutable(line.start);
        Vector3d lineDirection = convertToMutable(line.end.subtract(line.start));

        Vector3d normal = convertToMutable(this.normal);
        double lDotN = lineDirection.dot(normal);
        Vector3d pointMinusL = new Vector3d(convertToMutable(point));
        pointMinusL.sub(linePoint);
        double pointMinusLdotN = pointMinusL.dot(normal);

        double interp = Math.abs(pointMinusLdotN / lDotN);
        Vec3d pos = line.start.add(scale(line.end.subtract(line.start), interp));
        return new Interpolation(line, pos, interp);
    }

    public static class Line {
        public final Vec3d start, end;

        public Line(Vec3d start, Vec3d end) {
            this.start = start;
            this.end = end;
        }

        public static Line createLongLine(Vec3d start, Vec3d direction) {
            return new Line(start, scale(direction, 1024));
        }

        public Vec3d interpolate(double interp) {
            return scale(start, 1 - interp).add(scale(end, interp));
        }
    }

    private static Vec3d scale(Vec3d vec, double by) {
        return new Vec3d(vec.x * by, vec.y * by, vec.z * by);
    }

    public static class Interpolation {
        public final Line lineFrom;
        public final Vec3d point;
        public final double interp;

        public Interpolation(Line lineFrom, Vec3d point, double interp) {
            this.lineFrom = lineFrom;
            this.point = point;
            this.interp = interp;
        }
    }

    public enum Face {
        TOWARDS,
        IN_PLANE,
        AWAY
    }

    public enum Split {
        NOT_TOUCHING,
        IN_PLANE,
        PASSES_THROUGH_PLANE,
        TOUCHES_PLANE
    }
}
