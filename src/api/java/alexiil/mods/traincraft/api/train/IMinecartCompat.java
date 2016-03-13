package alexiil.mods.traincraft.api.train;

import net.minecraft.entity.item.EntityMinecart;

/** Implement this on any of your minecart classes to check */
public interface IMinecartCompat {
    boolean canUpdateManually();

    public interface IMinecartExternalCompat<M extends EntityMinecart> {
        boolean canUpdateManually(M minecart);
    }
}
