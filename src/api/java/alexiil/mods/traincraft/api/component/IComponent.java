package alexiil.mods.traincraft.api.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.AlignmentFailureException;
import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.ITrackPath;

public interface IComponent {
    IRollingStock stock();

    double originOffset();

    public abstract void tick();

    default Vec3 getTrackPos() {
        return getTrackPos(0);
    }

    Vec3 getTrackPos(float partialTicks);

    default Vec3 getTrackDirection() {
        return getTrackDirection(0);
    }

    Vec3 getTrackDirection(float partialTicks);

    @SideOnly(Side.CLIENT)
    void render(IRollingStock stock, float partialTicks);

    @SideOnly(Side.CLIENT)
    default void preRenderOffsets(IRollingStock stock, float partialTicks) {
        Vec3 actualPos = getTrackPos(partialTicks);
        GlStateManager.translate(actualPos.xCoord, actualPos.yCoord, actualPos.zCoord);

        Vec3 lookVec = rotatingComponent().getTrackDirection(partialTicks);

        double tan = Math.atan2(lookVec.xCoord, lookVec.zCoord);
        // The tan is in radians but OpenGL uses degrees
        GL11.glRotated(tan * (180 / Math.PI), 0, 1, 0);

        GL11.glRotated(Math.asin(-lookVec.yCoord) * 180 / Math.PI, 1, 0, 0);
    }

    default IComponent rotatingComponent() {
        if (parent() == null) return this;
        return parent();
    }

    IComponent parent();

    void setParent(IComponent parent);

    IComponent createNew(IRollingStock stock);

    void alignTo(ITrackPath around, double meters) throws AlignmentFailureException;

    double frictionCoefficient();

    double frontArea();

    AxisAlignedBB getBoundingBox();
}
