package alexiil.mc.mod.traincraft.api.component;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.train.IRollingStock;

public interface IComponent {
    IRollingStock stock();

    double originOffset();

    void tick();

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
    void preRenderOffsets(IRollingStock stock, float partialTicks);

    IComponentOuter parent();

    void setParent(IComponentOuter parent);

    AxisAlignedBB getBoundingBox();

    <T> T getCapability(Capability<T> capability, EnumFacing facing);

    boolean hasCapability(Capability<?> capability, EnumFacing facing);

    int weight();

    /** Should calculate the current amount of newtons of power this rolling stock is putting out. This will be
     * automatically used by the generic rolling stock to model everything properly. This should NOT take into account
     * the current speed.
     * 
     * @return The current power output of this locamotive (may be 0 in most cases if this is not a locamotive) */
    default double maxEngineOutput() {
        return 0;
    }
}
