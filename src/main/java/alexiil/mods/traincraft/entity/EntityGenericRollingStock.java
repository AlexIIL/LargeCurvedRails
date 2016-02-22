package alexiil.mods.traincraft.entity;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.TrainRegistry;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.api.component.ComponentTrackFollower;
import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.lib.MathUtil;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.RayTraceTrackPath;
import alexiil.mods.traincraft.api.train.*;
import alexiil.mods.traincraft.api.train.IRollingStockType.ConstructedData;
import alexiil.mods.traincraft.client.model.MatrixUtil;

import io.netty.buffer.ByteBuf;

public final class EntityGenericRollingStock extends Entity implements IRollingStock, IEntityAdditionalSpawnData {
    private static final int DATA_WATCHER_SPEED = 5;

    private static final Object[] DATA_WATCHER_COMPONENT_VARS = { Integer.valueOf(0), new Integer(0), Float.valueOf(0), new BlockPos(0, 0, 0) };
    /** Max speed of 20 meters per second */
    private static final double MAX_SPEED = 20;

    private ResourceLocation type;
    private IComponentOuter mainComponent;
    private Connector connectorFront, connectorBack;

    private StockPathFinder pathFinder = new StockPathFinder(this);
    private Vec3 lookVec = new Vec3(0, 0, 1);

    /** Speed (in meters per tick). This will always have the same sign as all other stocks in this train, and will be
     * inverted if this joins a train going in the other direction. */
    private double speedMPT = 0;

    public EntityGenericRollingStock(World world, IRollingStockType factory) {
        super(world);
        type = factory.uniqueID();
        ConstructedData data = factory.createInstance(this);
        mainComponent = data.outer;
        connectorFront = data.front;
        connectorBack = data.back;
    }

    public EntityGenericRollingStock(World world) {
        super(world);
        if (!world.isRemote) throw new IllegalStateException();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        AxisAlignedBB aabb = mainComponent.getBoundingBox();
        aabb = rotate(aabb, mainComponent().getTrackDirection());
        return aabb.offset(mainComponent.getTrackPos().xCoord, mainComponent.getTrackPos().yCoord, mainComponent.getTrackPos().zCoord);
    }

    /** Rotates the given bounding box from poiting towards (0, 0, 1) to the given direction. */
    private static AxisAlignedBB rotate(AxisAlignedBB aabb, Vec3 dir) {
        Matrix4f matrix = MatrixUtil.rotateTo(dir);
        Vec3 min = new Vec3(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        Vec3 max = new Vec3(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        boolean[] arr = { false, true };
        for (boolean x : arr) {
            for (boolean y : arr) {
                for (boolean z : arr) {
                    Point3d pd = new Point3d(//
                            x ? aabb.minX : aabb.maxX,//
                            y ? aabb.minY : aabb.maxY,//
                            z ? aabb.minZ : aabb.maxZ //
                    );
                    Point3f pf = new Point3f(pd);
                    matrix.transform(pf);
                    min = new Vec3(//
                            Math.min(min.xCoord, pf.x),//
                            Math.min(min.yCoord, pf.y),//
                            Math.min(min.zCoord, pf.z) //
                    );
                    max = new Vec3(//
                            Math.max(max.xCoord, pf.x),//
                            Math.max(max.yCoord, pf.y),//
                            Math.max(max.zCoord, pf.z) //
                    );
                }
            }
        }
        return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (entity.riddenByEntity != this && entity.ridingEntity != this) {
            if (!entity.noClip && !this.noClip) {
                Vec3 look = mainComponent.getTrackDirection();
                double speed = speedMPT * 10;
                entity.motionX += look.xCoord * speed;
                entity.motionY += look.yCoord * speed + 0.1;
                entity.motionZ += look.zCoord * speed;

                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    float damage = (float) speed;
                    damage *= damage;
                    living.attackEntityFrom(DamageSource.fallingBlock, damage);
                }
            }
        }
    }

    @Override
    public StockPathFinder pathFinder() {
        return pathFinder;
    }

    @Override
    public IComponentOuter mainComponent() {
        return mainComponent;
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

    @Override
    public float getEyeHeight() {
        // Push it up slightly off the ground
        return 0.5f;
    }

    @Override
    public double speed() {
        return speedMPT * 20;
    }

    @Override
    public void setSpeed(double newSpeed) {
        if (newSpeed > MAX_SPEED) newSpeed = MAX_SPEED;
        if (newSpeed < -MAX_SPEED) newSpeed = -MAX_SPEED;
        this.speedMPT = newSpeed / 20;
        dataWatcher.updateObject(DATA_WATCHER_SPEED, (float) speedMPT);
    }

    public double inclination() {
        return lookVec.yCoord;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        mainComponent.tick();
        Vec3 pos = getPathPosition();
        setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
        lookVec = mainComponent.getTrackDirection();

        if (getEntityWorld().isRemote) {// Client
            speedMPT = dataWatcher.getWatchableObjectFloat(DATA_WATCHER_SPEED);
        } else { // Server
            connectorFront.applyMomentum(-inclination() * weight() / 20.0, Face.FRONT);
            // connectorFront.slowAll(mainComponent.resistance() / 20.0); // broken!

            dataWatcher.updateObject(DATA_WATCHER_SPEED, (float) speedMPT);

            List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.2, 0.0D, 0.2));

            if (list != null && !list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity != this.riddenByEntity && entity.canBePushed()) {
                        if (entity instanceof IRollingStock) {
                            // applyRollingStockCollision((EntityRollingStockBase) entity);
                        } else {
                            applyEntityCollision(entity);
                        }
                    }
                }
            }

            if (riddenByEntity != null && riddenByEntity.isDead) {
                riddenByEntity = null;
            }
        }
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(DATA_WATCHER_SPEED, (float) speedMPT);
        // Each track following component uses these flags
        for (int i = 0; i < 4; i++) {
            int start = ComponentTrackFollower.DATA_WATCHER_COMPONENT_START + ComponentTrackFollower.DATA_WATCHER_COMPONENT_STRIDE * i;
            for (int j = 0; j < DATA_WATCHER_COMPONENT_VARS.length; j++) {
                dataWatcher.addObject(start + j, DATA_WATCHER_COMPONENT_VARS[j]);
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        String s = type.toString();
        packet.writeInt(s.length());
        packet.writeString(s);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        int length = packet.readInt();
        type = new ResourceLocation(packet.readStringFromBuffer(length));
        IRollingStockType factory = TrainRegistry.INSTANCE.getFactory(type);
        if (factory == null) throw new IllegalStateException("");
        ConstructedData data = factory.createInstance(this);
        mainComponent = data.outer;
        connectorFront = data.front;
        connectorBack = data.back;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {}

    public boolean alignFromPlayer(Vec3 lookDir, Vec3 lookPoint, boolean simulate) throws AlignmentFailureException {
        MovingObjectPosition pos = worldObj.rayTraceBlocks(lookPoint, lookPoint.add(MathUtil.scale(lookDir, 4)), false);
        if (pos == null) throw new AlignmentFailureException();
        List<BehaviourWrapper> paths = new ArrayList<>();
        BlockPos center = new BlockPos(pos.hitVec);
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos2 = center.add(x, y, z);
                    IBlockState state = worldObj.getBlockState(pos2);
                    List<TrackBehaviour> tracksAsList = TrainCraftAPI.TRACK_PROVIDER.getTracksAsList(worldObj, pos2, state);
                    paths.addAll(TrainCraftAPI.TRACK_PROVIDER.wrapList(tracksAsList, worldObj, pos2));
                }
            }
        }
        RayTraceTrackPath best = null;
        BehaviourWrapper bestWrapper = null;
        for (BehaviourWrapper p : paths) {
            // FIXME: Change RayTraceTrackPath to also have the behaviour wrapper!
            RayTraceTrackPath rayTrace = p.getPath().rayTrace(lookPoint, lookDir);
            if (best == null || best.distance > rayTrace.distance) {
                best = rayTrace;
                bestWrapper = p;
            }
        }
        if (best == null) throw new AlignmentFailureException();
        // if (best.distance > 0.4) throw new AlignmentFailureException();
        return alignToPath(bestWrapper, best.interp, simulate);
    }

    public boolean alignToPath(BehaviourWrapper behaviour, double interp, boolean simulate) throws AlignmentFailureException {
        pathFinder = new StockPathFinder(this, behaviour);
        mainComponent.alignTo(behaviour, behaviour.getPath().length() * interp, simulate);
        Vec3 vec = getPathPosition();
        setPosition(vec.xCoord, vec.yCoord, vec.zCoord);

        boolean connected = connectorFront.attemptJoinAround(simulate);
        connected |= connectorBack.attemptJoinAround(simulate);
        return connected;
    }

    @Override
    public Connector getConnector(Face direction) {
        return direction == Face.FRONT ? connectorFront : connectorBack;
    }

    @Override
    public int weight() {
        return mainComponent.weight();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (mainComponent.hasCapability(capability, facing)) return mainComponent.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (mainComponent.hasCapability(capability, facing)) return true;
        return super.hasCapability(capability, facing);
    }
}
