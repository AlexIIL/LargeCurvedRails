package alexiil.mc.mod.traincraft.component.inner;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import alexiil.mc.mod.traincraft.api.train.IRollingStock;

public abstract class InnerItemStorage extends AbstractComponentInner {
    private final IItemHandler itemHandler;

    public InnerItemStorage(IRollingStock stock, double originOffset, AxisAlignedBB boundingBox, IItemHandler handler) {
        super(stock, originOffset, boundingBox);
        itemHandler = handler;
    }

    public InnerItemStorage(IRollingStock stock, double originOffset, AxisAlignedBB boundingBox, int maxStacks) {
        this(stock, originOffset, boundingBox, new ItemStackHandler(maxStacks));
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemHandler;
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public void tick() {}
}
