package alexiil.mc.mod.traincraft.component.inner;

import net.minecraft.util.math.AxisAlignedBB;

import alexiil.mc.mod.traincraft.api.component.IComponentInner;
import alexiil.mc.mod.traincraft.api.component.IComponentOuter;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;

public abstract class AbstractComponentInner implements IComponentInner {
    private final double originOffset;
    private final AxisAlignedBB boundingBox;
    private final IRollingStock stock;
    private IComponentOuter parent;

    public AbstractComponentInner(IRollingStock stock, double originOffset, AxisAlignedBB boundingBox) {
        this.originOffset = originOffset;
        this.boundingBox = boundingBox;
        this.stock = stock;
    }

    @Override
    public double originOffset() {
        return originOffset;
    }

    @Override
    public IRollingStock stock() {
        return stock;
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
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }
}
