package alexiil.mods.traincraft.api.component;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.track.ITrackPath;
import alexiil.mods.traincraft.api.train.AlignmentFailureException;
import alexiil.mods.traincraft.api.train.IRollingStock;

/** A component that rests ontop of other components, ultimatly resting on {@link ComponentTrackFollower}. */
public abstract class ComponentResting implements IComponentOuter {
    private final IRollingStock stock;
    private IComponentOuter parent;
    protected final IComponentOuter childFront, childBack;
    protected final ImmutableList<IComponentOuter> childMiddle, allChildren;
    protected final ImmutableList<IComponentInner> innerComponents;
    protected final double frontBack;

    private final boolean isBogie;

    public ComponentResting(IRollingStock stock, IComponentOuter childFront, IComponentOuter childBack, List<IComponentOuter> childMiddle,
            List<IComponentInner> innerComponents, double frontBack) {
        this.stock = stock;
        if (childFront == null) throw new NullPointerException("childFront");
        this.childFront = childFront;
        childFront.setParent(this);
        if (childBack == null) throw new NullPointerException("childBack");
        this.childBack = childBack;
        childBack.setParent(this);
        this.frontBack = frontBack;

        ImmutableList.Builder<IComponentOuter> builder = ImmutableList.builder();
        childMiddle.forEach(c -> {
            if (c == null) throw new NullPointerException("childMiddle.[?]");
            builder.add(c);
            c.setParent(this);
        });
        this.childMiddle = builder.build();

        ImmutableList.Builder<IComponentInner> builder2 = ImmutableList.builder();
        innerComponents.forEach(c -> {
            if (c == null) throw new NullPointerException("innerComponents.[?]");
            builder2.add(c);
            c.setParent(this);
        });
        this.innerComponents = builder2.build();

        isBogie = false;

        ImmutableList.Builder<IComponentOuter> builder3 = ImmutableList.builder();
        builder3.add(childFront);
        builder3.addAll(childMiddle);
        builder3.add(childBack);
        this.allChildren = builder3.build();
    }

    @Override
    public IComponentOuter parent() {
        return parent;
    }

    @Override
    public void setParent(IComponentOuter parent) {
        this.parent = parent;
    }

    @Override
    public List<IComponentOuter> children() {
        return allChildren;
    }

    @Override
    public IComponentOuter rotatingComponent() {
        if (isBogie) return this;
        return IComponentOuter.super.rotatingComponent();
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
    public double resistance() {
        return 0;
    }

    @Override
    public double inclination() {
        return getTrackDirection().yCoord;
    }

    @Override
    public void alignTo(ITrackPath around, double offset, boolean simulate) throws AlignmentFailureException {
        childFront.alignTo(around, offset + childFront.originOffset(), false);
        childBack.alignTo(around, offset + childBack.originOffset(), false);
        for (IComponentOuter comp : childMiddle)
            comp.alignTo(around, offset + comp.originOffset(), false);
    }

    @Override
    public final AxisAlignedBB getBoundingBox() {
        AxisAlignedBB front = childFront.getBoundingBox().offset(0, 0, childFront.originOffset());
        // Use an array as a pointer to the internal AABB- this avoids the AABB being final
        AxisAlignedBB[] back = { childBack.getBoundingBox().offset(0, 0, childBack.originOffset()) };
        childMiddle.forEach(c -> back[0] = back[0].union(c.getBoundingBox().offset(0, 0, c.originOffset())));
        innerComponents.forEach(c -> back[0] = back[0].union(c.getBoundingBox().offset(0, 0, c.originOffset())));
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

    public static Vec3 scale(Vec3 v, double s) {
        return new Vec3(v.xCoord * s, v.yCoord * s, v.zCoord * s);
    }

    @Override
    public Vec3 getTrackDirection(float partialTicks) {
        Vec3 front = childFront.getTrackPos(partialTicks);
        Vec3 back = childBack.getTrackPos(partialTicks);
        return back.subtract(front).normalize();
    }

    @Override
    public List<IComponentInner> innerComponents() {
        return innerComponents;
    }
}
