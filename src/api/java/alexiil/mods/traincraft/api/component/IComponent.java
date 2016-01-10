package alexiil.mods.traincraft.api.component;

import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;

public interface IComponent {
    IRollingStock stock();

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
    void render(IRollingStock stock);
}
