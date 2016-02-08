package alexiil.mods.traincraft.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TrackPathCompositeBase implements ITrackPath {
    private final ITrackPath[] paths;
    private final BlockPos creatingBlock;
    private final double length;

    public TrackPathCompositeBase(BlockPos creator, ITrackPath... paths) {
        List<ITrackPath> total = new ArrayList<>();
        double totalLength = 0;
        for (ITrackPath p : paths) {
            if (p.length() > 0) {
                total.add(p);
                totalLength += p.length();
            }
        }
        if (total.size() == 0 || totalLength <= 0) throw new IllegalArgumentException("No actual paths!");
        this.paths = total.toArray(new ITrackPath[total.size()]);
        this.creatingBlock = creator;
        this.length = totalLength;
    }

    @Override
    public Vec3 interpolate(double position) {
        double interp = position * length;
        for (ITrackPath p : paths) {
            if (interp <= p.length()) {
                return p.interpolate(interp / p.length());
            }
            interp -= p.length();
        }
        return paths[paths.length - 1].interpolate(1);
    }

    @Override
    public Vec3 direction(double position) {
        double interp = position * length;
        for (ITrackPath p : paths) {
            if (interp <= p.length()) {
                return p.direction(interp / p.length());
            }
            interp -= p.length();
        }
        return paths[paths.length - 1].direction(1);
    }

    @Override
    public double length() {
        return length;
    }

    @Override
    public BlockPos creatingBlock() {
        return creatingBlock;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInfo(WorldRenderer wr) {
        for (ITrackPath p : paths) {
            p.renderInfo(wr);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(paths);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        TrackPathCompositeBase comp = (TrackPathCompositeBase) obj;
        return Arrays.equals(paths, comp.paths);
    }
}
