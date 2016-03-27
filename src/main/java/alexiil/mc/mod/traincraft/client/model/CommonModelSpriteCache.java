package alexiil.mc.mod.traincraft.client.model;

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

import alexiil.mc.mod.traincraft.TrainCraft;
import alexiil.mc.mod.traincraft.TrainRegistry;
import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.track.model.IModelSpriteGetter;
import alexiil.mc.mod.traincraft.api.track.model.RailGeneneratorParams;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mc.mod.traincraft.client.model.test.ModelSplitter;

public enum CommonModelSpriteCache implements IModelSpriteGetter {
    INSTANCE;

    // 4.3 is the lowest number that makes diagonal straight tracks use 3 sleepers rather than 2
    public static final double SLEEPER_COUNT_PER_METER = RailGeneneratorParams.SLEEPER_COUNT_PER_METER;
    public static final double RAIL_COUNT_PER_METER = RailGeneneratorParams.RAIL_COUNT_PER_METER;

    private TextureAtlasSprite railSprite, railSpriteMirrored, spriteVanillaExtras;
    private final List<List<BakedQuad>> sleepers = new ArrayList<>();

    public void clearModelMap() {
        sleepers.clear();
    }

    public void textureStitchPre(TextureStitchEvent.Pre event) {
        railSprite = event.map.registerSprite(new ResourceLocation("traincraft:block/track_straight"));
        railSpriteMirrored = event.map.registerSprite(new ResourceLocation("traincraft:block/track_straight_mirror"));
        spriteVanillaExtras = event.map.registerSprite(new ResourceLocation("traincraft:block/track_straight_vanilla_extra"));
    }

    @Override
    public TextureAtlasSprite spriteVanillaRails(boolean mirror) {
        return mirror ? railSpriteMirrored : railSprite;
    }

    @Override
    public TextureAtlasSprite spriteVanillaExtras() {
        return spriteVanillaExtras;
    }

    @Override
    public float textureU(VanillaExtrasSheet sheet) {
        switch (sheet) {
            // @formatter:off
            case POWERED_OFF_START: return 0;
            case POWERED_OFF_MIDDLE: return 1;
            case POWERED_OFF_END:
            case POWERED_ON_START: return 2;
            case POWERED_ON_MIDDLE: return 3;
            case POWERED_ON_END:
            case REDSTONE_OFF_START: return 4;
            case REDSTONE_OFF_MIDDLE: return 5;
            case REDSTONE_OFF_END:
            case REDSTONE_ON_START: return 6;
            case REDSTONE_ON_MIDDLE: return 7;
            case REDSTONE_ON_END: return 8;
            // @formatter:on
        }
        throw new IllegalArgumentException("Unknown sheet! " + sheet);
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

    public static List<BakedQuad> generateSleepers(ITrackPath path, List<List<BakedQuad>> sleepers, boolean reverse) {
        List<BakedQuad> list = new ArrayList<>();

        double length = path.length();
        int numSleepers = (int) (length * SLEEPER_COUNT_PER_METER);
        double sleeperDist = 1 / (double) numSleepers;

        int sleeperIndex = 0;
        double offset = sleeperDist / 2;
        for (int i = 0; i < numSleepers; i++) {
            List<BakedQuad> sleeper = sleepers.get(sleeperIndex);
            if (reverse) {
                sleeper = ModelUtil.multiplyMatrix(sleeper, MatrixUtil.rotateTo(new Vec3(0, 0, -1)));
            }
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
        return generateRails(path, new GenerateRailsArguments(path, railSprite));
    }

    // TODO: Replace all instances with RailGeneratorParams
    @Deprecated
    public static class GenerateRailsArguments {
        private TextureAtlasSprite railSprite;
        private double railGap = CommonModelSpriteCache.RAIL_COUNT_PER_METER;
        private float uMin = 1, uMax = 3;
        private boolean left = true, right = true;
        private double width = 2 / 16.0, radius = 5 / 16.0;
        private double yOffset = 0;

        public GenerateRailsArguments(ITrackPath path, TextureAtlasSprite railSprite) {
            this.railSprite = railSprite;
        }

        // @formatter:off
        public GenerateRailsArguments path(ITrackPath path) { throw new IllegalArgumentException("NOT IMPLEMETED"); }
        public GenerateRailsArguments railSprite(TextureAtlasSprite railSprite) { this.railSprite = railSprite; return this; }
        public GenerateRailsArguments railGap(double railGap) { this.railGap = railGap; return this; }
        public GenerateRailsArguments uMin(float uMin) { this.uMin = uMin; return this; }
        public GenerateRailsArguments uMax(float uMax) { this.uMax = uMax; return this; }
        public GenerateRailsArguments left(boolean l) {this.left = l; return this; }
        public GenerateRailsArguments right(boolean r) {this.right = r; return this; }
        public GenerateRailsArguments width(double w) {this.width = w; return this; }
        public GenerateRailsArguments radius(double r) {this.radius = r; return this; }
        public GenerateRailsArguments yOffset(double y) {this.yOffset = y; return this; }
        // @formatter:on
    }

    public static List<BakedQuad> generateRails(ITrackPath path, RailGeneneratorParams args) {
        List<BakedQuad> list = new ArrayList<>();

        double length = path.length();
        int numRailJoints = (int) (length * args.railGap());
        double railDist = 1 / (double) numRailJoints;

        double currentV = 0;
        double offset = 0;
        for (int i = 0; i < numRailJoints; i++) {
            if (currentV + railDist > 1) currentV = 0;
            double vL = 16 * currentV;
            Vec3 railStartMiddle = path.interpolate(offset).addVector(0, args.yOffset(), 0);
            Vec3 railStartDir = path.direction(offset);

            offset += railDist;
            currentV += railDist;
            double vH = 16 * currentV;

            Vec3 railEndMiddle = path.interpolate(offset).addVector(0, args.yOffset(), 0);
            Vec3 railEndDir = path.direction(offset);
            if (args.left()) {
                Vec3[][] vecs = { { railStartMiddle, railStartDir }, { railEndMiddle, railEndDir } };
                float[][] uvs = { //
                    { args.railSprite().getInterpolatedU(args.uMin()), args.railSprite().getInterpolatedU(args.uMax()) },//
                    { args.railSprite().getInterpolatedV(vL), args.railSprite().getInterpolatedV(vH) } //
                };
                list.addAll(makeQuads(vecs, uvs, args.radius(), args.width()));
            }
            if (args.right()) {
                Vec3[][] vecs = { { railStartMiddle, railStartDir }, { railEndMiddle, railEndDir } };
                float[][] uvs = { //
                    { args.railSprite().getInterpolatedU(args.uMax()), args.railSprite().getInterpolatedU(args.uMin()) },//
                    { args.railSprite().getInterpolatedV(vL), args.railSprite().getInterpolatedV(vH) } //
                };
                list.addAll(makeQuads(vecs, uvs, -args.radius(), args.width()));
            }
        }

        return list;
    }

    @Deprecated
    public static List<BakedQuad> generateRails(ITrackPath path, GenerateRailsArguments args) {
        return generateRails(path, new RailGeneneratorParams(args.railSprite)
                // @formatter:off
                .railGap(args.railGap)
                .uMin(args.uMin)
                .uMax(args.uMax)
                .left(args.left)
                .right(args.right)
                .width(args.width)
                .radius(args.radius)
                .yOffset(args.yOffset));
                // @formatter:on
    }

    /** @param coords An array of {{start, startDir}, {end, endDir}}
     * @param uvs An array of {{uMin, uMax}, {vMin, vMax}}
     * @param centerDist
     * @param width
     * @return */
    private static List<BakedQuad> makeQuads(Vec3[][] coords, float[][] uvs, double centerDist, double width) {
        Vec3 otherDirStart = MathUtil.cross(coords[0][1], new Vec3(0, 1, 0)).normalize();
        Vec3 otherDirEnd = MathUtil.cross(coords[1][1], new Vec3(0, 1, 0)).normalize();

        Vec3[] normal = { // All faces:
            new Vec3(0, 1, 0), // up
            new Vec3(0, -1, 0), // down
            replaceY(0, coords[0][1]).normalize(), // forwards
            replaceY(0, coords[1][1]).normalize().subtractReverse(new Vec3(0, 0, 0)), // backwards
            otherDirStart, // left
            otherDirEnd // right
        };
        Vec3[] t = { // This comment actually just keeps the formatter happy
            coords[0][0].add(MathUtil.scale(otherDirStart, centerDist + width / 2)), // Start Outer
            coords[0][0].add(MathUtil.scale(otherDirStart, centerDist - width / 2)),// Start Inner
            coords[1][0].add(MathUtil.scale(otherDirEnd, centerDist + width / 2)), // End Outer
            coords[1][0].add(MathUtil.scale(otherDirEnd, centerDist - width / 2)),// End Inner
        };

        Vec3[] b = { down(t[0]), down(t[1]), down(t[2]), down(t[3]) };// Same as tops but down

        Vec3[][][] pos = {// Very happy formatter right now
            { { t[0], t[1] }, { t[2], t[3] } }, // Up
            { { b[1], b[0] }, { b[3], b[2] } }, // Down
            { { t[1], t[0] }, { b[1], b[0] } }, // Forwards
            { { t[2], t[3] }, { b[2], b[3] } }, // Back
            { { t[2], b[2] }, { t[0], b[0] } }, // Left
            { { b[3], t[3] }, { b[1], t[1] } }, // Right
        };

        float[][][] tex = {// A bit too exited if you ask me...
            { { uvs[0][0], uvs[0][1] }, { uvs[1][0], uvs[1][1] } },// Up
            { { uvs[0][1], uvs[0][0] }, { uvs[1][0], uvs[1][1] } }, // Down
            { { uvs[0][1], uvs[0][0] }, { uvs[1][1], uvs[1][0] } }, // Forwards
            { { uvs[0][0], uvs[0][1] }, { uvs[1][1], uvs[1][0] } }, // Back
            { { uvs[0][0], (uvs[0][0] + uvs[0][1]) / 2 }, { uvs[1][1], uvs[1][0] } }, // Left
            { { uvs[0][1], (uvs[0][0] + uvs[0][1]) / 2 }, { uvs[1][1], uvs[1][0] } }, // Right
        };

        return makeQuads(pos, tex, normal);
    }

    private static Vec3 replaceY(int y, Vec3 v) {
        return new Vec3(v.xCoord, y, v.zCoord);
    }

    private static Vec3 down(Vec3 vec) {
        return vec.addVector(0, -1 / 16d, 0);
    }

    private static List<BakedQuad> makeQuads(Vec3[][][] pos, float[][][] tex, Vec3[] normal) {
        List<MutableQuad> quads = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            quads.add(makeQuad(pos[i], tex[i], normal[i]));
        }
        ModelUtil.applyColourByNormal(quads);
        return ModelSplitter.makeVanilla(quads, MutableQuad.ITEM_LMAP);
    }

    private static MutableQuad makeQuad(Vec3[][] pos, float[][] tex, Vec3 normal) {
        Vertex[] verticies = new Vertex[] { new Vertex(), new Vertex(), new Vertex(), new Vertex() };
        verticies[0].positionvd(pos[0][1]).colourf(1, 1, 1, 1).lighti(0, 0).texf(tex[0][1], tex[1][0]).normalvd(normal);
        verticies[1].positionvd(pos[0][0]).colourf(1, 1, 1, 1).lighti(0, 0).texf(tex[0][0], tex[1][0]).normalvd(normal);
        verticies[2].positionvd(pos[1][0]).colourf(1, 1, 1, 1).lighti(0, 0).texf(tex[0][0], tex[1][1]).normalvd(normal);
        verticies[3].positionvd(pos[1][1]).colourf(1, 1, 1, 1).lighti(0, 0).texf(tex[0][1], tex[1][1]).normalvd(normal);
        return new MutableQuad(verticies, -1, null);
    }
}
