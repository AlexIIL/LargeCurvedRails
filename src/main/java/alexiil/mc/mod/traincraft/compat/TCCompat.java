package alexiil.mc.mod.traincraft.compat;

import net.minecraftforge.fml.common.Loader;

import alexiil.mc.mod.traincraft.api.AddonManager;
import alexiil.mc.mod.traincraft.compat.buildcraft.BuildCraftAddon;
import alexiil.mc.mod.traincraft.compat.vanilla.VanillaAddon;

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
