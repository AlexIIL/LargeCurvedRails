package alexiil.mods.traincraft.api.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.train.IRollingStock;

/** A component that is held by an {@link IComponent}. This might represent a seat, a chimney producing steam or perhaps
 * a chest or tank containing items or others. */
public interface IComponentInner {
    double originOffset();

    void tick();

    default Vec3 trackPos() {
        return trackPos(0);
    }

    default Vec3 trackPos(float partialTicks) {
        IComponent parent = parent();
        Vec3 parPos = parent.getTrackPos(partialTicks);
        Vec3 offset = ComponentResting.scale(trackDirection(partialTicks), originOffset());
        return parPos.add(offset);
    }

    default Vec3 trackDirection() {
        return trackDirection(0);
    }

    default Vec3 trackDirection(float partialTicks) {
        return parent().getTrackDirection(partialTicks);
    }

    @SideOnly(Side.CLIENT)
    void render(IRollingStock stock, float partialTicks);

    @SideOnly(Side.CLIENT)
    default void preRenderOffsets(IRollingStock stock, float partialTicks) {
        Vec3 actualPos = trackPos(partialTicks);
        GlStateManager.translate(actualPos.xCoord, actualPos.yCoord, actualPos.zCoord);

        Vec3 lookVec = trackDirection(partialTicks);

        double tan = Math.atan2(lookVec.xCoord, lookVec.zCoord);
        // The tan is in radians but OpenGL uses degrees
        GL11.glRotated(tan * (180 / Math.PI), 0, 1, 0);

        GL11.glRotated(Math.asin(-lookVec.yCoord) * 180 / Math.PI, 1, 0, 0);
    }

    IComponent parent();

    void setParent(IComponent parent);

    IComponentInner createNew(IRollingStock stock);

    AxisAlignedBB getBoundingBox();

    default <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return null;
    }

    default boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return false;
    }
}
