package alexiil.mods.traincraft.api.component;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.ITrackPath;

/** A component that rests ontop of other components, ultimatly resting on {@link ComponentTrackFollower}. */
public abstract class ComponentResting implements IComponent {
    private final IRollingStock stock;
    private IComponent parent;
    protected final IComponent childFront, childBack;
    protected final ImmutableList<IComponent> childMiddle;
    protected final double frontBack;

    private final boolean isBogie;

    public ComponentResting(IRollingStock stock, IComponent childFront, IComponent childBack, List<IComponent> childMiddle, double frontBack) {
        this.stock = stock;
        if (childFront == null) throw new NullPointerException("childFront");
        this.childFront = childFront;
        childFront.setParent(this);
        if (childBack == null) throw new NullPointerException("childBack");
        this.childBack = childBack;
        childBack.setParent(this);
        this.frontBack = frontBack;

        ImmutableList.Builder<IComponent> builder = ImmutableList.builder();
        childMiddle.forEach(c -> {
            if (c == null) throw new NullPointerException("childMiddle.[?]");
            builder.add(c);
            c.setParent(this);
        });
        this.childMiddle = builder.build();

        isBogie = false;
    }

    @Override
    public IComponent parent() {
        return parent;
    }

    @Override
    public void setParent(IComponent parent) {
        this.parent = parent;
    }

    @Override
    public IComponent rotatingComponent() {
        if (isBogie) return this;
        return IComponent.super.rotatingComponent();
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
        childMiddle.forEach(c -> c.alignTo(around, offset + c.originOffset()));
    }

    @Override
    public final AxisAlignedBB getBoundingBox() {
        AxisAlignedBB front = childFront.getBoundingBox().offset(childFront.originOffset(), 0, 0);
        // Use an array as a pointer to the internal AABB- this avoids the AABB being final
        AxisAlignedBB[] back = { childBack.getBoundingBox().offset(childBack.originOffset(), 0, 0) };
        childMiddle.forEach(c -> back[0] = back[0].union(c.getBoundingBox().offset(c.originOffset(), 0, 0)));
        return front.union(back[0]).union(box());
    }

    protected abstract AxisAlignedBB box();

    @Override
    public void tick() {
        childFront.tick();
        childMiddle.forEach(c -> c.tick());
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