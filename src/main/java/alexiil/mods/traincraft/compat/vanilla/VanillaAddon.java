package alexiil.mods.traincraft.compat.vanilla;

import alexiil.mods.traincraft.TrackRegistry;
import alexiil.mods.traincraft.api.IAddon;

public class VanillaAddon implements IAddon {
    @Override
    public void preInit() {
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.NORMAL);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.ACTIVATOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.DETECTOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.SPEED);
    }

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
