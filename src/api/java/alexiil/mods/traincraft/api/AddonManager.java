package alexiil.mods.traincraft.api;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraftforge.fml.common.LoaderState.ModState;

public enum AddonManager {
    INSTANCE;
    private final Map<IAddon, ModState> addons = new IdentityHashMap<>();
    private ModState allReached = ModState.CONSTRUCTED;

    public <T extends IAddon> T registerAddon(T addon) {
        Entry<IAddon, ModState> entry = MutablePair.of(addon, ModState.CONSTRUCTED);
        advanceToState(entry, allReached);
        addons.put(entry.getKey(), entry.getValue());
        return addon;
    }

    public Set<IAddon> getAddons() {
        return Collections.unmodifiableSet(addons.keySet());
    }

    /////////////////////////
    //
    // State Changes (these should ONLY be called by traincraft)
    //
    /////////////////////////

    public void preInit() {
        addons.entrySet().forEach(a -> advanceToState(a, ModState.PREINITIALIZED));
        allReached = ModState.PREINITIALIZED;
    }

    public void init() {
        addons.entrySet().forEach(a -> advanceToState(a, ModState.INITIALIZED));
        allReached = ModState.INITIALIZED;
    }

    public void postInit() {
        addons.entrySet().forEach(a -> advanceToState(a, ModState.POSTINITIALIZED));
        allReached = ModState.POSTINITIALIZED;
    }

    public void enableAll() {
        addons.entrySet().forEach(a -> advanceToState(a, ModState.AVAILABLE));
        allReached = ModState.AVAILABLE;
    }

    public void disableAll() {
        addons.entrySet().forEach(a -> advanceToState(a, ModState.DISABLED));
        allReached = ModState.DISABLED;
    }

    private void advanceToState(Entry<IAddon, ModState> entry, ModState wantedState) {
        if (entry.getValue() == ModState.CONSTRUCTED) {
            entry.getKey().preInit();
            entry.setValue(ModState.PREINITIALIZED);
        }
        if (entry.getValue() == ModState.PREINITIALIZED && wantedState.ordinal() >= ModState.INITIALIZED.ordinal()) {
            entry.getKey().init();
            entry.setValue(ModState.INITIALIZED);
        }
        if (entry.getValue() == ModState.INITIALIZED && wantedState.ordinal() >= ModState.POSTINITIALIZED.ordinal()) {
            entry.getKey().postInit();
            entry.setValue(ModState.POSTINITIALIZED);
        }
        if (wantedState == ModState.AVAILABLE && entry.getKey().canEnable()) {
            entry.getKey().enable();
            entry.setValue(ModState.AVAILABLE);
        } else if (wantedState == ModState.DISABLED) {
            entry.getKey().disable();
            entry.setValue(ModState.AVAILABLE);
        }
    }
}
