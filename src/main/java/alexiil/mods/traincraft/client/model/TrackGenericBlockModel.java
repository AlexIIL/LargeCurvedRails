package alexiil.mods.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.client.model.ISmartBlockModel;

import alexiil.mods.traincraft.api.track.ITrackPath;
import alexiil.mods.traincraft.lib.BlockStateKeyWrapper;

public abstract class TrackGenericBlockModel extends PerspAwareModelBase implements ISmartBlockModel {
    private final LoadingCache<BlockStateKeyWrapper, IBakedModel> modelCache = CacheBuilder.newBuilder().maximumSize(maxCacheSize() + 10).build(
            CacheLoader.from(this::generateModel));

    public TrackGenericBlockModel() {
        super(null, null, null, null);
    }

    public long maxCacheSize() {
        // 16 is the maximum number of meta values so it should be enough
        return 16;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        return getOrCreate(state);
    }

    private IBakedModel generateModel(BlockStateKeyWrapper wrapper) {
        IBlockState state = wrapper.state;
        List<BakedQuad> quads = new ArrayList<>();

        ITrackPath path = path(state);

        if (path == null) return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();

        quads.addAll(generateSleepers(state, path));
        quads.addAll(generateRails(state, path));
        generateExtra(quads, state, path);

        return ModelUtil.wrapInBakedModel(quads, CommonModelSpriteCache.INSTANCE.spriteVanillaRail(false));
    }

    protected List<BakedQuad> generateSleepers(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers(), false);
    }

    protected List<BakedQuad> generateRails(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateRails(path, CommonModelSpriteCache.INSTANCE.spriteVanillaRail(false));
    }

    public IBakedModel getOrCreate(IBlockState state) {
        BlockStateKeyWrapper wrapper = new BlockStateKeyWrapper(state);
        return modelCache.getUnchecked(wrapper);
    }

    public abstract ITrackPath path(IBlockState state);

    public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {}
}
