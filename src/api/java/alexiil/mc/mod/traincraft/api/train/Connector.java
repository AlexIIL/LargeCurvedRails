package alexiil.mc.mod.traincraft.api.train;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.component.IComponentOuter;
import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.train.IRollingStock.Face;

public class Connector {
    private final IRollingStock stock;
    private final IComponentOuter componentOuter;
    private final double offset;
    private Connector joinedTo = null;
    /** True if this connector is fastened to the other connector, false if it is just pushing it . */
    private boolean joinedStrongly = true;

    public Connector(IRollingStock stock, IComponentOuter outer, double offset) {
        this.stock = stock;
        this.componentOuter = outer;
        this.offset = offset;
    }

    public boolean attemptJoin(Connector to, boolean simulate) {
        if (joinedTo != null) return false;
        if (to.joinedTo != null) return false;
        Vec3 joinPos = componentOuter.getTrackPos().add(MathUtil.scale(componentOuter.getTrackDirection(), offset));
        Vec3 otherPos = to.componentOuter.getTrackPos().add(MathUtil.scale(to.componentOuter.getTrackDirection(), to.offset));
        double dist = joinPos.distanceTo(otherPos);
        if (dist > 0.3) return false;
        if (!simulate) {
            joinedTo = to;
            to.joinedTo = this;

            joinedStrongly = true;
            to.joinedStrongly = true;
        }
        return true;
    }

    public boolean attemptJoinAround(boolean simulate) {
        Entity rollingStock = ((Entity) stock);
        World world = rollingStock.worldObj;
        if (world == null) throw new NullPointerException("world");
        AxisAlignedBB aabb = rollingStock.getCollisionBoundingBox().expand(5, 5, 5);
        for (Entity ent : world.getEntitiesWithinAABB(Entity.class, aabb)) {
            if (ent instanceof IRollingStock) {
                for (Face f : Face.values()) {
                    if (attemptJoin(((IRollingStock) ent).getConnector(f), simulate)) return true;
                }
            }
        }
        return false;
    }

    private Connector getOther() {
        if (stock.getConnector(Face.BACK) == this) return stock.getConnector(Face.FRONT);
        else return stock.getConnector(Face.BACK);
    }

    private static void addPushedStock(Map<IRollingStock, Boolean> set, Connector from) {
        Connector next = from.joinedTo;
        while (next != null) {
            boolean right = next.stock.getConnector(Face.BACK) == next ? true : false;
            Boolean before = set.put(next.stock, right);
            if (before != null) break;
            next = next.getOther().joinedTo;
        }
    }

    private static void addPulledStock(Map<IRollingStock, Boolean> set, Connector from) {
        Connector next = from.joinedTo;
        while (next != null && next.joinedStrongly) {
            boolean right = next.stock.getConnector(Face.FRONT) == next ? true : false;
            Boolean before = set.put(next.stock, right);
            if (before != null) break;
            next = next.getOther().joinedTo;
        }
    }

    /** Applies some momentum to this connector, going in a particular direction. If newtons is negative then the
     * direction will be reversed. */
    public void applyMomentum(double newtons, Face direction) {
        // if (newtons < 0) direction = direction.opposite();
        Map<IRollingStock, Boolean> map = new IdentityHashMap<>();
        map.put(stock, true);

        addPushedStock(map, stock.getConnector(direction));
        addPulledStock(map, stock.getConnector(direction.opposite()));

        double momentum = map.entrySet().stream().mapToDouble(e -> e.getKey().momentum() * (e.getValue() ? 1 : -1)).sum();
        if (direction == Face.FRONT) momentum += newtons;
        else momentum -= newtons;
        int totalWeight = map.keySet().stream().mapToInt(s -> s.weight()).sum();
        double speed = momentum / totalWeight;

        map.entrySet().forEach(e -> e.getKey().setSpeed(speed * (e.getValue() ? 1 : -1)));
    }

    /** Removes some momentum from this connector. */
    public void slowAll(double newtons) {
        Face direction = stock.momentum() > 0 ? Face.FRONT : Face.BACK;
        Map<IRollingStock, Boolean> map = new IdentityHashMap<>();
        map.put(stock, false);

        addPushedStock(map, stock.getConnector(direction.opposite()));
        addPulledStock(map, stock.getConnector(direction));

        double momentum = map.entrySet().stream().mapToDouble(e -> e.getKey().momentum() * (e.getValue() ? 1 : -1)).sum();
        if (momentum <= 0) {
            if (newtons <= momentum) momentum = 0;
            else momentum += newtons;
        } else {
            if (newtons >= momentum) momentum = 0;
            else momentum -= newtons;
        }
        int totalWeight = map.keySet().stream().mapToInt(s -> s.weight()).sum();
        double speed = momentum / totalWeight;

        map.entrySet().forEach(e -> e.getKey().setSpeed(speed * (e.getValue() ? 1 : -1)));
    }
}
