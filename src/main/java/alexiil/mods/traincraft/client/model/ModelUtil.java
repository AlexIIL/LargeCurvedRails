package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.IColoredBakedQuad;
import net.minecraftforge.client.model.IColoredBakedQuad.ColoredBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelUtil {
    public static List<BakedQuad> extractQuadList(IBakedModel baked) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(baked.getGeneralQuads());
        for (EnumFacing face : EnumFacing.values()) {
            quads.addAll(baked.getFaceQuads(face));
        }
        return quads;
    }

    public static IBakedModel wrapInBakedModel(List<BakedQuad> quads, TextureAtlasSprite sprite) {
        List<List<BakedQuad>> faceQuads = new ArrayList<>();
        for (EnumFacing face : EnumFacing.values()) {
            faceQuads.add(new ArrayList<>());
        }
        return new SimpleBakedModel(quads/* new ArrayList<>() */, faceQuads, false, false, sprite, ItemCameraTransforms.DEFAULT);
    }

    public static IBakedModel multiplyMatrix(IBakedModel baked, Matrix4f matrix) {
        return wrapInBakedModel(multiplyMatrix(extractQuadList(baked), matrix), baked.getParticleTexture());
    }

    public static List<BakedQuad> multiplyMatrix(List<BakedQuad> quads, Matrix4f matrix) {
        List<BakedQuad> newQuads = new ArrayList<>();
        for (BakedQuad quad : quads) {
            newQuads.add(multiplyMatrix(quad, matrix));
        }
        return newQuads;
    }

    public static BakedQuad multiplyMatrix(BakedQuad quad, Matrix4f matrix) {
        int[] data = quad.getVertexData();
        data = Arrays.copyOf(data, data.length);
        boolean colour = quad instanceof IColoredBakedQuad;
        int step = data.length / 4;
        for (int i = 0; i < 4; i++) {
            Point3f vec = new Point3f();
            vec.x = Float.intBitsToFloat(data[i * step + 0]);
            vec.y = Float.intBitsToFloat(data[i * step + 1]);
            vec.z = Float.intBitsToFloat(data[i * step + 2]);

            matrix.transform(vec);

            data[i * step + 0] = Float.floatToRawIntBits(vec.x);
            data[i * step + 1] = Float.floatToRawIntBits(vec.y);
            data[i * step + 2] = Float.floatToRawIntBits(vec.z);
        }
        return colour ? new ColoredBakedQuad(data, quad.getTintIndex(), quad.getFace()) : new BakedQuad(data, quad.getTintIndex(), quad.getFace());
    }

    public static void applyColourByNormal(List<MutableQuad> quads) {
        quads.forEach(q -> q.colourByNormal());
    }
}
