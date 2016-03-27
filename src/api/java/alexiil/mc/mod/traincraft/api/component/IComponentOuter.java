package alexiil.mc.mod.traincraft.api.component;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.train.AlignmentFailureException;
import alexiil.mc.mod.traincraft.api.train.IRollingStock;

public interface IComponentOuter extends IComponent {
    @Override
    @SideOnly(Side.CLIENT)
    default void render(IRollingStock stock, float partialTicks) {
        for (IComponentOuter child : children()) {
            child.render(stock, partialTicks);
        }
        for (IComponentInner inner : innerComponents()) {
            inner.render(stock, partialTicks);
        }
    }

    @Override
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

    default IComponentOuter rotatingComponent() {
        if (parent() == null) return this;
        return parent();
    }

    List<IComponentOuter> children();

    List<IComponentInner> innerComponents();

    @Override
    default <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        for (IComponentInner inner : innerComponents()) {
            if (inner.hasCapability(capability, facing)) return inner.getCapability(capability, facing);
        }
        return null;
    }

    @Override
    default boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        for (IComponentInner inner : innerComponents()) {
            if (inner.hasCapability(capability, facing)) return true;
        }
        return false;
    }

    void alignTo(BehaviourWrapper around, double meters, boolean simulate) throws AlignmentFailureException;

    double frontArea();

    /** The current resistance that this stock applies per second. Usually this will be
     * <p>
     * <code>
     * C * {@link #weight()} *  1 - {@link Math#abs(double)} ( {@link #inclination()} )  + frontArea * {@link #speed()} )
     * </code>
     * <p>
     * The value for C generally depends on the type and number of wheels you have, for example 0.08 for wooden based
     * wheels and 0.001 for steel based ones. Other materials will vary.
     * 
     * The value for frontArea should generally be the actual area of the stock, which should be smaller if the front is
     * aerodynamic.
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Rolling_resistance">https://en.wikipedia.org/wiki/Rolling_resistance
     *      </a> */
    double resistance();

    /** @return The current inclination (between -1 and 1) for how the train is positioned vertically. A value of -1
     *         indicates that the train is going vertically down, and a value of 1 indicates the train is going
     *         vertically upwards. If the train is on a 45 degree slope upwards then this should return 0.5. (in other
     *         words run the look vector y component through {@link Math#asin(double)}) */
    default double inclination() {
        return getTrackDirection().yCoord;
    }

    double maxBrakingForce();

    boolean isBraking();
}
