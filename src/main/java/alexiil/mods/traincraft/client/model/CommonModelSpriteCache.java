package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.TrainRegistry;

public enum CommonModelSpriteCache {
    INSTANCE;

    private TextureAtlasSprite railSprite, railSpriteMirrored;
    private final List<List<BakedQuad>> sleepers = new ArrayList<>();

    public void clearModelMap() {
        sleepers.clear();
    }

    public void textureStitchPost(TextureStitchEvent.Post event) {
        railSprite = event.map.getAtlasSprite("traincraft:block/track_straight");
        railSpriteMirrored = event.map.getAtlasSprite("traincraft:block/track_straight_mirror");
    }

    public TextureAtlasSprite railSprite(boolean mirror) {
        return mirror ? railSpriteMirrored : railSprite;
    }

    /** Loads (or returns immediatly from the cache) a list of all the available sleeper models. All of the lists are
     * the true lists, so you should duplicate the quads if you want to change them. */
    public List<List<BakedQuad>> loadSleepers() {
        if (sleepers.size() == 0) {
            int i = 0;
            try {
                while (i < 4) {
                    ResourceLocation loc = new ResourceLocation("traincraft:models/parts/sleeper_" + i + ".obj");
                    IModel model = OBJLoader.instance.loadModel(loc);
                    IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, TrainRegistry.INSTANCE.getSpriteFunction());
                    sleepers.add(ModelUtil.extractQuadList(baked));
                    i++;
                }
            } catch (Throwable t) {
                if (i == 0) throw new Error("Failed to load sleeper 0!", t);
                TrainCraft.trainCraftLog.warn("Tried to load sleeper " + i + " but failed!", t);
            }
        }
        return sleepers;
    }
}
