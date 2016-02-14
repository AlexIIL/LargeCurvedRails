package alexiil.mods.traincraft.compat.vanilla;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.component.IComponentInner;
import alexiil.mods.traincraft.api.train.IRollingStock;

public class ComponentChestCart extends ComponentResting {
    public ComponentChestCart(IRollingStock stock, IComponent childFront, IComponent childBack, List<IComponent> childMiddle,
            List<IComponentInner> innerComponents, double frontBack) {
        super(stock, childFront, childBack, childMiddle, innerComponents, frontBack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {

    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentChestCart(stock, childFront, childBack, childMiddle, innerComponents, frontBack);
    }

    @Override
    protected AxisAlignedBB box() {
        return null;
    }
}
