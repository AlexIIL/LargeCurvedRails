package alexiil.mc.mod.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import alexiil.mc.mod.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mc.mod.traincraft.client.model.Plane.Face;
import alexiil.mc.mod.traincraft.client.model.Plane.Interpolation;
import alexiil.mc.mod.traincraft.client.model.Plane.Line;
import alexiil.mc.mod.traincraft.client.model.Plane.Split;

public class Polygon {
    private final Vertex[] verticies;
    private final int tintIndex;
    private final EnumFacing face;

    public Polygon(MutableQuad quad) {
        verticies = new Vertex[] { quad.getVertex(0), quad.getVertex(1), quad.getVertex(2), quad.getVertex(3) };
        tintIndex = quad.tintIndex;
        face = quad.face;
    }

    public Polygon(Vertex[] verticies, int tintIndex, EnumFacing face) {
        this.verticies = verticies;
        this.tintIndex = tintIndex;
        this.face = face;
    }

    public MutableQuad[] toQuads() {
        if (verticies.length <= 4) {
            return new MutableQuad[] { new MutableQuad(verticies, tintIndex, face) };
        } else if (verticies.length == 5) {
            Vertex[] one = { verticies[0], verticies[1], verticies[2], verticies[3] };
            Vertex[] two = { verticies[3], verticies[4], verticies[0] };
            return new MutableQuad[] { new MutableQuad(one, tintIndex, face), new MutableQuad(two, tintIndex, face) };
        } else if (verticies.length == 6) {
            Vertex[] one = { verticies[0], verticies[1], verticies[2], verticies[3] };
            Vertex[] two = { verticies[3], verticies[4], verticies[5], verticies[0] };
            return new MutableQuad[] { new MutableQuad(one, tintIndex, face), new MutableQuad(two, tintIndex, face) };
        } else throw new IllegalStateException("Too many verticies!");
    }

    public BisectedPolygon bisect(Plane plane) {
        List<Vertex> towards = new ArrayList<>();
        List<Vertex> away = new ArrayList<>();

        for (int v = 0; v < verticies.length; v++) {
            Vertex a = verticies[v];
            Vertex b = verticies[(v + 1) % verticies.length];
            Line line = new Line(a.positionvd(), b.positionvd());
            Split split = plane.getSplit(line);
            switch (split) {
                case IN_PLANE: {
                    away.add(a);
                    towards.add(a);
                    continue;
                }
                case TOUCHES_PLANE: {
                    Face f = plane.getSide(a.positionvd());
                    if (f == Face.AWAY) away.add(a);
                    else if (f == Face.TOWARDS) towards.add(a);
                    else {
                        away.add(a);
                        towards.add(a);
                    }
                    continue;
                }
                case NOT_TOUCHING: {
                    if (plane.getSide(a.positionvd()) == Face.AWAY) away.add(a);
                    else towards.add(a);
                    continue;
                }
                case PASSES_THROUGH_PLANE: {
                    Interpolation interp = plane.getOnPlane(line);
                    Vertex interpV = new Vertex(a, b, (float) interp.interp);
                    if (plane.getSide(a.positionvd()) == Face.AWAY) {
                        away.add(a);
                        away.add(interpV);
                        towards.add(new Vertex(interpV));
                    } else {
                        towards.add(a);
                        towards.add(interpV);
                        away.add(new Vertex(interpV));
                    }
                    continue;
                }
            }
        }

        Polygon polyAway = new Polygon(away.toArray(new Vertex[away.size()]), tintIndex, face);
        Polygon polyTowards = new Polygon(towards.toArray(new Vertex[towards.size()]), tintIndex, face);

        if (away.size() < 3) polyAway = null;
        else if (towards.size() < 3) polyTowards = null;

        return new BisectedPolygon(polyAway, polyTowards);
    }

    public static class BisectedPolygon {
        /** One might be null, but at least one is guarenteed to be non-null */
        public final Polygon away, towards;

        public BisectedPolygon(Polygon away, Polygon towards) {
            this.away = away;
            this.towards = towards;
        }
    }

    public static class LinkedVertex {
        public final List<Polygon> linkedPolys = new ArrayList<>();
        public final List<LinkedVertex> linkedVerticies = new ArrayList<>();
        public final Vec3 pos;

        public LinkedVertex(Vec3 pos) {
            this.pos = pos;
        }

        public void linkTo(Polygon poly) {
            if (!linkedPolys.contains(poly)) linkedPolys.add(poly);
        }

        public void linkTo(LinkedVertex v) {
            if (!v.linkedVerticies.contains(this)) v.linkedVerticies.add(this);
            if (!linkedVerticies.contains(v)) linkedVerticies.add(v);
        }

        public void unlink(LinkedVertex v) {
            if (v.linkedVerticies.contains(this)) v.linkedVerticies.remove(this);
            if (linkedVerticies.contains(v)) linkedVerticies.remove(v);
        }
    }
}
