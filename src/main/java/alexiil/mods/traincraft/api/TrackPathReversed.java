package alexiil.mods.traincraft.api;

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
        return ORIGIN.subtract(original.direction(position));
    }

    @Override
    public ITrackPath offset(Vec3 by) {
        return new TrackPathReversed(original.offset(by));
    }
}
