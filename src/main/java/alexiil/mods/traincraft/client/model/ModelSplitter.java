package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mods.traincraft.client.model.Plane.Face;

public class ModelSplitter {
    public static List<MutableQuad> function(List<MutableQuad> quads, Function<MutableQuad, MutableQuad> func) {
        return quads.stream().parallel().map((mutable) -> func.apply(new MutableQuad(mutable))).collect(Collectors.toList());
    }

    public static List<MutableQuad> offset(List<MutableQuad> quads, Vec3 by) {
        return function(quads, (mutable) -> {
            for (int i = 0; i < 4; i++) {
                Vertex v = mutable.getVertex(i);
                v.positionvd(v.positionvd().add(by));
            }
            return mutable;
        });
    }

    public static List<MutableQuad> makeMutable(List<BakedQuad> quads, VertexFormat format) {
        return quads.stream().map((baked) -> MutableQuad.create(baked, format)).collect(Collectors.toList());
    }

    public static List<BakedQuad> makeVanilla(List<MutableQuad> quads, VertexFormat format) {
        return quads.stream().map(quad -> quad.toUnpacked(format)).collect(Collectors.toList());
    }

    public static List<MutableQuad>[] bisect(List<MutableQuad> quads, Plane p) {
        @SuppressWarnings("unchecked")
        List<MutableQuad>[] array = new List[] { new ArrayList<>(), new ArrayList<>() };
        quads.stream().forEach(q -> {
            MutableQuad[][] bisected = q.bisect(p);
            array[0].addAll(Arrays.asList(bisected[0]));
            array[1].addAll(Arrays.asList(bisected[1]));
        });
        return array;
    }

    public static List<MutableQuad> bisectCulling(List<MutableQuad> quads, Plane p, Face wantedQuads) {
        return bisect(quads, p)[wantedQuads == Face.TOWARDS ? 0 : 1];
    }

    private static String qPosString(MutableQuad q) {
        return "\n{" + vPosString(q.getVertex(0)) + "," + vPosString(q.getVertex(1)) + "," + vPosString(q.getVertex(2)) + "," + vPosString(q
                .getVertex(3)) + "\n}\n";
    }

    private static String vPosString(Vertex v) {
        return "\n\t" + v.positionvd().toString();
    }
}
