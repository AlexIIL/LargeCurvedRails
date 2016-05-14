package alexiil.mc.mod.traincraft.api.track.path;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;

public interface ITrackPath {
    /** Gets a Vec3d position that has been interpolated between
     * 
     * @param position A position between (and including) 0 and 1. Other values might not return what you expected. */
    Vec3d interpolate(double position);

    /** Get the direction of the track path at a particular interpolated point. */
    Vec3d direction(double position);

    default Vec3d start() {
        return interpolate(0);
    }

    default Vec3d end() {
        return interpolate(1);
    }

    /** Reverses the track path. Useful if the start is the opposite end of what you need. Should just invert the result
     * of {@link #interpolate(double)}. */
    default ITrackPath reverse() {// DIRECTIONS ARE INCORRRECT IN SOME WAY
        return new TrackPathReversed(this);
    }

    /** @return The length of this path. Straight sections will be equal to the distance between {@link #start()} and
     *         {@link #end()}. Curved sections will be longer. */
    double length();

    public static Vec3d interpolate(Vec3d start, Vec3d end, double position) {
        double x = start.xCoord * (1 - position) + end.xCoord * position;
        double y = start.yCoord * (1 - position) + end.yCoord * position;
        double z = start.zCoord * (1 - position) + end.zCoord * position;
        return new Vec3d(x, y, z);
    }

    default TrackBehaviour creatingBehaviour() {
        return null;
    }

    /** Will attempt to find the best progress value that, when fed to {@link #interpolate(double)} returns the vector
     * that is closest to lastPlace */
    default double progress(Vec3d lastPlace) {
        double pointP = 0.5;
        for (int i = 3; i < 20; i++) {
            double diff = 2 / (double) i;
            double aboveDiff = interpolate(pointP + 1e-30).squareDistanceTo(lastPlace);
            double belowDiff = interpolate(pointP - 1e-30).squareDistanceTo(lastPlace);
            if (aboveDiff > belowDiff) pointP -= diff;
            else pointP += diff;
        }
        return pointP;
    }

    /** Renderers information to inform the debug screen of what makes this path a path.
     * 
     * @param wr The world renderer that has been set up to render with {@link DefaultVertexFormats#POSITION_COLOR} and
     *            {@link GL11#GL_LINES} */
    @SideOnly(Side.CLIENT)
    default void renderInfo(VertexBuffer vb) {}

    default RayTraceTrackPath rayTrace(Vec3d start, Vec3d direction) {
        double ia = 0, ib = 1;
        double da = 0, db = 0;
        double id = 0.5;
        Vec3d va, vb;

        RayTraceTrackPath best = null;
        for (int i = 0; i < 10; i++) {
            Vec3d a = interpolate(ia);
            Vec3d b = interpolate(ib);
            va = closestPointOnLineToPoint(a, start, direction);
            vb = closestPointOnLineToPoint(b, start, direction);
            da = a.squareDistanceTo(va);
            db = b.squareDistanceTo(vb);
            if (da < db) {
                // We work out the square root at the end to get the actual distance
                best = new RayTraceTrackPath(this, ia, a, da);
                ib -= id;
            } else /* if (db < da) */ {
                // We work out the square root at the end to get the actual distance
                best = new RayTraceTrackPath(this, ib, b, db);
                ia += id;
            }
            id /= 2.0;
        }
        if (best == null) return null;
        return new RayTraceTrackPath(this, best.interp, best.closestPoint, Math.sqrt(best.distance));
    }

    public static Vec3d closestPointOnLineToPoint(Vec3d point, Vec3d linePoint, Vec3d lineVector) {
        Vec3d v = lineVector.normalize();
        Vec3d p1 = linePoint;
        Vec3d p2 = point;

        // Its maths. Its allowed to deviate from normal naming rules.
        Vec3d p2_minus_p1 = p2.subtract(p1);
        double _dot_v = MathUtil.dot(p2_minus_p1, v);
        Vec3d _scale_v = MathUtil.scale(v, _dot_v);
        return p1.add(_scale_v);
    }
}
