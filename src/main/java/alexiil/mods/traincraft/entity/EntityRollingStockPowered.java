package alexiil.mods.traincraft.entity;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.train.Connector.ConnectorFactory;

public abstract class EntityRollingStockPowered extends EntityGenericRollingStock {
    public EntityRollingStockPowered(World world, IComponentOuter componentOuter, ConnectorFactory front, ConnectorFactory back) {
        super(world, componentOuter, front, back);
    }

    @Override
    public final double engineOutput() {
        double speedDelta = 1 - Math.abs(speed() / maxSpeed());
        if (speedDelta < 0) return 0;
        return speedDelta * maxEnginePower();
    }

    @Override
    public double maxBrakingForce() {
        return maxEnginePower();
    }

    /** @return The maximum number of meters per second that this powered rolling stock can move at. */
    public abstract double maxSpeed();

    /** @return The maximum number of newtons that the engine can output. */
    public abstract double maxEnginePower();

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote) connectorFront.applyMomentum(engineOutput() / 20.0, Face.FRONT);
    }
}
