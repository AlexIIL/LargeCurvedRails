package alexiil.mods.traincraft.api.train;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.train.IRollingStock.Face;
import alexiil.mods.traincraft.lib.MathUtil;

public class Connector {
    private final IRollingStock stock;
    private final IComponent component;
    private final double offset;
    private Connector joinedTo = null;
    /** True if this connector is fastened to the other connector, false if it is just pushing it . */
    private boolean joinedStrongly = false;

    public Connector(IRollingStock stock, ConnectorFactory factory) {
        this.stock = stock;
        this.component = factory.getComponent(stock);
        this.offset = factory.offset;
    }

    public boolean attemptJoin(Connector to, boolean simulate) {
        if (joinedTo != null) return false;
        if (to.joinedTo != null) return false;
        // Firstly verify track paths
        Vec3 joinPos = component.getTrackPos().add(MathUtil.scale(component.getTrackDirection(), offset));
        Vec3 otherPos = to.component.getTrackPos().add(MathUtil.scale(to.component.getTrackDirection(), to.offset));
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
        // Just search through all entities
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
        Face direction = stock.getConnector(Face.FRONT) == this ? Face.FRONT : Face.BACK;
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

    public static class ConnectorFactory {
        private final double offset, maxNewtons;
        private final boolean[] pathToComponent;

        public ConnectorFactory(double offset, double maxNewtons, IComponent component) {
            this.offset = offset;
            this.maxNewtons = maxNewtons;
            List<Boolean> lst = new ArrayList<>();
            while (component.parent() != null) {
                IComponent parent = component.parent();
                List<IComponent> children = parent.children();
                if (children.size() == 0) throw new IllegalStateException("");
                else if (children.size() == 1) lst.add(true);
                else if (children.get(0) == component) {
                    lst.add(true);
                    break;
                } else if (children.get(children.size() - 1) == component) {
                    lst.add(false);
                    break;
                }
            }
            pathToComponent = new boolean[lst.size()];
            for (int i = 0; i < pathToComponent.length; i++) {
                pathToComponent[i] = lst.get(lst.size() - i - 1);
            }
        }

        private IComponent getComponent(IRollingStock stock) {
            IComponent component = stock.mainComponent();
            for (boolean b : pathToComponent) {
                if (b) component = component.children().get(0);
                else component = component.children().get(component.children().size() - 1);
            }
            return component;
        }
    }
}
