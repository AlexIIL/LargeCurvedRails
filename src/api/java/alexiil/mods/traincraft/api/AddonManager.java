package alexiil.mods.traincraft.api;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Controls loading traincraft addons. If you are making a compatibility module for TrainCraft then I highly recommend
 * registering here. */
public enum AddonManager {
    INSTANCE;
    private final Map<IAddon, ModState> addons = new IdentityHashMap<>();
    private ModState allReached = ModState.CONSTRUCTED;

    /** @param addon
     * @return */
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

    @SideOnly(Side.CLIENT)
    public void textureStitchPre(TextureStitchEvent.Pre pre) {
        addons.keySet().forEach(a -> a.textureStitchPre(pre));
    }

    @SideOnly(Side.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post post) {
        addons.keySet().forEach(a -> a.textureStitchPost(post));
    }

    @SideOnly(Side.CLIENT)
    public void modelBake(ModelBakeEvent bake) {
        addons.keySet().forEach(a -> a.modelBake(bake));
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
