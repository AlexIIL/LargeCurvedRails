package alexiil.mods.traincraft.entity;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponent;
import alexiil.mods.traincraft.component.ComponentCart;
import alexiil.mods.traincraft.component.ComponentSmallWheel;

/** Designates a simple rolling stock with no movement capabilites by itself */
public class EntityRollingStockCart extends EntityRollingStockBase {
    private static final IComponent mainComponent;

    static {
        ComponentTrackFollower wheel1 = new ComponentSmallWheel(null, -0.45, 0);
        ComponentTrackFollower wheel2 = new ComponentSmallWheel(null, 0.45, 1);
        mainComponent = new ComponentCart(null, wheel1, wheel2, 0.5);
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
        return 200;
    }

    @Override
    public boolean isBraking() {
        return false;
    }

    @Override
    public double resistance() {
        double C = 0.08;
        double frontArea = 0.25;
        double speed = Math.abs(speed(Face.FRONT));
        return C * weight() * speed + frontArea * speed * speed;
    }

    @Override
    public double engineOutput(Face face) {
        return 0;
    }
}
