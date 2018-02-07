package alexiil.mc.mod.traincraft.client.model.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.Vec3d;

import alexiil.mc.mod.traincraft.api.lib.MCObjectUtils;
import alexiil.mc.mod.traincraft.client.model.MutableQuad;
import alexiil.mc.mod.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mc.mod.traincraft.client.model.Plane;
import alexiil.mc.mod.traincraft.client.model.Plane.Face;
import alexiil.mc.mod.traincraft.client.model.Polygon;
import alexiil.mc.mod.traincraft.client.model.Polygon.LinkedVertex;

@Deprecated class Bisector {
    private final List<Polygon> polys = new ArrayList<>();
    private final List<LinkedVertex> linked = new ArrayList<>();

    /** @param sidedQuads
     * @param p
     * @param sprite
     * @return */
    public List<MutableQuad> generateExtraFaces(List<MutableQuad> sidedQuads, Plane p, TextureAtlasSprite sprite) {
        Face face = generateLinkedGraph(sidedQuads, p);
        return generatePlaneQuads(p, sprite, face);
    }

    private Face generateLinkedGraph(List<MutableQuad> sidedQuads, Plane p) {
        Face f = null;
        for (MutableQuad mutableQuad : sidedQuads) {
            Polygon poly = new Polygon(mutableQuad);
            polys.add(poly);

            for (int v = 0; v < 4; v++) {
                Vertex vertex = mutableQuad.getVertex(v);
                if (p.getSide(vertex.positionvd()) == Face.IN_PLANE) {
                    boolean found = false;
                    for (LinkedVertex linkedQ : linked) {
                        if (MCObjectUtils.equals(linkedQ.pos, vertex.positionvd())) {
                            linkedQ.linkTo(poly);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        LinkedVertex linkedv = new LinkedVertex(vertex.positionvd());
                        linkedv.linkTo(poly);
                        linked.add(linkedv);
                    }
                } else if (f == null) f = p.getSide(vertex.positionvd());
            }

            for (int v = 0; v < 4; v++) {
                Vertex v1 = mutableQuad.getVertex(v);
                Vertex v2 = mutableQuad.getVertex((v + 1) % 4);
                if (p.getSide(v1.positionvd()) == Face.IN_PLANE && p.getSide(v2.positionvd()) == Face.IN_PLANE) {
                    LinkedVertex l1 = getLinkedVertex(v1.positionvd());
                    LinkedVertex l2 = getLinkedVertex(v2.positionvd());
                    l1.linkTo(l2);
                }
            }
        }
        return f;
    }

    private List<MutableQuad> generatePlaneQuads(Plane p, TextureAtlasSprite sprite, Face face) {
        if (linked.size() < 3) return Collections.emptyList();

        List<MutableQuad> quads = new ArrayList<>();
        int times = 0;
        while (linked.size() > 2 && times < 4000) {
            List<LinkedVertex> verticies = findPoly(p, face);
            Polygon poly = generatePoly(verticies, p, sprite, face);
            if (poly == null) {
                linked.remove(0);
            } else for (MutableQuad q : poly.toQuads())
                quads.add(q);
            times++;
        }
        if (times > 100) {
            throw new IllegalStateException("Took too many times (" + times + ")");
        }

        return quads;
    }

    private List<LinkedVertex> findPoly(Plane plane, Face face) {
        LinkedVertex start = linked.get(0);
        boolean skipStart = false;
        int skipped = 0;

        List<LinkedVertex> total = new ArrayList<>();
        total.add(start);
        List<LinkedVertex> toRemove = new ArrayList<>();
        for (LinkedVertex v : linked) {
            LinkedVertex last = total.get(total.size() - 1);
            if (!last.linkedVerticies.contains(v)) continue;
            if (total.size() < 2) {
                total.add(v);
            } else {
                LinkedVertex lastButOne = total.get(total.size() - 2);
                if (isValid(last, lastButOne, v, plane, face)) {
                    if (skipped == 0) {
                        toRemove.add(last);
                    }
                    skipped = 0;
                    total.add(v);
                } else {
                    skipped++;
                }
            }
        }

        for (LinkedVertex l : toRemove) {
            linked.remove(l);
        }

        return total;
    }

    private static boolean isValid(LinkedVertex lastButOne, LinkedVertex last, LinkedVertex toCheck, Plane p, Face face) {
        // Vector3d normal = new Vector3d(p.normal.x, p.normal.y, p.normal.z);
        // Vector3d wantedNormal = new Vector3d(0, 1, 0);
        // Matrix3d matrix = MatrixUtil.rotate(normal, wantedNormal);
        //
        Point3d lastButOneP = new Point3d(lastButOne.pos.x, lastButOne.pos.y, lastButOne.pos.z);
        Point3d lastP = new Point3d(last.pos.x, last.pos.y, last.pos.z);
        Point3d toCheckP = new Point3d(toCheck.pos.x, toCheck.pos.y, toCheck.pos.z);
        //
        // matrix.transform(lastButOneP);
        // matrix.transform(lastP);
        // matrix.transform(toCheckP);

        Vector3d lastDir = new Vector3d(lastP);
        lastDir.sub(lastButOneP);
        lastDir.normalize();

        Vector3d thisDir = new Vector3d(toCheckP);
        thisDir.sub(lastP);
        thisDir.normalize();

        double lastAngle = Math.atan2(lastDir.z, lastDir.x) * 180 / Math.PI;
        double thisAngle = Math.atan2(thisDir.z, thisDir.x) * 180 / Math.PI;

        double compared = thisAngle - lastAngle;

        compared %= 180;

        if (face == Face.TOWARDS) {
            return compared > 0;
        } else {
            return compared < 0;
        }
    }

    private static Polygon generatePoly(List<LinkedVertex> linkedVerticies, Plane p, TextureAtlasSprite sprite, Face face) {
        if (linkedVerticies.size() < 3) return null;
        Vertex[] verticies = new Vertex[linkedVerticies.size()];
        for (int v = 0; v < verticies.length; v++) {
            LinkedVertex linked = linkedVerticies.get(v);
            Vertex vertex = new Vertex();
            vertex.positionvd(linked.pos);
            vertex.texf(sprite.getMinU(), sprite.getMinV());
            vertex.colourf(1, 1, 1, 1);
            vertex.lighti(0, 0);
            vertex.normalvd(face == Face.TOWARDS ? new Vec3d(0, 0, 0).subtract(p.normal) : p.normal);
            verticies[v] = vertex;
        }

        return new Polygon(verticies, -1, null);
    }

    private LinkedVertex getLinkedVertex(Vec3d positionvd) {
        for (LinkedVertex v : linked) {
            if (MCObjectUtils.equals(positionvd, v.pos)) return v;
        }
        return null;
    }
}