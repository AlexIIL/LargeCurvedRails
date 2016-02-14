package alexiil.mods.traincraft.component.inner;

import net.minecraft.util.AxisAlignedBB;

import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.component.IComponentInner;

public abstract class AbstractComponentInner implements IComponentInner {
    private final double originOffset;
    private final AxisAlignedBB boundingBox;
    private IComponent parent;

    public AbstractComponentInner(double originOffset, AxisAlignedBB boundingBox) {
        this.originOffset = originOffset;
        this.boundingBox = boundingBox;
    }

    @Override
    public double originOffset() {
        return originOffset;
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
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }
}
