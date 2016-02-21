package alexiil.mods.traincraft.component;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponentInner;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.train.Connector;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.api.train.IRollingStockType;
import alexiil.mods.traincraft.component.inner.InnerItemStorage;
import alexiil.mods.traincraft.item.TCItems;

public enum TypeSteamSmall implements IRollingStockType {
    INSTANCE;

    private static final ResourceLocation uniqueID = new ResourceLocation("traincraft:steam_locomotive_small");

    @Override
    public ConstructedData createInstance(IRollingStock stock) {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(stock, -0.25, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(stock, 0.25, 1);
        IComponentInner openChest = new OpenChest(stock, 0, new AxisAlignedBB(0, 0.3, 0, 0, 0.3, 0), 5);
        IComponentOuter cartComponent = new ComponentSmallSteamLocomotive(stock, wheel1, wheel2, Collections.emptyList(), ImmutableList.of(openChest),
                0.5);
        Connector front = new Connector(stock, cartComponent, 0.55);
        Connector back = new Connector(stock, cartComponent, -0.55);
        return new ConstructedData(cartComponent, front, back);
    }

    @Override
    public ResourceLocation uniqueID() {
        return uniqueID;
    }

    @Override
    public void addDroppedItemStacks(List<ItemStack> stacks) {
        stacks.add(getPickedItem());
    }

    @Override
    public ItemStack getPickedItem() {
        return new ItemStack(TCItems.STOCK_SMALL_CART.getItem());
    }

    private static class OpenChest extends InnerItemStorage {
        public OpenChest(IRollingStock stock, double originOffset, AxisAlignedBB boundingBox, int maxStacks) {
            super(stock, originOffset, boundingBox, maxStacks);
        }

        @Override
        public void render(IRollingStock stock, float partialTicks) {}
    }
}
