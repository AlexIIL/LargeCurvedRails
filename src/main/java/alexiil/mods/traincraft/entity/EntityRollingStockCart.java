package alexiil.mods.traincraft.entity;

import java.util.Collections;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.component.ComponentCart;
import alexiil.mods.traincraft.component.ComponentSmallWheel;

/** Designates a simple rolling stock with no movement capabilites by itself */
public class EntityRollingStockCart extends EntityRollingStockBase {
    private static final IComponent mainComponent;

    static {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(null, -0.25, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(null, 0.25, 1);
        mainComponent = new ComponentCart(null, wheel1, wheel2, Collections.emptyList(), 0.5);
    }

    public EntityRollingStockCart(World world) {
        super(world, mainComponent);
    }

    @Override
    public double maxBrakingForce() {
        return 10;// TODO: Experiment!
    }

    @Override
    public int weight() {
        return 50 /* + items() */;
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
