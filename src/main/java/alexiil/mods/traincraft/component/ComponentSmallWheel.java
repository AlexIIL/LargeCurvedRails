package alexiil.mods.traincraft.component;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

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

        double angleDiff = stock().speed() / (20 * wheelRadius);
        double angle = rotationAngle + partialTicks * angleDiff;

        Matrix4f total = new Matrix4f();
        total.setIdentity();
        total.setTranslation(new Vector3f(0, (float) wheelRadius, 0));

        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        transform.setRotation(new AxisAngle4f(1, 0, 0, (float) angle));
        total.mul(transform);

        transform.setIdentity();
        transform.setTranslation(new Vector3f(0, -(float) wheelRadius, 0));
        total.mul(transform);

        RenderRollingStockBase.renderModelAnimated(modelLocation, total);

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
