package alexiil.mc.mod.traincraft.api.track.model;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

public final class TrackModelWrapper {
    public final ITrackPath path;
    public final ITrackModel model;

    public TrackModelWrapper(ITrackPath path, ITrackModel model) {
        if (path.creatingBlock().equals(BlockPos.ORIGIN)) {
            this.path = path;
        } else {
            this.path = path.offset(BlockPos.ORIGIN.subtract(path.creatingBlock()));
        }
        this.model = model;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, model);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        TrackModelWrapper other = (TrackModelWrapper) obj;
        if (!other.path.equals(path)) return false;
        if (!other.model.equals(model)) return false;
        return true;
    }
}
