package alexiil.mods.traincraft.api;

import java.util.Objects;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TrackPathComposite<A extends ITrackPath, B extends ITrackPath, C extends ITrackPath> implements ITrackPath {
    private final BlockPos center;
    private final A pathA;
    private final B pathB;
    private final C pathC;
    private final double totalLength;

    public TrackPathComposite(BlockPos center, A pathA, B pathB, C pathC) {
        this.center = center;
        this.pathA = pathA;
        this.pathB = pathB;
        this.pathC = pathC;
        this.totalLength = pathA.length() + pathB.length() + pathC.length();
    }

    @Override
    public Vec3 interpolate(double position) {
        final double posLength = position * totalLength;
        if (posLength <= pathA.length()) {
            double aInterp = posLength / pathA.length();
            return pathA.interpolate(aInterp);
        } else if (posLength <= pathA.length() + pathB.length()) {
            double bInterp = (posLength - pathA.length()) / pathB.length();
            return pathB.interpolate(bInterp);
        } else {
            double cInterp = (posLength - pathA.length() - pathB.length()) / pathC.length();
            return pathC.interpolate(cInterp);
        }
    }

    @Override
    public Vec3 direction(double position) {
        final double posLength = position * totalLength;
        if (posLength <= pathA.length()) {
            double aInterp = posLength / pathA.length();
            return pathA.direction(aInterp);
        } else if (posLength <= pathA.length() + pathB.length()) {
            double bInterp = (posLength - pathA.length()) / pathB.length();
            return pathB.direction(bInterp);
        } else {
            double cInterp = (posLength - pathA.length() - pathB.length()) / pathC.length();
            return pathC.direction(cInterp);
        }
    }

    @Override
    public double length() {
        return totalLength;
    }

    @Override
    public TrackPathComposite<A, B, C> offset(BlockPos pos) {
        return new TrackPathComposite<A, B, C>(pos.add(center), (A) pathA.offset(pos), (B) pathB.offset(pos), (C) pathC.offset(pos));
    }

    @Override
    public BlockPos creatingBlock() {
        return center;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInfo(WorldRenderer wr) {
        pathA.renderInfo(wr);
        pathB.renderInfo(wr);
        pathC.renderInfo(wr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathA, pathB);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        TrackPathComposite<?, ?, ?> comp = (TrackPathComposite) obj;
        return pathA.equals(comp.pathA) && pathB.equals(comp.pathB) && pathC.equals(comp.pathC);
    }
}
