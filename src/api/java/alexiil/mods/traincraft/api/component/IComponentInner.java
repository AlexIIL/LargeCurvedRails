package alexiil.mods.traincraft.api.component;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.train.IRollingStock;

/** A component that is held by an {@link IComponentOuter}. This might represent a seat, a chimney producing steam or
 * perhaps a chest or tank containing items or others. */
public interface IComponentInner extends IComponent {
    @Override
    @SideOnly(Side.CLIENT)
    default void preRenderOffsets(IRollingStock stock, float partialTicks) {
        Vec3 actualPos = parent().getTrackPos(partialTicks);
        GlStateManager.translate(actualPos.xCoord, actualPos.yCoord, actualPos.zCoord);

        Vec3 lookVec = parent().getTrackDirection(partialTicks);

        double tan = Math.atan2(lookVec.xCoord, lookVec.zCoord);
        // The tan is in radians but OpenGL uses degrees
        GL11.glRotated(tan * (180 / Math.PI), 0, 1, 0);

        GL11.glRotated(Math.asin(-lookVec.yCoord) * 180 / Math.PI, 1, 0, 0);
    }

    @Override
    default Vec3 getTrackDirection(float partialTicks) {
        return parent().getTrackDirection(partialTicks);
    }
    
    @Override
    default Vec3 getTrackPos(float partialTicks) {
        return parent().getTrackPos(partialTicks);
    }
}
