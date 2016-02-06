package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.client.model.ISmartBlockModel;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.lib.BlockStateKeyWrapper;

public abstract class TrackGenericBlockModel extends PerspAwareModelBase implements ISmartBlockModel {
    private final Cache<BlockStateKeyWrapper, IBakedModel> modelCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize()).build();

    public TrackGenericBlockModel() {
        super(null, null, null, null);
    }

    public long maxCacheSize() {
        // 16 is the maximum number of meta values so it should be enough
        return 16;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        IBakedModel model = getFromCache(state);
        if (model != null) return model;
        model = generateModel(state);
        storeInCache(state, model);
        return model;
    }

    private IBakedModel generateModel(IBlockState state) {
        List<BakedQuad> quads = new ArrayList<>();

        ITrackPath path = path(state);

        quads.addAll(generateSleepers(state, path));
        quads.addAll(generateRails(state, path));
        generateExtra(quads, state, path);

        return ModelUtil.wrapInBakedModel(quads, CommonModelSpriteCache.INSTANCE.railSprite(false));
    }

    protected List<BakedQuad> generateSleepers(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers());
    }

    protected List<BakedQuad> generateRails(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateRails(path, CommonModelSpriteCache.INSTANCE.railSprite(false));
    }

    public IBakedModel getFromCache(IBlockState state) {
        BlockStateKeyWrapper wrapper = new BlockStateKeyWrapper(state);
        return modelCache.getIfPresent(wrapper);
    }

    public void storeInCache(IBlockState state, IBakedModel model) {
        modelCache.put(new BlockStateKeyWrapper(state), model);
    }

    public abstract ITrackPath path(IBlockState state);

    public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {}
}
