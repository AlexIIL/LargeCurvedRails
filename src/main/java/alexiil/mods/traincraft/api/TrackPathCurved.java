package alexiil.mods.traincraft.api;

import javax.vecmath.Vector3d;

import net.minecraft.util.Vec3;

/** Implements a Bézier curve (quadratic) to create a path that curves infinitly. */
public class TrackPathCurved implements ITrackPath {
    private final Vec3 start, bezOffset, end;
    private final double length;

    public TrackPathCurved(Vec3 start, Vec3 bezOffset, Vec3 end) {
        this.start = start;
        this.bezOffset = bezOffset;
        this.end = end;
        this.length = bezLength();
    }

    @Override
    public Vec3 interpolate(double position) {
        ITrackPath.checkInterp(position);
        Vec3 p0 = ITrackPath.interpolate(start, bezOffset, position);
        Vec3 p1 = ITrackPath.interpolate(bezOffset, end, position);
        return ITrackPath.interpolate(p0, p1, position);
    }

    @Override
    public Vec3 direction(double position) {
        ITrackPath.checkInterp(position);
        Vec3 p0 = ITrackPath.interpolate(start, bezOffset, position);
        Vec3 p1 = ITrackPath.interpolate(bezOffset, end, position);
        return p1.subtract(p0).normalize();
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(Vec3 by) {
        return new TrackPathCurved(start.add(by), bezOffset.add(by), end.add(by));
    }

    private double bezLength() {
        /* Implementation of the integral of a curve length algorithm. Pretty expensive so then this will probably want
         * to be optimised in some way. */
        Vector3d a = new Vector3d(), b = new Vector3d();
        a.x = start.xCoord - 2 * bezOffset.xCoord + end.xCoord;
        a.y = start.yCoord - 2 * bezOffset.yCoord + end.yCoord;
        a.z = start.zCoord - 2 * bezOffset.zCoord + end.zCoord;

        b.x = 2 * bezOffset.xCoord - 2 * start.xCoord;
        b.y = 2 * bezOffset.yCoord - 2 * start.yCoord;
        b.z = 2 * bezOffset.zCoord - 2 * start.zCoord;

        double A = 4 * (a.x * a.x + a.y * a.y);
        double B = 4 * (a.x * b.x + a.y * b.y);
        double C = b.x * b.x + b.y * b.y;

        double Sabc = 2 * Math.sqrt(A + B + C);
        double A_2 = Math.sqrt(A);
        double A_32 = 2 * A * A_2;
        double C_2 = 2 * Math.sqrt(C);
        double BA = B / A_2;

        return (A_32 * Sabc + A_2 * B * (Sabc - C_2) + (4 * C * A - B * B) * Math.log((2 * A_2 + BA + Sabc) / (BA + C_2))) / (4 * A_32);
    }
}
