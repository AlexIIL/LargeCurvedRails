package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.ITrackPath;

@SideOnly(Side.CLIENT)
public enum CurvedModelGenerator {
    INSTANCE;

    private Map<ITrackPath, IBakedModel> models = new HashMap<>();

    public void clearModelMap() {
        models.clear();
    }

    private static List<BakedQuad> generateModelFor(ITrackPath path, TextureAtlasSprite railSprite, List<List<BakedQuad>> sleepers) {
        List<BakedQuad> list = new ArrayList<>();

        double length = path.length();
        int numSleepers = (int) (length * 16);
        double sleeperDist = 1 / (double) numSleepers;

        int sleeperIndex = 0;
        double offset = sleeperDist / 2;
        for (int i = 0; i < numSleepers; i++) {
            List<BakedQuad> sleeper = sleepers.get(sleeperIndex);
            sleeper = ModelUtil.multiplyMatrix(sleeper, MatrixUtil.rotateTo(path.direction(offset)));

            Vec3 translationVec = path.interpolate(offset).subtract(new Vec3(path.creatingBlock()));
            Matrix4f translation = MatrixUtil.translation(translationVec);
            sleeper = ModelUtil.multiplyMatrix(sleeper, translation);

            list.addAll(sleeper);

            offset += sleeperDist;
            sleeperIndex++;
            sleeperIndex %= sleepers.size();
        }

        return list;
    }

    public IBakedModel generateModelFor(ITrackPath path, boolean mirror) {
        if (!models.containsKey(path)) {
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.railSprite(mirror);
            List<BakedQuad> quads = generateModelFor(path, sprite, CommonModelSpriteCache.INSTANCE.loadSleepers());
            models.put(path, ModelUtil.wrapInBakedModel(quads, sprite));
        }
        return models.get(path);
    }
}
