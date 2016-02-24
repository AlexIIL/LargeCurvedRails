package alexiil.mods.traincraft.compat.vanilla;

import net.minecraft.init.Blocks;

import alexiil.mods.traincraft.TrackPathProvider;
import alexiil.mods.traincraft.TrackRegistry;
import alexiil.mods.traincraft.api.IAddon;

public class VanillaAddon implements IAddon {
    @Override
    public void preInit() {
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.NORMAL);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.ACTIVATOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.DETECTOR);
        TrackRegistry.INSTANCE.register(BehaviourVanillaState.Factory.GOLDEN);

        TrackPathProvider.INSTANCE.registerBlock(Blocks.rail, new VanillaTrackBlock(BehaviourVanillaNative.Normal.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.detector_rail, new VanillaTrackBlock(BehaviourVanillaNative.Detector.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.activator_rail, new VanillaTrackBlock(BehaviourVanillaNative.Activator.INSTANCE));
        TrackPathProvider.INSTANCE.registerBlock(Blocks.golden_rail, new VanillaTrackBlock(BehaviourVanillaNative.Golden.INSTANCE));
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
