package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.MCObjectUtils;
import alexiil.mods.traincraft.client.model.MutableQuad.Vertex;
import alexiil.mods.traincraft.client.model.Plane.Face;
import alexiil.mods.traincraft.client.model.Polygon.LinkedVertex;

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

    @Deprecated
    public static List<MutableQuad>[] bisectAddingFaces(List<MutableQuad> quads, Plane p, TextureAtlasSprite repeatingSprite) {
        List<MutableQuad>[] bisected = bisect(quads, p);
        for (List<MutableQuad> lst : bisected) {
            Bisector bis = new Bisector();
            lst.addAll(bis.generateExtraFaces(lst, p, repeatingSprite));
        }
        return bisected;
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
        total.addAll(squishIntoPlane(toSquish, plane));
        return total;
    }

    private static List<MutableQuad> squishIntoPlane(List<MutableQuad> toSquish, Plane plane) {
        List<MutableQuad> squished = new ArrayList<>();
        for (MutableQuad q : toSquish) {
            MutableQuad post = new MutableQuad(q);
            for (Vertex v : post.verticies()) {
                Vec3 pos = v.positionvd();
                pos = squish(pos, plane);
                v.positionvd(pos);
            }
            squished.add(post);
        }
        return squished;
    }

    private static Vec3 squish(Vec3 vec, Plane plane) {
        // v* is the vector that we want (we are given x and z, we want y)
        double vx = vec.xCoord, vz = vec.zCoord;
        // p* is the plane point
        double px = plane.point.xCoord, py = plane.point.yCoord, pz = plane.point.zCoord;
        // n* is the planes normal
        double nx = plane.normal.xCoord, ny = plane.normal.yCoord, nz = plane.normal.zCoord;
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
        return new Vec3(vec.xCoord, vy, vec.zCoord);
    }

    @Deprecated
    public static List<MutableQuad> generateExtraFaces(List<MutableQuad> quads, Plane p, TextureAtlasSprite sprite, Face face) {
        Bisector bis = new Bisector();
        return bis.generateExtraFaces(quads, p, sprite);
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

    @Deprecated
    private static class Bisector {
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
            // Vector3d normal = new Vector3d(p.normal.xCoord, p.normal.yCoord, p.normal.zCoord);
            // Vector3d wantedNormal = new Vector3d(0, 1, 0);
            // Matrix3d matrix = MatrixUtil.rotate(normal, wantedNormal);
            //
            Point3d lastButOneP = new Point3d(lastButOne.pos.xCoord, lastButOne.pos.yCoord, lastButOne.pos.zCoord);
            Point3d lastP = new Point3d(last.pos.xCoord, last.pos.yCoord, last.pos.zCoord);
            Point3d toCheckP = new Point3d(toCheck.pos.xCoord, toCheck.pos.yCoord, toCheck.pos.zCoord);
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
                vertex.normalvd(face == Face.TOWARDS ? new Vec3(0, 0, 0).subtract(p.normal) : p.normal);
                verticies[v] = vertex;
            }

            return new Polygon(verticies, -1, null);
        }

        private LinkedVertex getLinkedVertex(Vec3 positionvd) {
            for (LinkedVertex v : linked) {
                if (MCObjectUtils.equals(positionvd, v.pos)) return v;
            }
            return null;
        }
    }
}
