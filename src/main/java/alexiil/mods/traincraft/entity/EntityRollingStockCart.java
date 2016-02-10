package alexiil.mods.traincraft.entity;

import java.util.Collections;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.train.Connector.ConnectorFactory;
import alexiil.mods.traincraft.component.ComponentCart;
import alexiil.mods.traincraft.component.ComponentSmallWheel;

/** Designates a simple rolling stock with no movement capabilites by itself */
public class EntityRollingStockCart extends EntityRollingStockBase {
    private static final IComponent cartComponent;
    private static final ConnectorFactory frontConnector, backConnector;

    static {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(null, -0.25, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(null, 0.25, 1);
        // IComponentInner openChest = null;
        cartComponent = new ComponentCart(null, wheel1, wheel2, Collections.emptyList(), Collections.emptyList()
        // ImmutableList.of(openChest)
        , 0.5);

        frontConnector = new ConnectorFactory(0.55, 1000, cartComponent);
        backConnector = new ConnectorFactory(-0.55, 1000, cartComponent);
    }

    public EntityRollingStockCart(World world) {
        super(world, cartComponent, frontConnector, backConnector);
    }

    @Override
    public double maxBrakingForce() {
        return 10;
    }

    @Override
    public int weight() {
        return 100 /* + items() */;
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
