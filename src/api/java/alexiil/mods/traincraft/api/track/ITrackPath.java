package alexiil.mods.traincraft.api.track;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.lib.MathUtil;

public interface ITrackPath {
    /** Gets a Vec3 position that has been interpolated between
     * 
     * @param position A position between (and including) 0 and 1. Other values might not return what you expected. */
    Vec3 interpolate(double position);

    /** Get the direction of the track path at a particular interpolated point. */
    Vec3 direction(double position);

    default Vec3 start() {
        return interpolate(0);
    }

    default Vec3 end() {
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

    /** Offsets this track path by the given amount. Useful for caching a single path object (that works out the length)
     * and offsetting it for every block position that exists. */
    ITrackPath offset(BlockPos pos);

    public static Vec3 interpolate(Vec3 start, Vec3 end, double position) {
        double x = start.xCoord * (1 - position) + end.xCoord * position;
        double y = start.yCoord * (1 - position) + end.yCoord * position;
        double z = start.zCoord * (1 - position) + end.zCoord * position;
        return new Vec3(x, y, z);
    }

    /** @return The block position that created this path */
    BlockPos creatingBlock();

    /** Will attempt to find the best progress value that, when fed to {@link #interpolate(double)} returns the vector
     * that is closest to lastPlace */
    default double progress(Vec3 lastPlace) {
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
    default void renderInfo(WorldRenderer wr) {}

    default RayTraceTrackPath rayTrace(Vec3 start, Vec3 direction) {
        double ia = 0, ib = 1;
        double da = 0, db = 0, dBest = Double.MAX_VALUE;
        double id = 0.5;
        Vec3 va, vb;

        RayTraceTrackPath best = null;
        for (int i = 0; i < 10; i++) {
            Vec3 a = interpolate(ia);
            Vec3 b = interpolate(ib);
            va= closestPointOnLineToPoint(a, start, direction);
            vb= closestPointOnLineToPoint(b, start, direction);
            da = a.squareDistanceTo(va);
            db = b.squareDistanceTo(vb);
            if (da < db) {
                if (best == null || da < dBest) {
                    dBest = da;
                    // We work out the square root at the end to get the actual distance
                    best = new RayTraceTrackPath(this, ia, a, da);
                    ib -= id;
                } else if (dBest < da) {
                    // Um... wtf? lets just ignore that...
                    ib -= id;
                }
            } else /* if (db < da) */ {
                if (best == null || db < dBest) {
                    dBest = db;
                    // We work out the square root at the end to get the actual distance
                    best = new RayTraceTrackPath(this, ib, b, db);
                    ia += id;
                } else if (dBest < da) {
                    // Um... wtf? lets just ignore that...
                    ia += id;
                }
            }
            id /= 2.0;
        }
        if (best == null) return null;
        return new RayTraceTrackPath(this, best.interp, best.closestPoint, Math.sqrt(best.distance));
    }

    public static Vec3 closestPointOnLineToPoint(Vec3 point, Vec3 linePoint, Vec3 lineVector) {
        Vec3 v = lineVector.normalize();
        Vec3 p1 = linePoint;
        // point from
        Vec3 p2 = point;
        //                (P2-P1)dot(v)
        // Pr = P1 + ------------- * v
        //                      v

        // Its maths. Its allowed to deviate from normal naming rules.
        Vec3 p2_minus_p1 = p2.subtract(p1);
        double _dot_v = MathUtil.dot(p2_minus_p1, v);
        Vec3 _scale_v = MathUtil.scale(v, _dot_v);
        return p1.add(_scale_v);
    }
}
