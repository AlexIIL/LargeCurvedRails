package alexiil.mods.traincraft.api.train;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.track.TrackPathProvider;
import alexiil.mods.traincraft.api.track.path.ITrackPath;

public class StockPathFinder {
    public final Map<ITrackPath, PathNode> paths = new HashMap<>();
    private final IRollingStock stock;

    public StockPathFinder(IRollingStock stock) {
        this(stock, null);
    }

    public StockPathFinder(IRollingStock stock, ITrackPath path) {
        if (stock == null) throw new NullPointerException("stock");
        if (!(stock instanceof Entity)) throw new IllegalArgumentException(stock.getClass() + " was not an instanceof Entity!");
        paths.put(path, new PathNode(path));
        this.stock = stock;
    }

    // ###########################
    //
    // Path changes
    //
    // ###########################

    private World world() {
        return ((Entity) stock).getEntityWorld();
    }

    private Stream<ITrackPath> findPaths(Vec3 attachPoint, int pos, Vec3 direction) {
        Builder<ITrackPath> paths = Stream.builder();
        World world = world();
        BlockPos toTry = new BlockPos(attachPoint);

        TrackPathProvider.getPathsAsStream(world, toTry, world.getBlockState(toTry)).forEach(p -> paths.add(p));
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos offset = toTry.offset(face);
            TrackPathProvider.getPathsAsStream(world, offset, world.getBlockState(offset)).forEach(p -> paths.add(p));
            for (EnumFacing face2 : EnumFacing.values()) {
                BlockPos offset2 = offset.offset(face2);
                TrackPathProvider.getPathsAsStream(world, offset2, world.getBlockState(offset2)).forEach(p -> paths.add(p));
            }
        }
        // @formatter:off
        return paths.build()
                .flatMap(p -> Arrays.asList(p, p.reverse()).stream())
                // Add a *tiny* bit of leway for path positions
                //   -Arcs don't quite work out start and ends exactly.
                .filter(p -> p.interpolate(pos).distanceTo(attachPoint) <= 1E-2)
                .filter(p -> p.direction(pos).distanceTo(direction) <= 1.2);
    }

    private Stream<ITrackPath> findPathsForward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 0, direction);
    }

    private Stream<ITrackPath> findPathsBackward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 1, direction);
    }

    private ITrackPath findNextForward(ITrackPath path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.forward != null) return node.forward;
        Vec3 nextStart = path.end();
        Vec3 direction = path.direction(1);
        ITrackPath next = findPathsForward(nextStart, direction).findFirst().orElse(null);
        if (next == null) return null;
        node.forward = next;
        PathNode nextNode = new PathNode(next);
        nextNode.back = path;
        paths.put(next, nextNode);
        return next;
    }

    private ITrackPath findNextBackwards(ITrackPath path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.back != null) return node.back;
        Vec3 nextEnd = path.start();
        Vec3 direction = path.direction(0);
        ITrackPath next = findPathsBackward(nextEnd, direction).findFirst().orElse(null);
        if (next == null) return null;
        node.back = next;
        PathNode nextNode = new PathNode(next);
        nextNode.forward = path;
        paths.put(next, nextNode);
        return next;
    }

    public ITrackPath offsetPath(ITrackPath from, double meters) {
        if (meters >= 0 && meters <= from.length()) return from;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return null;
            meters += from.length();
        }
        while (meters > 1) {
            meters -= from.length();
            from = findNextForward(from);
            if (from == null) return null;
        }
        return from;
    }

    public double offsetMeters(ITrackPath from, double meters) {
        if (meters >= 0 && meters <= from.length()) return meters;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return meters;
            meters += from.length();
        }
        while (meters > from.length()) {
            meters -= from.length();
            from = findNextForward(from);
            if (from == null) return meters;
        }
        return meters;
    }

    public void usePath(ITrackPath path, IComponentOuter user) {
        paths.get(path).uses.add(user);
    }

    public void releasePath(ITrackPath path,  IComponentOuter user) {
        PathNode node = paths.get(path);
        node.uses.remove(user);

        if (node.uses.size() != 0) return;

        if (node.forward == null) {
            // GC this node
            ITrackPath back = node.back;
            if (back == null) return;
            PathNode n = paths.get(back);
            n.forward = null;
            paths.remove(path);
        } else if (node.back == null) {
            // GC this node
            ITrackPath forward = node.forward;
            if (forward == null) return;
            PathNode n = paths.get(forward);
            n.back = null;
            paths.remove(path);
        }
    }

    public static class PathNode {// TODO: Convert this to a proper linked list. Or just fix the broken behaviour above.
        public final ITrackPath thisPath;
        public ITrackPath forward, back;
        public final Set<IComponentOuter> uses = Sets.newIdentityHashSet();

        public PathNode(ITrackPath thisPath) {
            this.thisPath = thisPath;
        }
    }
}
