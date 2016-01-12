package alexiil.mods.traincraft.component;

import org.lwjgl.opengl.GL11;

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
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.render.RenderRollingStockBase;

public class ComponentSmallWheel extends ComponentTrackFollower {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/wheel_small.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:trains/wheel_small");
    private double rotationAngle = 0;

    public ComponentSmallWheel(IRollingStock stock, double offset, int dataWatcherOffset) {
        super(stock, offset, dataWatcherOffset);
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitch(TextureStitchEvent.Pre event) {
        event.map.registerSprite(textureLocation);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        GlStateManager.pushMatrix();

        preRenderOffsets(stock, partialTicks);

        GL11.glTranslated(0, 0.125, 0);
        rotationAngle += 3;
        GL11.glRotated(rotationAngle + partialTicks * 3, 1, 0, 0);
        GL11.glTranslated(0, -0.125, 0);

        IBakedModel model = RenderRollingStockBase.getModel(modelLocation);
        BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        renderer.renderModelBrightnessColor(model, ((Entity) stock).getBrightness(partialTicks), 1, 1, 1);

        GlStateManager.popMatrix();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentSmallWheel(stock, constructorOffset, componentIndex);
    }
}
