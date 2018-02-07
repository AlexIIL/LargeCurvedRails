package alexiil.mc.mod.traincraft.api.track.path;

import java.util.Objects;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TrackPath2DArc implements ITrackPath {
    private final Vec3d center;
    private final double radius, angleStart, angleEnd, length;

    public static TrackPath2DArc createDegrees(Vec3d center, double radius, int start, int end) {
        return new TrackPath2DArc(center, radius, start * Math.PI / 180, end * Math.PI / 180);
    }

    public TrackPath2DArc(Vec3d center, double radius, double start, double end) {
        this.center = center;
        this.radius = radius;
        this.angleStart = start;
        this.angleEnd = end;
        this.length = radius * Math.abs(angleStart - angleEnd);
    }

    @Override
    public Vec3d interpolate(double position) {
        float angle = (float) (angleStart * (1 - position) + angleEnd * position);
        Vec3d vec = new Vec3d(radius * MathHelper.cos(angle), 0, radius * MathHelper.sin(angle));
        return center.add(vec);
    }

    @Override
    public Vec3d direction(double position) {
        if (position < 1) return interpolate(position + 0.01).subtract(interpolate(position)).normalize();

        // WARNING: MATHS!

        // (A differencial equation for the above function)

        float o = (float) position;
        float sa = (float) angleStart;
        float ea = (float) angleEnd;
        float sr = (float) radius;
        float er = (float) radius;

        float a = sa + o * (ea - sa);
        float da_do = ea - sa;

        float r = sr + o * (er - sr);
        // float f = MathHelper.cos(a);
        float df_do = -MathHelper.sin(a) * da_do;

        // float x = r * f;
        float dx_do = r * df_do;

        // float g = MathHelper.sin(a);
        float dg_do = MathHelper.cos(a) * da_do;

        // float z = r * g;
        float dz_do = r * dg_do;

        return new Vec3d(dx_do, 0, dz_do).normalize();
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInfo(BufferBuilder bb) {
        Vec3d c = center;

        Vec3d s = start();
        bb.pos(c.x, c.y, c.z).color(0, 0, 255, 255).endVertex();
        bb.pos(s.x, s.y, s.z).color(0, 0, 255, 255).endVertex();

        Vec3d e = end();
        bb.pos(c.x, c.y, c.z).color(0, 0, 255, 255).endVertex();
        bb.pos(e.x, e.y, e.z).color(0, 0, 255, 255).endVertex();

        bb.pos(c.x, c.y, c.z).color(255, 255, 0, 255).endVertex();
        bb.pos(c.x, c.y + 0.3, c.z).color(255, 255, 0, 255).endVertex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius, angleStart, angleEnd);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        TrackPath2DArc arc = (TrackPath2DArc) obj;
        // @formatter:off
        return Objects.equals(center, arc.center) &&
                radius == arc.radius &&
                angleStart == arc.angleStart &&
                angleEnd == arc.angleEnd &&
                length == arc.length;
        // @formatter:on
    }
}
