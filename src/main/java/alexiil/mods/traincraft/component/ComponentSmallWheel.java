package alexiil.mods.traincraft.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.render.RenderRollingStockBase;

public class ComponentSmallWheel extends ComponentTrackFollower {
    public ComponentSmallWheel(IRollingStock stock, double offset, int dataWatcherOffset) {
        super(stock, offset, dataWatcherOffset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        GlStateManager.pushMatrix();

        Vec3 actualPos = getTrackPos(partialTicks);
        GlStateManager.translate(actualPos.xCoord, actualPos.yCoord, actualPos.zCoord);

        GlStateManager.disableTexture2D();
        GlStateManager.color(0, 0, 1);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glEnd();
        GlStateManager.enableTexture2D();

        Vec3 lookVec = getTrackDirection(partialTicks);

        double tan = Math.atan2(lookVec.xCoord, lookVec.zCoord);
        // The tan is in radians but OpenGL uses degrees
        GL11.glRotated(tan * (360 / Math.PI / 2), 0, 1, 0);

        IBakedModel model = RenderRollingStockBase.getModel((EntityRollingStockBase) stock);

        BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        renderer.renderModelBrightnessColor(model, ((Entity) stock).getBrightness(partialTicks), 1, 1, 1);
        GlStateManager.popMatrix();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentSmallWheel(stock, constructorOffset, componentIndex);
    }
}
