package alexiil.mods.traincraft.compat.vanilla;

import alexiil.mods.traincraft.api.IAddon;

public class VanillaAddon implements IAddon {
    @Override
    public void preInit() {}

    @Override
    public void init() {}

    @Override
    public void postInit() {}

    @Override
    public void enable() {}

    @Override
    public void disable() {}

    @Override
    public boolean canEnable() {
        return true;
    }

    @Override
    public String getName() {
        return "vanilla";
    }
}
