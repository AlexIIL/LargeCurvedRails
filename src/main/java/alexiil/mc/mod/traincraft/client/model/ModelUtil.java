package alexiil.mc.mod.traincraft.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelUtil {
    public static List<BakedQuad> extractQuadList(IBlockState state, IBakedModel baked) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(baked.getQuads(state, null, 0));
        for (EnumFacing face : EnumFacing.values()) {
            quads.addAll(baked.getQuads(state, face, 0));
        }
        return quads;
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
        return new BakedQuad(data, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
    }

    public static void applyColourByNormal(List<MutableQuad> quads) {
        quads.forEach(q -> q.colourByNormal());
    }
}
