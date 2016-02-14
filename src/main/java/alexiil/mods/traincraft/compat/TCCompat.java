package alexiil.mods.traincraft.compat;

import net.minecraftforge.fml.common.Loader;

import alexiil.mods.traincraft.api.AddonManager;
import alexiil.mods.traincraft.compat.buildcraft.BuildCraftAddon;
import alexiil.mods.traincraft.compat.vanilla.VanillaAddon;

public class TCCompat {
    public static VanillaAddon vanilla;
    public static BuildCraftAddon buildcraft;

    public static void preInit() {
        vanilla = AddonManager.INSTANCE.registerAddon(new VanillaAddon());
        if (Loader.isModLoaded("BuildCraft|Energy")) buildcraft = AddonManager.INSTANCE.registerAddon(new BuildCraftAddon());
    }

    public static void init() {}

    public static void postInit() {}
}
