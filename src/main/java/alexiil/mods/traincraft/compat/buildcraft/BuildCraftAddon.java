package alexiil.mods.traincraft.compat.buildcraft;

import net.minecraftforge.fml.common.Loader;

import alexiil.mods.traincraft.api.IAddon;

public class BuildCraftAddon implements IAddon {
    @Override
    public void preInit() {}

    @Override
    public void init() {}

    @Override
    public void postInit() {}

    @Override
    public void enable() {}

    @Override
    public boolean canEnable() {
        return Loader.isModLoaded("BuildCraft|Energy");
    }

    @Override
    public void disable() {}

    @Override
    public String getName() {
        return "buildcraft";
    }
}
