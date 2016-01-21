package alexiil.mods.traincraft.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.TrainRegistry;
import alexiil.mods.traincraft.api.ITrackPath;

@SideOnly(Side.CLIENT)
public enum CurvedModelGenerator {
    INSTANCE;

    private TextureAtlasSprite railSprite, railSpriteMirrored;
    private Map<ITrackPath, IBakedModel> models = new HashMap<>();
    private List<List<BakedQuad>> sleepers = new ArrayList<>();

    private CurvedModelGenerator() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post event) {
        railSprite = event.map.getAtlasSprite("traincraft:block/track_straight");
        railSpriteMirrored = event.map.getAtlasSprite("traincraft:block/track_straight_mirror");
    }

    @SubscribeEvent
    public void modelBakedEvent(ModelBakeEvent event) {
        models.clear();
        sleepers.clear();
    }

    private static List<BakedQuad> generateModelFor(ITrackPath path, TextureAtlasSprite railSprite, List<List<BakedQuad>> sleepers) {
        List<BakedQuad> list = new ArrayList<>();
        if (true) return list;

        double length = path.length();
        int numSleepers = (int) (length * 4);
        double sleeperDist = 1 / numSleepers;

        int sleeperIndex = 0;
        double offset = 0;
        for (int i = 0; i < numSleepers; i++) {
            double position = offset + sleeperDist;
            TrainCraft.trainCraftLog.info("offset = " + offset + ", position = " + position);
            List<BakedQuad> sleeper = sleepers.get(sleeperIndex);
            sleeper = ModelUtil.multiplyMatrix(sleeper, MatrixUtil.rotateTo(path.direction(position)));
            sleeper = ModelUtil.multiplyMatrix(sleeper, MatrixUtil.translation(path.interpolate(position)));
            list.addAll(sleeper);

            offset += 1 / (double) numSleepers;
            sleeperIndex++;
            sleeperIndex %= sleepers.size();
        }

        return list;
    }

    private List<List<BakedQuad>> loadSleepers() {
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

    public IBakedModel generateModelFor(ITrackPath path, boolean mirror) {
        if (!models.containsKey(path)) {
            TextureAtlasSprite sprite = mirror ? railSpriteMirrored : railSprite;
            List<BakedQuad> quads = generateModelFor(path, sprite, loadSleepers());
            models.put(path, ModelUtil.wrapInBakedModel(quads, sprite));
        }
        return models.get(path);
    }
}
