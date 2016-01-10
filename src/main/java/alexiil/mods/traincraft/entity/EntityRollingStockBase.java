package alexiil.mods.traincraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.Train;
import alexiil.mods.traincraft.api.TrainCraftAPI;

public abstract class EntityRollingStockBase extends Entity implements IRollingStock {
    private static final int DATA_WATCHER_PROGRESS = 6;
    private static final int DATA_WATCHER_SPEED = 7;

    private ITrackPath currentPath;
    private double progress = 0;
    private double lastRecievedProgress = 0;

    // Init this in the tick
    private Train train = null;

    private Vec3 lookVec = new Vec3(0, 0, 1);

    /** Speed (in meters per tick) */
    private double speedMPT = 2.0 / 20.0;

    public EntityRollingStockBase(World worldIn) {
        super(worldIn);
        if (worldObj != null) TrainCraftAPI.apiLog.info("EntityRollingStockBase::<init> | Created with an ID of " + getEntityId() + ", "
            + worldObj.isRemote + ", found = " + (worldObj.getEntityByID(getEntityId()) != null));
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
        if (currentPath == null) return super.getPositionVector();
        return currentPath.interpolate(progress);
    }

    @Override
    public Vec3 getPathDirection(Face direction) {
        if (currentPath == null) return lookVec;
        return direction == Face.FRONT ? currentPath.direction(progress) : new Vec3(0, 0, 0).subtract(currentPath.direction(progress));
    }

    @SideOnly(Side.CLIENT)
    public Vec3 getInterpolatedPosition(float partialTicks) {
        if (currentPath == null) return super.getPositionVector();
        double progress = this.progress + partialTicks * speedMPT / currentPath.length();
        return currentPath.interpolate(progress);
    }

    @SideOnly(Side.CLIENT)
    public Vec3 getInterpolatedDirection(float partialTicks) {
        if (currentPath == null) return lookVec;
        double progress = this.progress + partialTicks * speedMPT / currentPath.length();
        return lookVec = currentPath.direction(progress > 1 ? 1 : progress);
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
        if (train == null) {
            if (getEntityWorld().isRemote) return;
            train = new Train(this);
            TrainCraftAPI.WORLD_CACHE.createTrain(train);
            return;
        }
        // getTrain().tick(this);
        if (getEntityWorld().isRemote) {
            // double speed = dataWatcher.getWatchableObjectFloat(DATA_WATCHER_SPEED);
            // speedMPT = speed;

            // Update the server side progress seperatly from the client side progress
            double prog = dataWatcher.getWatchableObjectFloat(DATA_WATCHER_PROGRESS);
            if (prog > lastRecievedProgress) {
                progress = prog;
                lastRecievedProgress = prog;
            } else {// We must have changed paths (THIS IS UGLY!)
                progress = prog;
                lastRecievedProgress = prog;
                currentPath = getTrain().requestNextTrackPath(this, currentPath, Face.FRONT);
            }

            if (currentPath != null) {
                double length = currentPath.length();
                progress += speedMPT / length;
                Vec3 interp = currentPath.interpolate(progress);
                setPosition(interp.xCoord, interp.yCoord, interp.zCoord);
                lookVec = currentPath.direction(progress);
            }

        } else {
            // Find the best track path
            if (currentPath == null) {
                currentPath = getTrain().requestNextTrackPath(this, null, Face.FRONT);

                // The client "lerps" between a place and another place on the track. Maybe the client shouldn't request
                // things? Maybe the client needs the current track path at all times?
                
                // Maybe the component should save+send the blockpos of the path and future path to use? but why only a single one? should the train request more?

            }

            // Use the current track path
            if (currentPath != null) {
                double length = currentPath.length();
                progress += speedMPT / length;
                if (progress > 1) {
                    Vec3 newPos = currentPath.interpolate(1.001);
                    setPosition(newPos.xCoord, newPos.yCoord, newPos.zCoord);
                    // Because we are off the path, lets just reset it to 0 and pretend nothing happened
                    // TODO: Past paths! And future paths!
                    progress = progress - 1;
                    ITrackPath path = currentPath;
                    currentPath = getTrain().requestNextTrackPath(this, currentPath, Face.FRONT);
                    getTrain().disposePath(path, this, Face.FRONT);
                } else {
                    Vec3 newPos = currentPath.interpolate(progress);
                    setPosition(newPos.xCoord, newPos.yCoord, newPos.zCoord);

                    lookVec = currentPath.direction(progress);
                    dataWatcher.updateObject(DATA_WATCHER_PROGRESS, (float) progress);
                }
            }
        }
    }

    public Vec3 vec3() {
        return new Vec3(posX, posY, posZ);
    }

    @Override
    protected void entityInit() {
        // Flags
        dataWatcher.addObject(DATA_WATCHER_PROGRESS, 0.0f);
        dataWatcher.addObject(DATA_WATCHER_SPEED, (float) speedMPT);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {}

    public boolean isOnTrack() {
        return currentPath != null;
    }
}
