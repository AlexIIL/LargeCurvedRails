package alexiil.mods.traincraft.api;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class TrackPath2DArc implements ITrackPath {
    private final BlockPos creator;
    private final Vec3 center;
    private final double radius, start, end, length;

    public TrackPath2DArc(BlockPos creator, Vec3 center, double radius, double start, double end) {
        this.creator = creator;
        this.center = center;
        this.radius = radius;
        this.start = start;
        this.end = end;
        this.length = approximateLength();
    }

    private TrackPath2DArc(BlockPos creator, Vec3 center, double radius, double start, double end, double length) {
        this.creator = creator;
        this.center = center;
        this.radius = radius;
        this.start = start;
        this.end = end;
        this.length = length;
    }

    @Override
    public BlockPos creatingBlock() {
        return creator;
    }

    @Override
    public Vec3 interpolate(double position) {
        float angle = (float) (start * (1 - position) + end * position);
        Vec3 vec = new Vec3(radius * MathHelper.cos(angle), 0, radius * MathHelper.sin(angle));
        return center.add(vec).add(new Vec3(creator));
    }

    @Override
    public Vec3 direction(double position) {
        float angle = (float) (start * (1 - position) + end * position);
        Vec3 vec = new Vec3(MathHelper.sin(angle), 0, MathHelper.cos(angle));
        return vec.normalize();
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(BlockPos pos) {
        return new TrackPath2DArc(creator.add(pos), center, radius, start, end, length);
    }

    private double approximateLength() {
        /* Approximation of the length of any curve. Larger values of "num" will produce more accurate results. Because
         * this is not called very often (the main curves are cached by the correct classes, so each unique curve
         * computes this once) this is set to a large value. */
        double length = 0;
        int num = 1024;
        for (int i = 1; i < num; i++) {
            Vec3 p0 = interpolate((i - 1) / (double) num);
            Vec3 p1 = interpolate(i / (double) num);
            length += p0.distanceTo(p1);
        }
        return length;
    }
}
