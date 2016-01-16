package alexiil.mods.traincraft.entity;

import java.util.Collections;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.component.ComponentSmallSteamLocomotive;
import alexiil.mods.traincraft.component.ComponentSmallWheel;

public class EntitySmallSteamLocomotive extends EntityRollingStockBase {
    private static final IComponent mainComponent;

    static {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(null, -0.3, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(null, 0.3, 1);
        mainComponent = new ComponentSmallSteamLocomotive(null, wheel1, wheel2, Collections.emptyList(), 0.5);
    }

    public EntitySmallSteamLocomotive(World world) {
        super(world, mainComponent);
    }

    @Override
    public double maxBrakingForce() {
        return 10;// TODO: Experiment!
    }

    @Override
    public int weight() {
        return 500 /* + items() */;
    }

    @Override
    public boolean isBraking() {
        return false;
    }

    @Override
    public double engineOutput() {
        return 100;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return super.getCollisionBoundingBox();
    }
}
