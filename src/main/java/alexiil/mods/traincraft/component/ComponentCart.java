package alexiil.mods.traincraft.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.render.RenderRollingStockBase;

public class ComponentCart extends ComponentResting {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/cart.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/cart");

    public ComponentCart(IRollingStock stock, IComponent childFront, IComponent childBack, double frontBack) {
        super(stock, childFront, childBack, frontBack);
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitch(TextureStitchEvent.Pre event) {
        event.map.registerSprite(textureLocation);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        childFront.render(stock, partialTicks);
        childBack.render(stock, partialTicks);

        GlStateManager.pushMatrix();
        preRenderOffsets(stock, partialTicks);
        
        IBakedModel model = RenderRollingStockBase.getModel(modelLocation);
        BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        renderer.renderModelBrightnessColor(model, ((Entity) stock).getBrightness(partialTicks), 1, 1, 1);
        
        GlStateManager.popMatrix();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentCart(stock, childFront.createNew(stock), childBack.createNew(stock), frontBack);
    }
}
