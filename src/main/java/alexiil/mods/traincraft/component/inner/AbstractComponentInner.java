package alexiil.mods.traincraft.component.inner;

import net.minecraft.util.AxisAlignedBB;

import alexiil.mods.traincraft.api.component.IComponentInner;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.train.IRollingStock;

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
