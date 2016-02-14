package alexiil.mods.traincraft.compat.vanilla;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.train.Connector.ConnectorFactory;
import alexiil.mods.traincraft.component.ComponentSmallWheel;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public abstract class EntityStockVanilla extends EntityRollingStockBase {
    protected static final ComponentTrackFollower defaultWheel1, defaultWheel2;
    private static final ConnectorFactory defaultFrontConnector, defaultBackConnector;

    static {
        defaultWheel1 = new ComponentSmallWheel(null, -0.25, 0);
        defaultWheel2 = new ComponentSmallWheel(null, 0.25, 1);

        defaultFrontConnector = new ConnectorFactory(0.55, 1000, null);
        defaultBackConnector = new ConnectorFactory(-0.55, 1000, null);
    }

    public EntityStockVanilla(World world, IComponent component) {
        super(world, component, defaultFrontConnector, defaultBackConnector);
    }

    @Override
    public double maxBrakingForce() {
        return 0;
    }

    @Override
    public boolean isBraking() {
        return false;
    }

    @Override
    public double engineOutput() {
        return 0;
    }
}
