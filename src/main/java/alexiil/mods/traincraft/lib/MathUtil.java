package alexiil.mods.traincraft.lib;

import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.MCObjectUtils;

public class MathUtil {
    public static final double DEFAULT_MATH_LIMIT = 1e-10;

    public static double[] toArray(Vec3 v) {
        return new double[] { v.xCoord, v.yCoord, v.zCoord };
    }

    public static Vec3 cross(Vec3 vecA, Vec3 vecB) {
        double[] a = toArray(vecA);
        double[] b = toArray(vecB);
        double x = a[1] * b[2] - a[2] * b[1];
        double y = a[2] * b[0] - a[0] * b[2];
        double z = a[0] * b[1] - a[1] * b[0];
        return new Vec3(x, y, z);
    }

    public static Vec3 scale(Vec3 vec, double by) {
        return new Vec3(vec.xCoord * by, vec.yCoord * by, vec.zCoord * by);
    }

    public static double dot(Vec3 a, Vec3 b) {
        return a.xCoord * b.xCoord + a.yCoord * b.yCoord + a.zCoord * b.zCoord;
    }

    public static boolean roughlyEquals(double a, double b) {
        double diff = a - b;
        if (diff < 0) return diff > -DEFAULT_MATH_LIMIT;
        else return diff < DEFAULT_MATH_LIMIT;
    }

    public static double roundToClosest(double v) {
        // Basically removes the "0.4999999999213456" that comes up from rounding (changes it to 0.5)
        double approximation = 1 << 10;
        double vBig = v * approximation;
        long rounded = Math.round(vBig);
        return rounded / approximation;
    }

    public static Vec3 roundToClosest(Vec3 vec) {
        return new Vec3(roundToClosest(vec.xCoord), roundToClosest(vec.yCoord), roundToClosest(vec.zCoord));
    }

    public static Vec3 findCommonPoint(Vec3 r1, Vec3 e1, Vec3 r2, Vec3 e2) {
        // They must be normalized
        e1 = e1.normalize();
        e2 = e2.normalize();
        /* http://stackoverflow.com/questions/10551555/need-an-algorithm-for-3d-vectors-intersection
         * 
         * (The answer is repeated here) */
        // * Find the direction projection u=Dot(e1,e2)=e1x*e2x+e1y*e2y+e1z*e2z
        double u = dot(e1, e2);

        // * If u==1 then lines are parallel. No intersection exists.
        if (u == 1) throw new IllegalArgumentException("The lines are parallel!");

        // * Find the separation projections t1=Dot(r2-r1,e1) and t2=Dot(r2-r1,e2)
        double t1 = dot(r2.subtract(r1), e1);
        double t2 = dot(r2.subtract(r1), e2);

        // * Find distance along line1 d1 = (t1-u*t2)/(1-u*u)
        double d1 = (t1 - u * t2) / (1 - u * u);

        // * Find distance along line2 d2 = (t2-u*t1)/(u*u-1)
        double d2 = (t2 - u * t1) / (u * u - 1);

        // * Find the point on line1 p1=Add(r1,Scale(d1,e1))
        Vec3 p1 = roundToClosest(r1.add(scale(e1, d1)));

        // * Find the point on line2 p2=Add(r2,Scale(d2,e2))
        Vec3 p2 = roundToClosest(r2.add(scale(e2, d2)));

        if (!MCObjectUtils.equals(p1, p2)) throw new IllegalStateException(p1 + " != " + p2);
        return p1;
    }
}
