package alexiil.mods.traincraft.api.track.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.properties.IProperty;

import net.minecraftforge.common.property.IUnlistedProperty;

public class TrackModelProperty implements IUnlistedProperty<TrackModelWrapper[]>, IProperty {
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

    @Override
    public Collection getAllowedValues() {
        return Collections.emptyList();
    }

    @Override
    public Class getValueClass() {
        return Void.class;
    }

    @Override
    public String getName(Comparable value) {
        return "";
    }
}
