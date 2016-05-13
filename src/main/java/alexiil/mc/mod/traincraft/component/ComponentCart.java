package alexiil.mc.mod.traincraft.component;

import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.component.ComponentResting;
import alexiil.mc.mod.traincraft.api.component.IComponentInner;
import alexiil.mc.mod.traincraft.api.component.IComponentOuter;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;
import alexiil.mc.mod.traincraft.client.render.RenderRollingStockBase;

public class ComponentCart extends ComponentResting {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/cart.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/cart");

    public ComponentCart(IRollingStock stock, IComponentOuter childFront, IComponentOuter childBack, List<IComponentOuter> childMiddle,
            List<IComponentInner> inners, double frontBack) {
        super(stock, childFront, childBack, childMiddle, inners, frontBack);
    }

    @Override
    public double frontArea() {
        return super.frontArea() + 0.5;
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitchPre(TextureStitchEvent.Pre event) {
        event.map.registerSprite(textureLocation);
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
        return new AxisAlignedBB(-0.5, 0.2, -0.5, 0.5, 1, 0.5);
    }

    @Override
    public int weight() {
        return 100 + super.weight();
    }

    @Override
    public double maxBrakingForce() {
        return 10;
    }

    @Override
    public boolean isBraking() {
        return false;
    }
}
