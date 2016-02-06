package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.ITrackPath;

@Deprecated
@SideOnly(Side.CLIENT)
public enum CurvedModelGenerator {
    INSTANCE;

    private Map<ITrackPath, IBakedModel> models = new HashMap<>();

    public void clearModelMap() {
        models.clear();
    }

    @Deprecated
    private static List<BakedQuad> generateModelFor(ITrackPath path, TextureAtlasSprite railSprite, List<List<BakedQuad>> sleepers) {
        List<BakedQuad> list = new ArrayList<>();

        list.addAll(CommonModelSpriteCache.generateSleepers(path, sleepers));

        list.addAll(CommonModelSpriteCache.generateRails(path, railSprite));

        return list;
    }

    @Deprecated
    public IBakedModel generateModelFor(ITrackPath path, boolean mirror) {
        if (!models.containsKey(path)) {
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.railSprite(mirror);
            List<BakedQuad> quads = generateModelFor(path, sprite, CommonModelSpriteCache.INSTANCE.loadSleepers());
            models.put(path, ModelUtil.wrapInBakedModel(quads, sprite));
        }
        return models.get(path);
    }
}
