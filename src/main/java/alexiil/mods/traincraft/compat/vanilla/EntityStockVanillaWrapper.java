package alexiil.mods.traincraft.compat.vanilla;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.train.Connector.ConnectorFactory;
import alexiil.mods.traincraft.component.ComponentSmallWheel;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public abstract class EntityStockVanillaWrapper<M extends EntityMinecart> extends EntityRollingStockBase {
    protected static final ComponentTrackFollower wheel1, wheel2;
    private static final ConnectorFactory frontConnector, backConnector;

    private final M wrapped;

    static {
        wheel1 = new ComponentSmallWheel(null, -0.25, 0);
        wheel2 = new ComponentSmallWheel(null, 0.25, 1);

        frontConnector = new ConnectorFactory(0.55, 1000, null);
        backConnector = new ConnectorFactory(-0.55, 1000, null);
    }

    public EntityStockVanillaWrapper(World world, IComponent component, M wrapped) {
        super(world, component, frontConnector, backConnector);
        this.wrapped = wrapped;
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

    public M getWrapped() {
        return wrapped;
    }
}
