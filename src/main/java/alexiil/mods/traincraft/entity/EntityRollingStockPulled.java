package alexiil.mods.traincraft.entity;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Designates a simple rolling stock with no movement capabilites by itself */
public class EntityRollingStockPulled extends EntityRollingStockBase {
    private static final ResourceLocation modelLocation = new ResourceLocation("traincraft:models/trains/wheel_small.obj");
    private static final ResourceLocation textureLocation = new ResourceLocation("traincraft:textures/trains/wheel_small.png");

    public EntityRollingStockPulled(World world) {
        super(world);
    }

    @Override
    public ResourceLocation getModelLocation() {
        return modelLocation;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return textureLocation;
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
