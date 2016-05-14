package alexiil.mc.mod.traincraft.client.model;

import com.google.common.collect.ImmutableList;

public class TrackPointerBlockModel extends PerspAwareModelBase {
    public static final TrackPointerBlockModel INSTANCE = new TrackPointerBlockModel();

    public TrackPointerBlockModel() {
        super(ImmutableList.of(), null, null);
    }

}
