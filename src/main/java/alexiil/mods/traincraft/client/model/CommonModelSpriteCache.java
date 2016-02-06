package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.TrainRegistry;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mods.traincraft.lib.MathUtil;

public enum CommonModelSpriteCache {
    INSTANCE;

    public static final int DEFAULT_SLEEPER_GAP = 4;
    public static final int DEFAULT_RAIL_GAP = 6;

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

    public static List<BakedQuad> generateSleepers(ITrackPath path, List<List<BakedQuad>> sleepers) {
        List<BakedQuad> list = new ArrayList<>();

        double length = path.length();
        int numSleepers = (int) (length * DEFAULT_SLEEPER_GAP);
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

    public static List<BakedQuad> generateRails(ITrackPath path, TextureAtlasSprite railSprite) {
        return generateRails(path, railSprite, DEFAULT_RAIL_GAP);
    }

    public static List<BakedQuad> generateRails(ITrackPath path, TextureAtlasSprite railSprite, int railGap) {
        List<BakedQuad> list = new ArrayList<>();

        double length = path.length();
        int numRailJoints = (int) (length * railGap);
        double railDist = 1 / (double) numRailJoints;

        // double currentV = 0;
        double offset = 0;
        for (int i = 0; i < numRailJoints; i++) {
            // if (currentV + 1 / DEFAULT_RAIL_GAP > 1) currentV = 0;
            Vec3 railStartMiddle = path.interpolate(offset);
            Vec3 railStartDir = path.direction(offset);

            offset += railDist;
            // currentV += 1 / DEFAULT_RAIL_GAP;

            Vec3 railEndMiddle = path.interpolate(offset);
            Vec3 railEndDir = path.direction(offset);

            list.addAll(generateRailLeft(railStartMiddle, railEndMiddle, railStartDir, railEndDir, railSprite.getInterpolatedU(1), railSprite
                    .getInterpolatedU(3), railSprite.getInterpolatedV(0), railSprite.getInterpolatedV(16f / railGap)));
            list.addAll(generateRailRight(railStartMiddle, railEndMiddle, railStartDir, railEndDir, railSprite.getInterpolatedU(3), railSprite
                    .getInterpolatedU(1), railSprite.getInterpolatedV(0), railSprite.getInterpolatedV(16f / railGap)));
        }

        return list;
    }

    private static List<BakedQuad> generateRailLeft(Vec3 startMiddle, Vec3 endMiddle, Vec3 startDir, Vec3 endDir, float uS, float uE, float vS,
            float vE) {
        Vec3 otherDirStart = MathUtil.cross(startDir, new Vec3(0, 1, 0)).normalize();
        Vec3 leftTopSS = startMiddle.add(MathUtil.scale(otherDirStart, 6 / 16.0));
        Vec3 leftTopSE = startMiddle.add(MathUtil.scale(otherDirStart, 4 / 16.0));

        Vec3 leftBottomSS = leftTopSS.addVector(0, -1 / 16.0, 0);
        Vec3 leftBottomSE = leftTopSE.addVector(0, -1 / 16.0, 0);

        Vec3 otherDirEnd = MathUtil.cross(endDir, new Vec3(0, 1, 0)).normalize();
        Vec3 leftTopES = endMiddle.add(MathUtil.scale(otherDirEnd, 6 / 16.0));
        Vec3 leftTopEE = endMiddle.add(MathUtil.scale(otherDirEnd, 4 / 16.0));

        Vec3 leftBottomES = leftTopES.addVector(0, -1 / 16.0, 0);
        Vec3 leftBottomEE = leftTopEE.addVector(0, -1 / 16.0, 0);

        return makeRailCuboid(uS, uE, vS, vE, leftTopSS, leftTopSE, leftBottomSS, leftBottomSE, leftTopES, leftTopEE, leftBottomES, leftBottomEE);
    }

    private static List<BakedQuad> generateRailRight(Vec3 startMiddle, Vec3 endMiddle, Vec3 startDir, Vec3 endDir, float uS, float uE, float vS,
            float vE) {
        Vec3 otherDirStart = MathUtil.cross(startDir, new Vec3(0, 1, 0)).normalize();
        Vec3 leftTopSS = startMiddle.add(MathUtil.scale(otherDirStart, -4 / 16.0));
        Vec3 leftTopSE = startMiddle.add(MathUtil.scale(otherDirStart, -6 / 16.0));

        Vec3 leftBottomSS = leftTopSS.addVector(0, -1 / 16.0, 0);
        Vec3 leftBottomSE = leftTopSE.addVector(0, -1 / 16.0, 0);

        Vec3 otherDirEnd = MathUtil.cross(endDir, new Vec3(0, 1, 0)).normalize();
        Vec3 leftTopES = endMiddle.add(MathUtil.scale(otherDirEnd, -4 / 16.0));
        Vec3 leftTopEE = endMiddle.add(MathUtil.scale(otherDirEnd, -6 / 16.0));

        Vec3 leftBottomES = leftTopES.addVector(0, -1 / 16.0, 0);
        Vec3 leftBottomEE = leftTopEE.addVector(0, -1 / 16.0, 0);

        return makeRailCuboid(uS, uE, vS, vE, leftTopSS, leftTopSE, leftBottomSS, leftBottomSE, leftTopES, leftTopEE, leftBottomES, leftBottomEE);
    }

    private static List<BakedQuad> makeRailCuboid(float uS, float uE, float vS, float vE, Vec3 topSS, Vec3 topSE, Vec3 bottomSS, Vec3 bottomSE,
            Vec3 topES, Vec3 topEE, Vec3 bottomES, Vec3 bottomEE) {
        List<MutableQuad> quads = new ArrayList<>();
        Vec3 normalSides = new Vec3(0, 0, 1);
        Vec3 normalCaps = new Vec3(1, 0, 0);
        Vec3 compare = topSS.subtract(topEE);
        if (Math.abs(compare.xCoord) < Math.abs(compare.zCoord)) {
            normalSides = new Vec3(1, 0, 0);
            normalCaps = new Vec3(0, 0, 1);
        }

        Vec3 normalSidesOther = new Vec3(-normalSides.xCoord, 0, -normalSides.zCoord);
        Vec3 normalCapsOther = new Vec3(-normalCaps.xCoord, 0, -normalCaps.zCoord);

        Vertex[] verticies = new Vertex[] { new Vertex(), new Vertex(), new Vertex(), new Vertex() };
        // Top
        verticies[0].positionvd(topSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalf(0, 1, 0);
        verticies[1].positionvd(topSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalf(0, 1, 0);
        verticies[2].positionvd(topES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalf(0, 1, 0);
        verticies[3].positionvd(topEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalf(0, 1, 0);
        quads.add(new MutableQuad(verticies, -1, null));
        // Bottom
        verticies[0].positionvd(bottomSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalf(0, -1, 0);
        verticies[1].positionvd(bottomSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalf(0, -1, 0);
        verticies[2].positionvd(bottomEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalf(0, -1, 0);
        verticies[3].positionvd(bottomES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalf(0, -1, 0);
        quads.add(new MutableQuad(verticies, -1, null));
        // Caps
        verticies[0].positionvd(topSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalvd(normalCaps);
        verticies[1].positionvd(topSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalvd(normalCaps);
        verticies[2].positionvd(bottomSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalvd(normalCaps);
        verticies[3].positionvd(bottomSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalvd(normalCaps);
        quads.add(new MutableQuad(verticies, -1, null));
        verticies[0].positionvd(topEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalvd(normalCapsOther);
        verticies[1].positionvd(topES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalvd(normalCapsOther);
        verticies[2].positionvd(bottomES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalvd(normalCapsOther);
        verticies[3].positionvd(bottomEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalvd(normalCapsOther);
        quads.add(new MutableQuad(verticies, -1, null));
        // Sides
        verticies[0].positionvd(topES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalvd(normalSides);
        verticies[1].positionvd(topSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vS).normalvd(normalSides);
        verticies[2].positionvd(bottomSS).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalvd(normalSides);
        verticies[3].positionvd(bottomES).colourf(1, 1, 1, 1).lighti(0, 0).texf(uS, vE).normalvd(normalSides);
        quads.add(new MutableQuad(verticies, -1, null));
        verticies[0].positionvd(topSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalvd(normalSidesOther);
        verticies[1].positionvd(topEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vS).normalvd(normalSidesOther);
        verticies[2].positionvd(bottomEE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalvd(normalSidesOther);
        verticies[3].positionvd(bottomSE).colourf(1, 1, 1, 1).lighti(0, 0).texf(uE, vE).normalvd(normalSidesOther);
        quads.add(new MutableQuad(verticies, -1, null));

        ModelUtil.applyColourByNormal(quads);
        return ModelSplitter.makeVanilla(quads, MutableQuad.ITEM_LMAP);
    }
}
