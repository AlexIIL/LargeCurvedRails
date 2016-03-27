package alexiil.mc.mod.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;

import alexiil.mc.mod.traincraft.api.track.model.ITrackModel;
import alexiil.mc.mod.traincraft.api.track.model.RailGeneneratorParams;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelProperty;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

public class TrackGenericBlockModel_NEW_ extends PerspAwareModelBase implements ISmartBlockModel {
    public static final TrackGenericBlockModel_NEW_ INSTANCE = new TrackGenericBlockModel_NEW_();

    private TrackGenericBlockModel_NEW_() {
        super(null, null, null, null);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        if (!(state instanceof IExtendedBlockState)) return missingModel();
        IExtendedBlockState extended = (IExtendedBlockState) state;
        TrackModelWrapper[] wrappers = extended.getValue(TrackModelProperty.INSTANCE);
        if (!TrackModelProperty.INSTANCE.isValid(wrappers)) return missingModel();
        return makeModel(wrappers);
    }

    public static IBakedModel makeModel(TrackModelWrapper[] wrappers) {
        // For now we will just bake each model as it comes.
        List<BakedQuad> allQuads = new ArrayList<>();

        for (TrackModelWrapper wrap : wrappers) {
            ITrackPath path = wrap.path;
            ITrackModel model = wrap.model;

            // Add rails
            for (RailGeneneratorParams rail : model.getRailGen()) {
                // TODO: Y-offsets (or perhaps a very inteligent system that stops rails before another one?)
                allQuads.addAll(CommonModelSpriteCache.generateRails(path, rail));
            }
        }
        return ModelUtil.wrapInBakedModel(allQuads, CommonModelSpriteCache.INSTANCE.spriteVanillaRails(false));
    }
}
