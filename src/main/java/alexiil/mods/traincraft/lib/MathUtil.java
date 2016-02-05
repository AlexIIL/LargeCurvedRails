package alexiil.mods.traincraft.lib;

import net.minecraft.util.Vec3;

public class MathUtil {
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
}
