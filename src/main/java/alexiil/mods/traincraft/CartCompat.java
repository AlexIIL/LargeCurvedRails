package alexiil.mods.traincraft;

import java.util.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.path.RayTraceTrackPath;
import alexiil.mods.traincraft.api.train.IMinecartCompat;

public enum CartCompat {
    INSTANCE;

    private final Set<Class<? extends EntityMinecart>> blacklistClasses = new HashSet<>();
    private final Map<Class<? extends EntityMinecart>, IMinecartCompat.IMinecartExternalCompat<?>> extenalHandlers = new HashMap<>();

    private boolean canUpdateCart(EntityMinecart minecart) {
        if (minecart == null) return false;
        if (minecart instanceof IMinecartCompat) {
            return ((IMinecartCompat) minecart).canUpdateManually();
        }
        return !blacklistClasses.contains(minecart.getClass());
    }

    public void registerBlackListClass(Class<? extends EntityMinecart> clazz) {
        blacklistClasses.add(clazz);
    }

    @SubscribeEvent
    public void onEntityTick(MinecartUpdateEvent event) {
        EntityMinecart minecart = event.minecart;
        if (!canUpdateCart(minecart)) return;
        Vec3 cartPoint = new Vec3(minecart.posX, minecart.posY, minecart.posZ);

        List<BehaviourWrapper[]> wrappers = new ArrayList<>();
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(cartPoint).add(-2, -2, -2), new BlockPos(cartPoint).add(2, 2, 2))) {
            IBlockState state = minecart.worldObj.getBlockState(pos);
            wrappers.add(TrackPathProvider.INSTANCE.getTracksAsArray(minecart.worldObj, pos, state));
        }
        double angDiff = Integer.MAX_VALUE;
        double posDiff = 2;
        BehaviourWrapper best = null;
        RayTraceTrackPath bestTrace = null;
        boolean reverse = false;
        for (BehaviourWrapper[] wrapperArray : wrappers) {
            for (BehaviourWrapper w : wrapperArray) {
                RayTraceTrackPath rayTrace = w.getPath().rayTrace(cartPoint.addVector(0, -0.5, 0), new Vec3(0, 1, 0));
                if (rayTrace == null) continue;
                if (rayTrace.distance > posDiff) continue;
                boolean rev = false;

                Vec3 cartAngle = new Vec3(minecart.motionX, minecart.motionY, minecart.motionZ).normalize();
                Vec3 pathAngle = w.getPath().direction(rayTrace.interp).normalize();
                if (cartAngle.distanceTo(pathAngle) > cartAngle.distanceTo(pathAngle.subtractReverse(new Vec3(0, 0, 0)))) {
                    pathAngle = pathAngle.subtractReverse(new Vec3(0, 0, 0));
                    rev = true;
                }

                double diff = cartAngle.distanceTo(pathAngle);
                if (diff > angDiff) continue;

                best = w;
                posDiff = rayTrace.distance;
                angDiff = rayTrace.distance;
                bestTrace = rayTrace;
                reverse = rev;
            }
        }
        if (best != null && bestTrace != null) {
            Vec3 pathDir = best.getPath().direction(bestTrace.interp).normalize();
            if (reverse) pathDir = pathDir.subtractReverse(new Vec3(0, 0, 0));
            Vec3 cartAngle = new Vec3(minecart.motionX, minecart.motionY, minecart.motionZ);
            double speed = cartAngle.lengthVector();
            speed = Math.min(speed, 0.2);
            minecart.motionX = pathDir.xCoord * speed;
            minecart.motionY = pathDir.yCoord * speed;
            minecart.motionZ = pathDir.zCoord * speed;

            Vec3 pathPos = bestTrace.closestPoint;
            minecart.posX = pathPos.xCoord + minecart.motionX;
            minecart.posY = pathPos.yCoord + minecart.motionY;
            minecart.posZ = pathPos.zCoord + minecart.motionZ;
        }
    }
}
