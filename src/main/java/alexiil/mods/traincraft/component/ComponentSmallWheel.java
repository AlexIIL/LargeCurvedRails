package alexiil.mods.traincraft.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
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
    private static final double wheelRadius = 0.125;
    private double rotationAngle = 0;

    public ComponentSmallWheel(IRollingStock stock, double offset, int dataWatcherOffset) {
        super(stock, offset, dataWatcherOffset);
    }

    @SideOnly(Side.CLIENT)
    public static void textureStitch(TextureStitchEvent.Pre event) {
        event.map.registerSprite(textureLocation);
    }

    @Override
    public void tick() {
        super.tick();
        if (((Entity) stock()).getEntityWorld().isRemote) {
            double angleDiff = stock().speed() / (20 * wheelRadius);
            angleDiff *= 180 / Math.PI;
            rotationAngle += angleDiff;
        }
    }

    @Override
    public double frictionCoefficient() {
        return 0.04;
    }

    @Override
    public double frontArea() {
        return 0.005;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IRollingStock stock, float partialTicks) {
        GlStateManager.pushMatrix();

        preRenderOffsets(stock, partialTicks);

        GL11.glTranslated(0, wheelRadius, 0);

        double angleDiff = stock().speed() / (20 * wheelRadius);
        angleDiff *= 180 / Math.PI;
        GL11.glRotated(rotationAngle + partialTicks * angleDiff, 1, 0, 0);
        GL11.glTranslated(0, -wheelRadius, 0);

        RenderRollingStockBase.renderModel(modelLocation);

        GlStateManager.popMatrix();
    }

    @Override
    public IComponent createNew(IRollingStock stock) {
        return new ComponentSmallWheel(stock, constructorOffset, componentIndex);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(0, 0, -wheelRadius, 1, wheelRadius * 2, wheelRadius);
    }
}
