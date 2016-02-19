package alexiil.mods.traincraft.compat.vanilla;

import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponentInner;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.client.render.RenderRollingStockBase;

public class ComponentChestCart extends ComponentResting {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/vanilla/cart_chest.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/vanilla/cart_chest");
    private static final AxisAlignedBB boundingBox = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public ComponentChestCart(IRollingStock stock, IComponentOuter childFront, IComponentOuter childBack, List<IComponentOuter> childMiddle,
            List<IComponentInner> innerComponents, double frontBack) {
        super(stock, childFront, childBack, childMiddle, innerComponents, frontBack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        super.render(stock, partialTicks);

        GlStateManager.pushMatrix();
        preRenderOffsets(stock, partialTicks);

        RenderRollingStockBase.renderModel(modelLocation);

        GlStateManager.popMatrix();
    }

    @Override
    protected AxisAlignedBB box() {
        return boundingBox;
    }

    @Override
    public int weight() {
        return 50 + super.weight();
    }

    @Override
    public double maxBrakingForce() {
        return 0;
    }

    @Override
    public boolean isBraking() {
        return false;
    }
}
