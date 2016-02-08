package alexiil.mods.traincraft.api;

import java.util.Objects;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.lib.MathUtil;

public class MCObjectUtils {
    public static int hash(Vec3 vec) {
        if (vec == null) return 0;
        return Objects.hash(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static int hash(Vec3... array){
    if (array == null) return 0;

    int result = 1;

    for (Vec3 element : array)
        result = 31 * result + (element == null ? 0 : hash(element));

    return result;}

public static int hash(Object... array) {
        if (array == null) return 0;

        int result = 1;

        for (Object element : array) {
            int hash;
            if (element instanceof Vec3) hash = hash((Vec3) element);
            else hash = Objects.hashCode(element);
            result = 31 * result + (element == null ? 0 : hash);
        }
        return result;
    }

    public static boolean equals(Vec3 a, Vec3 b) {
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
}
