package alexiil.mods.traincraft.api.train;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;

public class StockPathFinder {
    public final Map<BehaviourWrapper, PathNode> paths = new HashMap<>();
    private final IRollingStock stock;

    public StockPathFinder(IRollingStock stock) {
        this(stock, null);
    }

    public StockPathFinder(IRollingStock stock, BehaviourWrapper path) {
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

    private Stream<BehaviourWrapper> findPaths(Vec3 attachPoint, int pos, Vec3 direction) {
        World world = world();
        BlockPos toTry = new BlockPos(attachPoint);

        Builder<BehaviourWrapper> tracks = Stream.builder();

        for (BlockPos offset : BlockPos.getAllInBox(new BlockPos(-2, -2, -2), new BlockPos(2, 2, 2))) {
            BlockPos p = toTry.add(offset);
            IBlockState state = world.getBlockState(p);
            TrackBehaviour track = TrainCraftAPI.TRACK_PROVIDER.getTrackFromPoint(world, p, state, attachPoint);
            if (track != null) tracks.add(TrainCraftAPI.TRACK_PROVIDER.wrap(track, world, p));
        }

        return tracks.build().filter(t -> {
            return t.getPath().direction(pos).distanceTo(direction) <= 1e-3;
        });
    }

    private Stream<BehaviourWrapper> findPathsForward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 0, direction);
    }

    private Stream<BehaviourWrapper> findPathsBackward(Vec3 attachPoint, Vec3 direction) {
        return findPaths(attachPoint, 1, direction);
    }

    private BehaviourWrapper findNextForward(BehaviourWrapper path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.forward != null) return node.forward;
        Vec3 nextStart = path.getPath().end();
        Vec3 direction = path.getPath().direction(1);
        BehaviourWrapper next = stock.controller().findBehaviour(findPathsForward(nextStart, direction));
        if (next == null) return null;
        node.forward = next;
        PathNode nextNode = new PathNode(next);
        nextNode.back = path;
        paths.put(next, nextNode);
        return next;
    }

    private BehaviourWrapper findNextBackwards(BehaviourWrapper path) {
        PathNode node = paths.get(path);
        if (node == null) return null;
        if (node.back != null) return node.back;
        Vec3 nextEnd = path.getPath().start();
        Vec3 direction = path.getPath().direction(0);
        BehaviourWrapper next = stock.controller().findBehaviour(findPathsBackward(nextEnd, direction));
        if (next == null) return null;
        node.back = next;
        PathNode nextNode = new PathNode(next);
        nextNode.forward = path;
        paths.put(next, nextNode);
        return next;
    }

    public BehaviourWrapper offsetPath(BehaviourWrapper from, double meters) {
        if (meters >= 0 && meters <= from.getPath().length()) return from;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return null;
            meters += from.getPath().length();
        }
        while (meters > 1) {
            meters -= from.getPath().length();
            from = findNextForward(from);
            if (from == null) return null;
        }
        return from;
    }

    public double offsetMeters(BehaviourWrapper from, double meters) {
        if (meters >= 0 && meters <= from.getPath().length()) return meters;
        while (meters < 0) {
            from = findNextBackwards(from);
            if (from == null) return meters;
            meters += from.getPath().length();
        }
        while (meters > from.getPath().length()) {
            meters -= from.getPath().length();
            from = findNextForward(from);
            if (from == null) return meters;
        }
        return meters;
    }

    public void usePath(BehaviourWrapper path, IComponentOuter user) {
        paths.get(path).uses.add(user);
    }

    public void releasePath(BehaviourWrapper path, IComponentOuter user) {
        PathNode node = paths.get(path);
        node.uses.remove(user);

        if (node.uses.size() != 0) return;

        if (node.forward == null) {
            // GC this node
            BehaviourWrapper back = node.back;
            if (back == null) return;
            PathNode n = paths.get(back);
            n.forward = null;
            paths.remove(path);
        } else if (node.back == null) {
            // GC this node
            BehaviourWrapper forward = node.forward;
            if (forward == null) return;
            PathNode n = paths.get(forward);
            n.back = null;
            paths.remove(path);
        }
    }

    public static class PathNode {// TODO: Convert this to a proper linked list. Or just fix the broken behaviour above.
        public final BehaviourWrapper thisPath;
        public BehaviourWrapper forward, back;
        public final Set<IComponentOuter> uses = Sets.newIdentityHashSet();

        public PathNode(BehaviourWrapper thisPath) {
            this.thisPath = thisPath;
        }
    }
}
