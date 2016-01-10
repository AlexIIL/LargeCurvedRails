package alexiil.mods.traincraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.Train;
import alexiil.mods.traincraft.api.component.IComponent;

public abstract class EntityRollingStockBase extends Entity implements IRollingStock {
    private static final int DATA_WATCHER_SPEED = 5;

    // Each component uses: [ int (flag), float (progress), blockpos (track), int (track index)]
    private static final int DATA_WATCHER_COMPONENT_START = 6;
    private static final int DATA_WATCHER_COMPONENT_STRIDE = 4;
    private static final Object[] DATA_WATCHER_COMPONENT_VARS = { Integer.valueOf(0), Float.valueOf(0), new BlockPos(0, 0, 0), Integer.valueOf(0) };

    private final IComponent mainComponent;

    // Init this in the tick
    private Train train = null;

    private Vec3 lookVec = new Vec3(0, 0, 1);

    /** Speed (in meters per tick) */
    private double speedMPT = 2.0 / 20.0;

    public EntityRollingStockBase(World world) {
        super(world);
        // TODO!
        mainComponent = null;
    }

    public EntityRollingStockBase(World worldIn, IComponent component) {
        super(worldIn);
        this.mainComponent = component;
    }

    @Override
    public Train getTrain() {
        return train;
    }

    @Override
    public void setTrain(Train train) {
        this.train = train;
    }

    @Override
    public Vec3 getPathPosition() {
        return mainComponent.getTrackPos();
    }

    @Override
    public Vec3 getPathDirection(Face direction) {
        return mainComponent.getTrackDirection();
    }

    @SideOnly(Side.CLIENT)
    public Vec3 getInterpolatedPosition(float partialTicks) {
        return mainComponent.getTrackPos(partialTicks);
    }

    @SideOnly(Side.CLIENT)
    public Vec3 getInterpolatedDirection(float partialTicks) {
        return mainComponent.getTrackDirection(partialTicks);
    }

    public abstract ResourceLocation getModelLocation();

    public abstract ResourceLocation getTextureLocation();

    @Override
    public double speed(Face face) {
        if (face == Face.FRONT) return speedMPT * 20;
        return -speedMPT * 20;
    }

    @Override
    public void setSpeed(double newSpeed, Face face) {
        if (face == Face.BACK) newSpeed = -newSpeed;
        this.speedMPT = newSpeed / 20;
        dataWatcher.updateObject(DATA_WATCHER_SPEED, (float) speedMPT);
    }

    @Override
    public double inclination(Face face) {
        // We always go forwards atm TODO Change that :)
        return lookVec.yCoord;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        mainComponent.tick();
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(DATA_WATCHER_SPEED, (float) speedMPT);
        // Each track following component uses these flags

        // FIXME: Use the number of trackFollowers to init this.
        // But I might not ba able to...?

        for (int i = 0; i < 4; i++) {
            int start = DATA_WATCHER_COMPONENT_START + DATA_WATCHER_COMPONENT_STRIDE * i;
            for (int j = 0; j < DATA_WATCHER_COMPONENT_VARS.length; j++) {
                dataWatcher.addObject(start + j, DATA_WATCHER_COMPONENT_VARS[j]);
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {}
}
