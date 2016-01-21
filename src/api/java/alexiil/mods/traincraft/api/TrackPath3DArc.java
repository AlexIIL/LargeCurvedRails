package alexiil.mods.traincraft.api;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class TrackPath3DArc implements ITrackPath {
    public TrackPath3DArc() {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public Vec3 interpolate(double position) {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public Vec3 direction(double position) {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public double length() {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public ITrackPath offset(BlockPos pos) {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public BlockPos creatingBlock() {
        throw new AbstractMethodError("Not yet implemented");
    }
}
