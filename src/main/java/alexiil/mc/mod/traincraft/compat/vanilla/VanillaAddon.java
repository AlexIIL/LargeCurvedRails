package alexiil.mc.mod.traincraft.compat.vanilla;

import net.minecraft.init.Blocks;

import alexiil.mc.mod.traincraft.TrackPathProvider;
import alexiil.mc.mod.traincraft.TrackRegistry;
import alexiil.mc.mod.traincraft.api.IAddon;

public class VanillaAddon implements IAddon {
    @Override
    public void preInit() {
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.NORMAL);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.ACTIVATOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.DETECTOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.GOLDEN);

        TrackPathProvider.INSTANCE.registerBlock(Blocks.RAIL, new VanillaTrackBlock(BehaviourVanillaNative.Normal.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.DETECTOR_RAIL, new VanillaTrackBlock(BehaviourVanillaNative.Detector.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.ACTIVATOR_RAIL, new VanillaTrackBlock(BehaviourVanillaNative.Activator.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.GOLDEN_RAIL, new VanillaTrackBlock(BehaviourVanillaNative.Golden.INSTANCE));
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
