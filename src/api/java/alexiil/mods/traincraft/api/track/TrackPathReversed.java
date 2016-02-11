package alexiil.mods.traincraft.api.track;

import java.util.Objects;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class TrackPathReversed implements ITrackPath {
    private static final Vec3 ORIGIN = new Vec3(0, 0, 0);

    final ITrackPath original;

    public TrackPathReversed(ITrackPath original) {
        this.original = original;
    }

    @Override
    public Vec3 interpolate(double position) {
        return original.interpolate(1 - position);
    }

    @Override
    public double length() {
        return original.length();
    }

    @Override
    public ITrackPath reverse() {
        return original;
    }

    @Override
    public Vec3 direction(double position) {
        return ORIGIN.subtract(original.direction(1 - position));
    }

    @Override
    public ITrackPath offset(BlockPos by) {
        return new TrackPathReversed(original.offset(by));
    }

    @Override
    public BlockPos creatingBlock() {
        return original.creatingBlock();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(original);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TrackPathReversed other = (TrackPathReversed) obj;
        return Objects.equals(original, other.original);
    }

    @Override
    public double progress(Vec3 lastPlace) {
        return 1 - original.progress(lastPlace);
    }

    @Override
    public String toString() {
        return "TrackPathReversed[" + original + "]";
    }
}
