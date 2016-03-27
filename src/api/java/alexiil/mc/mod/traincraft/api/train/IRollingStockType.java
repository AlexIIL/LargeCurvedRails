package alexiil.mc.mod.traincraft.api.train;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.traincraft.api.component.IComponentOuter;

public interface IRollingStockType {
    ConstructedData createInstance(IRollingStock stock);

    /** @return A (global) unique name that identifies this factory. This will usually be in the form
     *         "modid:moduniqueid" to avoid confusion. */
    ResourceLocation uniqueID();

    default void addDroppedItemStacks(List<ItemStack> stacks) {
        if (getPickedItem() != null) stacks.add(getPickedItem());
    }

    @Nullable
    ItemStack getPickedItem();

    public static class ConstructedData {
        public final IComponentOuter outer;
        public final Connector front, back;

        public ConstructedData(IComponentOuter outer, Connector front, Connector back) {
            this.outer = outer;
            this.front = front;
            this.back = back;
        }
    }
}
