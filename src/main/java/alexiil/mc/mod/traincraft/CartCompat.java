package alexiil.mc.mod.traincraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.RayTraceTrackPath;

public enum CartCompat {
    INSTANCE;

    private final Set<Class<? extends EntityMinecart>> blacklistClasses = new HashSet<>();

    private boolean canUpdateCart(EntityMinecart minecart) {
        if (minecart == null) return false;
        return !blacklistClasses.contains(minecart.getClass());
    }

    public void registerBlackListClass(Class<? extends EntityMinecart> clazz) {
        blacklistClasses.add(clazz);
    }

    @SubscribeEvent
    public void onEntityTick(MinecartUpdateEvent event) {
        EntityMinecart minecart = event.getMinecart();
        if (!canUpdateCart(minecart)) return;
        Vec3d cartPoint = new Vec3d(minecart.posX, minecart.posY, minecart.posZ);

        List<BehaviourWrapper[]> wrappers = new ArrayList<>();
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(cartPoint).add(-2, -2, -2), new BlockPos(cartPoint).add(2, 2, 2))) {
            IBlockState state = minecart.world.getBlockState(pos);
            wrappers.add(TrackPathProvider.INSTANCE.getTracksAsArray(minecart.world, pos, state));
        }
        double angDiff = Integer.MAX_VALUE;
        double posDiff = 2;
        BehaviourWrapper best = null;
        RayTraceTrackPath bestTrace = null;
        boolean reverse = false;
        for (BehaviourWrapper[] wrapperArray : wrappers) {
            for (BehaviourWrapper w : wrapperArray) {
                RayTraceTrackPath rayTrace = w.getPath().rayTrace(cartPoint.addVector(0, -0.5, 0), new Vec3d(0, 1, 0));
                if (rayTrace == null) continue;
                if (rayTrace.distance > posDiff) continue;
                boolean rev = false;

                Vec3d cartAngle = new Vec3d(minecart.motionX, minecart.motionY, minecart.motionZ).normalize();
                Vec3d pathAngle = w.getPath().direction(rayTrace.interp).normalize();
                if (cartAngle.distanceTo(pathAngle) > cartAngle.distanceTo(pathAngle.subtractReverse(new Vec3d(0, 0, 0)))) {
                    pathAngle = pathAngle.subtractReverse(new Vec3d(0, 0, 0));
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
            Vec3d pathDir = best.getPath().direction(bestTrace.interp).normalize();
            if (reverse) pathDir = pathDir.subtractReverse(new Vec3d(0, 0, 0));
            Vec3d cartAngle = new Vec3d(minecart.motionX, minecart.motionY, minecart.motionZ);
            double speed = cartAngle.lengthVector();
            speed = Math.min(speed, 0.2);
            minecart.motionX = pathDir.x * speed;
            minecart.motionY = pathDir.y * speed;
            minecart.motionZ = pathDir.z * speed;

            Vec3d pathPos = bestTrace.closestPoint;
            minecart.posX = pathPos.x + minecart.motionX;
            minecart.posY = pathPos.y + minecart.motionY;
            minecart.posZ = pathPos.z + minecart.motionZ;
        }
    }
}
