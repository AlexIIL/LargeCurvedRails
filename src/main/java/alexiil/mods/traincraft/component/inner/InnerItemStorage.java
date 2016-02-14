package alexiil.mods.traincraft.component.inner;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class InnerItemStorage extends AbstractComponentInner {
    private final IItemHandler itemHandler;

    public InnerItemStorage(double originOffset, AxisAlignedBB boundingBox, IItemHandler handler) {
        super(originOffset, boundingBox);
        itemHandler = handler;
    }

    public InnerItemStorage(double originOffset, AxisAlignedBB boundingBox, int maxStacks) {
        this(originOffset, boundingBox, new ItemStackHandler(maxStacks));
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
}
