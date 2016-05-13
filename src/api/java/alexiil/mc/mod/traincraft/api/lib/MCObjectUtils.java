package alexiil.mc.mod.traincraft.api.lib;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MCObjectUtils {
    public static int hash(Vec3d vec) {
        if (vec == null) return 0;
        return Objects.hash(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static int hash(Vec3d[] array) {
        if (array == null) return 0;

        int result = 1;

        for (Vec3d element : array)
            result = 31 * result + (element == null ? 0 : hash(element));

        return result;
    }

    public static int hash(Object... array) {
        if (array == null) return 0;

        int result = 1;

        for (Object element : array) {
            int hash;
            if (element instanceof Vec3d) hash = hash((Vec3d) element);
            else hash = Objects.hashCode(element);
            result = 31 * result + (element == null ? 0 : hash);
        }
        return result;
    }

    public static boolean equals(Vec3d a, Vec3d b) {
        return MathUtil.roughlyEquals(a.xCoord, b.xCoord) && MathUtil.roughlyEquals(a.yCoord, b.yCoord) && MathUtil.roughlyEquals(a.zCoord, b.zCoord);
    }

    public static BlockPos getWatchableObjectBlockPos(DataWatcher watcher, int id) {
        for (WatchableObject watchable : watcher.getAllWatched()) {
            if (watchable.getDataValueId() == id) {
                return (BlockPos) watchable.getObject();
            }
        }
        throw new IllegalStateException("Did not find a block position!");
    }

    public static final class Vec3dKey {
        private final Vec3d key;

        public Vec3dKey(Vec3d key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            return hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null) return false;
            if (obj instanceof Vec3dKey) return MCObjectUtils.equals(key, ((Vec3dKey) obj).key);
            return false;
        }
    }
}
