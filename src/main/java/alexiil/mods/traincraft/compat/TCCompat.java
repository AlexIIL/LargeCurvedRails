package alexiil.mods.traincraft.compat;

import alexiil.mods.traincraft.api.AddonManager;
import alexiil.mods.traincraft.compat.vanilla.VanillaAddon;

public class TCCompat {
    public static VanillaAddon vanilla;

    public static void preInit() {
        vanilla = AddonManager.INSTANCE.registerAddon(new VanillaAddon());
    }

    public static void init() {}

    public static void postInit() {}
}
