package alexiil.mc.mod.traincraft.api.track.model;

import java.util.Arrays;

import net.minecraftforge.common.property.IUnlistedProperty;

public class TrackModelProperty implements IUnlistedProperty<TrackModelWrapper[]> {
    public static final TrackModelProperty INSTANCE = new TrackModelProperty();

    private TrackModelProperty() {}

    @Override
    public String getName() {
        return "track_model";
    }

    @Override
    public boolean isValid(TrackModelWrapper[] value) {
        if (value == null) return false;
        for (TrackModelWrapper wrapper : value) {
            if (wrapper == null) return false;
        }
        return true;
    }

    @Override
    public Class<TrackModelWrapper[]> getType() {
        return TrackModelWrapper[].class;
    }

    @Override
    public String valueToString(TrackModelWrapper[] value) {
        return Arrays.toString(value);
    }
}
