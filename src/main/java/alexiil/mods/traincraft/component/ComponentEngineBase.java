package alexiil.mods.traincraft.component;

import java.util.List;

import alexiil.mods.traincraft.api.component.ComponentResting;
import alexiil.mods.traincraft.api.component.IComponentInner;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.api.train.IRollingStock.Face;

/** Provides an implementation for actually applying engine output properly. */
public abstract class ComponentEngineBase extends ComponentResting {
    public ComponentEngineBase(IRollingStock stock, IComponentOuter childFront, IComponentOuter childBack, List<IComponentOuter> childMiddle,
            List<IComponentInner> innerComponents, double frontBack) {
        super(stock, childFront, childBack, childMiddle, innerComponents, frontBack);
    }

    @Override
    public void tick() {
        super.tick();

        double speedDelta = 1 - Math.abs(stock().speed() / maxSpeed());
        if (speedDelta > 0) {
            double engine = speedDelta * maxEngineOutput();
            stock().getConnector(Face.FRONT).applyMomentum(engine / 20.0, Face.FRONT);
        }
    }

    public abstract double maxSpeed();
}
