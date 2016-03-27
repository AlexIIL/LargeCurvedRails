package alexiil.mc.mod.traincraft.api.track.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** A getter for misc sprites and texture coords for generating rails. Currently only contains defaults for vanilla
 * tracks. */
public interface IModelSpriteGetter {
    TextureAtlasSprite spriteVanillaRails(boolean mirror);

    /** This sprite holds all of the extras for vanilla tracks. You might find these useful. */
    TextureAtlasSprite spriteVanillaExtras();

    float textureU(VanillaExtrasSheet sheet);

    public enum VanillaExtrasSheet {
        POWERED_OFF_START,
        POWERED_OFF_MIDDLE,
        POWERED_OFF_END,
        POWERED_ON_START,
        POWERED_ON_MIDDLE,
        POWERED_ON_END,

        REDSTONE_OFF_START,
        REDSTONE_OFF_MIDDLE,
        REDSTONE_OFF_END,
        REDSTONE_ON_START,
        REDSTONE_ON_MIDDLE,
        REDSTONE_ON_END,;
    }
}
