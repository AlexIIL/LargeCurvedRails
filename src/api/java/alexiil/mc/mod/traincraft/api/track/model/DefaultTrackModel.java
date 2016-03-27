package alexiil.mc.mod.traincraft.api.track.model;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import alexiil.mc.mod.traincraft.api.TrainCraftAPI;

public final class DefaultTrackModel implements ITrackModel {
    public static final DefaultTrackModel INSTANCE = new DefaultTrackModel();
    private static final List<RailGeneneratorParams> RAIL_GEN;

    static {
        ImmutableList.Builder<RailGeneneratorParams> builder = ImmutableList.builder();
        builder.add(new RailGeneneratorParams(null));
        RAIL_GEN = builder.build();
    }

    public static void textureStitchPost() {
        for (RailGeneneratorParams gen : RAIL_GEN) {
            gen.railSprite(TrainCraftAPI.SPRITE_GETTER.spriteVanillaRails(false));
        }
    }

    private DefaultTrackModel() {}

    @Override
    public List<RailGeneneratorParams> getRailGen() {
        return RAIL_GEN;
    }

    @Override
    public List<ISleeperGen> getSleeperGen() {
        return Collections.emptyList();
    }
}
