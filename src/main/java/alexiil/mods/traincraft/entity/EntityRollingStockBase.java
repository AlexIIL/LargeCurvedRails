package alexiil.mods.traincraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.*;

public abstract class EntityRollingStockBase extends Entity implements IRollingStock {
    private static final int DATA_WATCHER_FLAGS = 5;

    private static final int DATA_WATCHER_PROGRESS = 6;

    private static final int DATA_WATCHER_LAST_TRACK_X = 7;
    private static final int DATA_WATCHER_LAST_TRACK_Y = 8;
    private static final int DATA_WATCHER_LAST_TRACK_Z = 9;

    private static final int DATA_WATCHER_PATH_INDEX = 10;
    private static final int DATA_WATCHER_SPEED = 11;

    private static final int FLAG_HAS_PATH = 1;
    private static final int FLAG_PATH_REVERSED = 2;

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
        TrainCraftAPI.apiLog.info("EntityRollingStockBase::<init> | Created with an ID of " + getEntityId());
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
        // This sends the train before the client has the entity... somehow....
        TrainCraftAPI.apiLog.info("EntityRollingStockBase::onUpdate | Ticked with an ID of " + getEntityId() + ", " + worldObj.isRemote);
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
            if (prog != lastRecievedProgress) {
                progress = prog;
                lastRecievedProgress = prog;
            }

            if (hasChangedPaths()) {
                int flags = dataWatcher.getWatchableObjectInt(DATA_WATCHER_FLAGS);
                if ((flags & FLAG_HAS_PATH) == FLAG_HAS_PATH) {
                    int x = dataWatcher.getWatchableObjectInt(DATA_WATCHER_LAST_TRACK_X);
                    int y = dataWatcher.getWatchableObjectInt(DATA_WATCHER_LAST_TRACK_Y);
                    int z = dataWatcher.getWatchableObjectInt(DATA_WATCHER_LAST_TRACK_Z);
                    BlockPos currentTrack = new BlockPos(x, y, z);

                    int index = dataWatcher.getWatchableObjectInt(DATA_WATCHER_PATH_INDEX);

                    ITrackPath[] paths = TrackPathProvider.getPathsFor(worldObj, currentTrack, worldObj.getBlockState(currentTrack));
                    if (index >= paths.length || index < 0) {
                        // Something went wrong...
                        currentPath = null;
                        currentTrack = null;
                    } else {
                        currentPath = paths[index];
                        if ((flags & FLAG_PATH_REVERSED) == FLAG_PATH_REVERSED) {
                            currentPath = currentPath.reverse();
                        }
                    }
                } else {
                    currentPath = null;
                }
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

                if (currentPath == null) {
                    dataWatcher.updateObject(DATA_WATCHER_FLAGS, 0);
                } else {
                    int bestIndex = -1;
                    boolean reversed = false;
                    if (currentPath != null) {
                        dataWatcher.updateObject(DATA_WATCHER_LAST_TRACK_X, currentPath.creatingBlock().getX());
                        dataWatcher.updateObject(DATA_WATCHER_LAST_TRACK_Y, currentPath.creatingBlock().getY());
                        dataWatcher.updateObject(DATA_WATCHER_LAST_TRACK_Z, currentPath.creatingBlock().getZ());
                        dataWatcher.updateObject(DATA_WATCHER_FLAGS, FLAG_HAS_PATH + (reversed ? FLAG_PATH_REVERSED : 0));
                        dataWatcher.updateObject(DATA_WATCHER_PATH_INDEX, bestIndex);
                        dataWatcher.updateObject(DATA_WATCHER_PROGRESS, 0f);
                    } else {
                        dataWatcher.updateObject(DATA_WATCHER_FLAGS, 0);
                    }
                }
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

    private boolean hasChangedPaths() {
        return true;
        // List<WatchableObject> list = dataWatcher.getChanged();
        // if (list == null) return false;
        // for (WatchableObject watched : list) {
        // int id = watched.getDataValueId();
        // if (id == DATA_WATCHER_FLAGS) return true;
        // if (id == DATA_WATCHER_LAST_TRACK_X) return true;
        // if (id == DATA_WATCHER_LAST_TRACK_Y) return true;
        // if (id == DATA_WATCHER_LAST_TRACK_Z) return true;
        // if (id == DATA_WATCHER_PATH_INDEX) return true;
        // }
        // return false;
    }

    public Vec3 vec3() {
        return new Vec3(posX, posY, posZ);
    }

    @Override
    protected void entityInit() {
        // Flags
        dataWatcher.addObject(DATA_WATCHER_FLAGS, 0);

        // Last Track Pos
        dataWatcher.addObject(DATA_WATCHER_LAST_TRACK_X, 0);
        dataWatcher.addObject(DATA_WATCHER_LAST_TRACK_Y, 0);
        dataWatcher.addObject(DATA_WATCHER_LAST_TRACK_Z, 0);

        dataWatcher.addObject(DATA_WATCHER_PATH_INDEX, 0);
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
