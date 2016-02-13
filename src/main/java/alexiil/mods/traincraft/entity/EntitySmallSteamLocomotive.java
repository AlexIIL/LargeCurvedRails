package alexiil.mods.traincraft.entity;

import java.util.Collections;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.api.train.Connector.ConnectorFactory;
import alexiil.mods.traincraft.component.ComponentSmallSteamLocomotive;
import alexiil.mods.traincraft.component.ComponentSmallWheel;

public class EntitySmallSteamLocomotive extends EntityRollingStockPowered {
    private static final IComponent steamComponent;
    private static final ConnectorFactory frontConnector, backConnector;

    static {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(null, -0.3, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(null, 0.3, 1);
        // IComponentInner engine = null;
        // IComponentInner smokeStack = null;
        steamComponent = new ComponentSmallSteamLocomotive(null, wheel1, wheel2, Collections.emptyList(), Collections.emptyList(),
                // ImmutableList.of(engine, smokeStack),
                0.5);

        frontConnector = new ConnectorFactory(0.55, 1000, steamComponent);
        backConnector = new ConnectorFactory(-0.55, 1000, steamComponent);
    }

    public EntitySmallSteamLocomotive(World world) {
        super(world, steamComponent, frontConnector, backConnector);
    }

    @Override
    public int weight() {
        return 300 /* + items() */;
    }

    @Override
    public boolean isBraking() {
        return false;
    }

    @Override
    public double maxSpeed() {
        return 4;
    }

    @Override
    public double maxEnginePower() {
        return 600;
    }
}
