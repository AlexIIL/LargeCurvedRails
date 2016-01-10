package alexiil.mods.traincraft.api.component;

import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.ITrackPath;

/** A component that rests ontop of other components, ultimatly resting on {@link ComponentTrackFollower}. */
public abstract class ComponentResting implements IComponent {
    private final IRollingStock stock;
    protected final IComponent childFront, childBack;
    protected final double frontBack;

    public ComponentResting(IRollingStock stock, IComponent childFront, IComponent childBack, double frontBack) {
        this.stock = stock;
        if (childFront == null) throw new NullPointerException("childFront");
        this.childFront = childFront;
        if (childBack == null) throw new NullPointerException("childBack");
        this.childBack = childBack;
        this.frontBack = frontBack;
    }

    @Override
    public IRollingStock stock() {
        return stock;
    }

    @Override
    public double originOffset() {
        return 0;
    }

    @Override
    public void alignTo(Vec3 position, Vec3 direction, ITrackPath path) {
        childFront.alignTo(position.add(scale(direction, childFront.originOffset())), direction, path);
        childBack.alignTo(position.add(scale(direction, childBack.originOffset())), direction, path);
    }

    @Override
    public void tick() {
        childFront.tick();
        childBack.tick();
    }

    @Override
    public Vec3 getTrackPos(float partialTicks) {
        Vec3 front = scale(childFront.getTrackPos(partialTicks), frontBack);
        Vec3 back = scale(childBack.getTrackPos(partialTicks), 1 - frontBack);
        return front.add(back);
    }

    private static Vec3 scale(Vec3 v, double s) {
        return new Vec3(v.xCoord * s, v.yCoord * s, v.zCoord * s);
    }

    @Override
    public Vec3 getTrackDirection(float partialTicks) {
        Vec3 front = childFront.getTrackPos(partialTicks);
        Vec3 back = childBack.getTrackPos(partialTicks);
        return back.subtract(front).normalize();
    }
}
