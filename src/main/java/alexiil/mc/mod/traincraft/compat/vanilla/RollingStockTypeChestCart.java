package alexiil.mc.mod.traincraft.compat.vanilla;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.traincraft.api.component.IComponentInner;
import alexiil.mc.mod.traincraft.api.component.IComponentOuter;
import alexiil.mc.mod.traincraft.api.train.Connector;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;
import alexiil.mc.mod.traincraft.api.train.IRollingStockType;

public enum RollingStockTypeChestCart implements IRollingStockType {
    INSTANCE;

    private static final ResourceLocation uniqueID = new ResourceLocation("traincraft:vanilla/chest_cart");

    @Override
    public ConstructedData createInstance(IRollingStock stock) {
        IComponentOuter wheel1 = new ComponentTinyWheel(stock, -0.25, 0);
        IComponentOuter wheel2 = new ComponentTinyWheel(stock, 0.25, 1);
        IComponentInner chest = new InnerVanillaChest(stock);
        IComponentOuter cartComponent = new ComponentChestCart(stock, wheel1, wheel2, Collections.emptyList(), ImmutableList.of(chest), 0.5);

        Connector front = new Connector(stock, cartComponent, 0.55);
        Connector back = new Connector(stock, cartComponent, -0.55);
        return new ConstructedData(cartComponent, front, back);
    }

    @Override
    public ResourceLocation uniqueID() {
        return uniqueID;
    }

    @Override
    public ItemStack getPickedItem() {
        return new ItemStack(Items.chest_minecart);
    }
}
