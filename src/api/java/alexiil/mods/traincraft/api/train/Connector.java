package alexiil.mods.traincraft.api.train;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

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

    // This is probably wrong
    public void slowAll(double maxNewtons) {
        Set<IRollingStock> parts = Sets.newIdentityHashSet();
        parts.add(stock);
        boolean added = true;
        while (added) {
            List<IRollingStock> s = new ArrayList<>();
            for (IRollingStock test : parts) {
                Connector c = test.getConnector(Face.FRONT).joinedTo;
                if (c != null) s.add(c.stock);
                c = test.getConnector(Face.BACK).joinedTo;
                if (c != null) s.add(c.stock);
            }
            added = parts.addAll(s);
        }

        double momentum = parts.stream().mapToDouble(p -> p.momentum()).sum();
        maxNewtons = Math.abs(maxNewtons);
        if (Math.abs(momentum) <= maxNewtons) {
            parts.forEach(s -> s.setSpeed(0));
        } else {
            int totalWeight = parts.stream().mapToInt(s -> s.weight()).sum();
            if (momentum < 0) maxNewtons *= -1;
            double newSpeed = (momentum - maxNewtons) / totalWeight;
            parts.forEach(s -> s.setSpeed(newSpeed));
        }
    }

    public void pullAll(double newtons) {
        Set<IRollingStock> allStock = Sets.newIdentityHashSet();
        allStock.add(stock);
        boolean added = true;
        while (added) {
            List<IRollingStock> s = new ArrayList<>();
            for (IRollingStock test : allStock) {
                Connector c = test.getConnector(Face.FRONT).joinedTo;
                if (c != null) s.add(c.stock);
                c = test.getConnector(Face.BACK).joinedTo;
                if (c != null) s.add(c.stock);
            }
            added = allStock.addAll(s);
        }
        allStock.remove(null);

        double totalMomentum = allStock.stream().mapToDouble(s -> s.momentum()).sum();
        totalMomentum += newtons;
        int totalWeight = allStock.stream().mapToInt(s -> s.weight()).sum();
        double speed = totalMomentum / totalWeight;
        allStock.forEach(s -> s.setSpeed(speed));
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
