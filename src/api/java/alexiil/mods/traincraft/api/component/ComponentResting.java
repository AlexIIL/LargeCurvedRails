package alexiil.mods.traincraft.api.component;

import net.minecraft.util.AxisAlignedBB;
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
    public double frictionCoefficient() {
        return childFront.frictionCoefficient() + childBack.frictionCoefficient();
    }

    @Override
    public double frontArea() {
        return childFront.frontArea();
    }

    @Override
    public void alignTo(ITrackPath around, double offset) {
        childFront.alignTo(around, offset + childFront.originOffset());
        childBack.alignTo(around, offset + childBack.originOffset());
    }

    @Override
    public final AxisAlignedBB getBoundingBox() {
        AxisAlignedBB front = childFront.getBoundingBox().offset(childFront.originOffset(), 0, 0);
        AxisAlignedBB back = childBack.getBoundingBox().offset(childBack.originOffset(), 0, 0);
        return front.union(back).union(box());
    }

    protected abstract AxisAlignedBB box();

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
