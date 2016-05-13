package alexiil.mc.mod.traincraft.api.track.path;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TrackPath3DArc implements ITrackPath {
    public TrackPath3DArc() {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public Vec3d interpolate(double position) {
        throw new AbstractMethodError("Not yet implemented");
    }

    @Override
    public Vec3d direction(double position) {
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
