package alexiil.mods.traincraft.api;

import java.util.Objects;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TrackPath2DArc implements ITrackPath {
    private final BlockPos creator;
    private final Vec3 center;
    private final double radiusStart, radiusEnd, angleStart, angleEnd, length;

    public static TrackPath2DArc createDegrees(BlockPos creator, Vec3 center, double radiusStart, double endRadius, int start, int end) {
        return new TrackPath2DArc(creator, center, radiusStart, endRadius, start * Math.PI / 180, end * Math.PI / 180);
    }

    public TrackPath2DArc(BlockPos creator, Vec3 center, double radiusStart, double radiusEnd, double start, double end) {
        this.creator = creator;
        this.center = center;
        this.radiusStart = radiusStart;
        this.radiusEnd = radiusEnd;
        this.angleStart = start;
        this.angleEnd = end;
        this.length = approximateLength();
    }

    private TrackPath2DArc(BlockPos creator, Vec3 center, double radiusStart, double radiusEnd, double start, double end, double length) {
        this.creator = creator;
        this.center = center;
        this.radiusStart = radiusStart;
        this.radiusEnd = radiusEnd;
        this.angleStart = start;
        this.angleEnd = end;
        this.length = length;
    }

    @Override
    public BlockPos creatingBlock() {
        return creator;
    }

    @Override
    public Vec3 interpolate(double position) {
        float angle = (float) (angleStart * (1 - position) + angleEnd * position);
        double radius = (radiusStart * (1 - position) + radiusEnd * position);
        Vec3 vec = new Vec3(radius * MathHelper.cos(angle), 0, radius * MathHelper.sin(angle));
        return center.add(vec).add(new Vec3(creator));
    }

    @Override
    public Vec3 direction(double position) {
        float angle = (float) (angleStart * (1 - position) + angleEnd * position);
        double radius = (radiusStart * (1 - position) + radiusEnd * position);
        Vec3 vec = new Vec3(radius * -MathHelper.sin(angle), 0, radius * MathHelper.cos(angle));
        return vec.normalize();
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public ITrackPath offset(BlockPos pos) {
        return new TrackPath2DArc(creator.add(pos), center, radiusStart, radiusEnd, angleStart, angleEnd, length);
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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInfo(WorldRenderer wr) {
        Vec3 c = center.add(new Vec3(creator));

        Vec3 s = start();
        wr.pos(c.xCoord, c.yCoord, c.zCoord).color(0, 0, 255, 255).endVertex();
        wr.pos(s.xCoord, s.yCoord, s.zCoord).color(0, 0, 255, 255).endVertex();

        Vec3 e = end();
        wr.pos(c.xCoord, c.yCoord, c.zCoord).color(0, 0, 255, 255).endVertex();
        wr.pos(e.xCoord, e.yCoord, e.zCoord).color(0, 0, 255, 255).endVertex();

        wr.pos(c.xCoord, c.yCoord, c.zCoord).color(255, 255, 0, 255).endVertex();
        wr.pos(c.xCoord, c.yCoord + 0.3, c.zCoord).color(255, 255, 0, 255).endVertex();
    }

    @Override
    public int hashCode() {
        return MCObjectUtils.hash(creator, center, radiusStart, radiusEnd, angleStart, angleEnd);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        TrackPath2DArc arc = (TrackPath2DArc) obj;
        // @formatter:off
        return Objects.equals(creator, arc.creator) && 
                MCObjectUtils.equals(center, arc.center) &&
                radiusStart == arc.radiusStart &&
                radiusEnd == arc.radiusEnd &&
                angleStart == arc.angleStart &&
                angleEnd == arc.angleEnd &&
                length == arc.length;
        // @formatter:on
    }
}
