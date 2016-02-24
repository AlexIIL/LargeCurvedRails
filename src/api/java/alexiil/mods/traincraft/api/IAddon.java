package alexiil.mods.traincraft.api;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAddon {
    void preInit();

    void init();

    void postInit();

    void enable();

    boolean canEnable();

    void disable();

    String getName();

    @SideOnly(Side.CLIENT)
    default void textureStitchPre(TextureStitchEvent.Pre pre) {}

    @SideOnly(Side.CLIENT)
    default void textureStitchPost(TextureStitchEvent.Post post) {}

    @SideOnly(Side.CLIENT)
    default void modelBake(ModelBakeEvent bake) {}
}
