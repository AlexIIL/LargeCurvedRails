package alexiil.mc.mod.traincraft.client.render;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4i;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

public class SmoothFaceRenderer {
    public static void preRenderGL() {
        RenderHelper.enableStandardItemLighting();
    }

    public static void postRenderGL() {
        RenderHelper.disableStandardItemLighting();
    }

    public static void renderDisplayList(int glList) {
        // preRenderGL();
        GL11.glCallList(glList);
        // postRenderGL();
    }

    public static void renderModel(IBakedModel model, Matrix4f transform) {
        renderModelMultColour(model, transform, 1, 1, 1);
    }

    public static void renderModelMultColour(IBakedModel model, Matrix4f transform, float red, float green, float blue) {
        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(model.getQuads(null, null, 0));
        for (EnumFacing face : EnumFacing.values())
            quads.addAll(model.getQuads(null, face, 0));
        renderModelMultColour(quads, transform, red, green, blue);
    }

    public static void renderModel(List<BakedQuad> quads, Matrix4f transform) {
        renderModelMultColour(quads, transform, 1, 1, 1);
    }

    public static void renderModelMultColour(List<BakedQuad> quads, Matrix4f transform, float red, float green, float blue) {
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.setTranslation(0, 0, 0);
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (BakedQuad quad : quads) {
            if (quad == null) continue;
            Point3f[] points = transform(points(quad), transform);
            // TODO: Normals per-vertex
            Vector3f normal = normal(points);
            float diffuse = diffuse(normal);

            for (int i = 0; i < 4; i++) {
                Point3f pos = points[i];
                bb.pos(pos.x, pos.y, pos.z);

                Point2f uv = uv(quad, i);
                bb.tex(uv.x, uv.y);

                Point4i colour = colour(quad, i);
                bb.color((int) (diffuse * colour.x * red), (int) (diffuse * colour.y * green), (int) (diffuse * colour.z * blue), colour.w);

                bb.normal(normal.x, normal.y, normal.z);

                bb.endVertex();
            }
        }
        Tessellator.getInstance().draw();
    }

    static float diffuse(Vector3f normal) {
        return diffuse(normal.x, normal.y, normal.z);
    }

    static float diffuse(float x, float y, float z) {
        boolean up = y >= 0;

        float xx = x * x;
        float yy = y * y;
        float zz = z * z;

        float t = xx + yy + zz;
        float light = (xx * 0.6f + zz * 0.8f) / t;

        float yyt = yy / t;
        if (!up) yyt *= 0.5;
        light += yyt;

        return light;
    }

    static Point3f[] points(BakedQuad quad) {
        int[] data = quad.getVertexData();
        int step = data.length / 4;
        Point3f[] positions = new Point3f[4];
        for (int i = 0; i < 4; i++) {
            Point3f vec = new Point3f();
            vec.x = Float.intBitsToFloat(data[i * step + 0]);
            vec.y = Float.intBitsToFloat(data[i * step + 1]);
            vec.z = Float.intBitsToFloat(data[i * step + 2]);
            positions[i] = vec;
        }
        return positions;
    }

    static Point3f[] transform(Point3f[] points, Matrix4f matrix) {
        Point3f[] p = new Point3f[points.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Point3f(points[i]);
            matrix.transform(p[i]);
        }
        return p;
    }

    static Vector3f normal(Point3f[] points) {
        Vector3f a = new Vector3f(points[1]);
        a.sub(points[0]);

        Vector3f b = new Vector3f(points[2]);
        b.sub(points[0]);

        Vector3f c = new Vector3f();
        c.cross(a, b);
        return c;
    }

    static Point3f pos(BakedQuad quad, int index) {
        int[] data = quad.getVertexData();
        int offset = index * data.length / 4;
        return new Point3f(toFloat(data[offset]), toFloat(data[1 + offset]), toFloat(data[2 + offset]));
    }

    static Point4i colour(BakedQuad quad, int index) {
        return new Point4i(0xFF, 0xFF, 0xFF, 0xFF);
        // int[] data = quad.getVertexData();
        // int offset = index * data.length / 4;
        // int rgba = data[3 + offset];
        // int r = rgba & 0xFF;
        // int g = (rgba >> 8) & 0xFF;
        // int b = (rgba >> 16) & 0xFF;
        // int a = (rgba >> 24) & 0xFF;
        // return new Point4i(r, g, b, a);
    }

    static Point2f uv(BakedQuad quad, int index) {
        int[] data = quad.getVertexData();
        int stride = data.length / 4;
        return new Point2f(toFloat(data[4 + index * stride]), toFloat(data[5 + index * stride]));
    }

    static float toFloat(int i) {
        return Float.intBitsToFloat(i);
    }
}
