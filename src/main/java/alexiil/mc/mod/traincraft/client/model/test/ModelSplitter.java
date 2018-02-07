package alexiil.mc.mod.traincraft.client.model.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.Vec3d;

import alexiil.mc.mod.traincraft.client.model.MutableQuad;
import alexiil.mc.mod.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mc.mod.traincraft.client.model.Plane;
import alexiil.mc.mod.traincraft.client.model.Plane.Face;

public class ModelSplitter {
    public static List<MutableQuad> function(List<MutableQuad> quads, Function<MutableQuad, MutableQuad> func) {
        return quads.stream().parallel().map((mutable) -> func.apply(new MutableQuad(mutable))).collect(Collectors.toList());
    }

    public static List<MutableQuad> offset(List<MutableQuad> quads, Vec3d by) {
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

    public static List<MutableQuad> squashBisected(List<MutableQuad>[] quads, Plane plane, Face face) {
        List<MutableQuad> toSquish = new ArrayList<>(), toKeep = new ArrayList<>(), total = new ArrayList<>();
        if (face == Face.AWAY) {
            toSquish = quads[0];
            toKeep = quads[1];
        } else {
            toSquish = quads[1];
            toKeep = quads[0];
        }
        total.addAll(toKeep);
        total.addAll(squashIntoPlane(toSquish, plane));
        return total;
    }

    private static List<MutableQuad> squashIntoPlane(List<MutableQuad> toSquish, Plane plane) {
        List<MutableQuad> squished = new ArrayList<>();
        for (MutableQuad q : toSquish) {
            MutableQuad post = new MutableQuad(q);
            for (Vertex v : post.verticies()) {
                Vec3d pos = v.positionvd();
                pos = squish(pos, plane);
                v.positionvd(pos);
            }
            squished.add(post);
        }
        return squished;
    }

    private static Vec3d squish(Vec3d vec, Plane plane) {
        // v* is the vector that we want (we are given x and z, we want y)
        double vx = vec.x, vz = vec.z;
        // p* is the plane point
        double px = plane.point.x, py = plane.point.y, pz = plane.point.z;
        // n* is the planes normal
        double nx = plane.normal.x, ny = plane.normal.y, nz = plane.normal.z;
        // eqn for plane: (v)
        // (v - p) . n = 0
        /** Expanding the dot product */
        // (vx-px)*nx+(vy-py)*ny+(vz-pz)*nz = 0
        /** Solving for vy = ? */
        // (vy-py)*ny=-(vx-px)*nx-(vz-pz)*nz
        // vy-py= (-[vx-px]*nx-[vz-pz]*nz)/ny
        // vy = py - ([vx-px]*nx+[vz-pz]*nz)/ny
        /** Computing */
        double vy = py - ((vx - px) * nx + (vz - pz) * nz) / ny;
        return new Vec3d(vec.x, vy, vec.z);
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
