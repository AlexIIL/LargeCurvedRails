package alexiil.mods.traincraft.api.component;

import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.IRollingStock;

/** A component that rests ontop of other components, ultimatly resting on {@link ComponentTrackFollower}. */
public abstract class ComponentResting implements IComponent {
    private final IRollingStock stock;
    private final IComponent childFront, childBack;
    private final double frontBack;

    public ComponentResting(IRollingStock stock, IComponent childFront, IComponent childBack, double frontBack) {
        if (stock == null) throw new NullPointerException("stock");
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
        Vec3 front = childFront.getTrackDirection(partialTicks);
        Vec3 back = childBack.getTrackDirection(partialTicks);
        return front.add(back).normalize();
    }
}
