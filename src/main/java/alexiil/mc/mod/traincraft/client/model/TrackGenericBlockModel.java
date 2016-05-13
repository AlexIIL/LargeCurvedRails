package alexiil.mc.mod.traincraft.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.lib.BlockStateKeyWrapper;

// TODO: Convert this to use ITrackModel and be final rather than abstract
public abstract class TrackGenericBlockModel extends PerspAwareModelBase {
    private final LoadingCache<BlockStateKeyWrapper, List<BakedQuad>> modelCache = CacheBuilder.newBuilder()//
            .maximumSize(maxCacheSize() + 10)//
            .build(CacheLoader.from(this::generateModel));

    public TrackGenericBlockModel() {
        super(null, null, null);
    }

    public long maxCacheSize() {
        // 16 is the maximum number of meta values so it should be enough
        return 16;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return ImmutableList.of();
        return getOrCreate(state);
    }

    private List<BakedQuad> generateModel(BlockStateKeyWrapper wrapper) {
        IBlockState state = wrapper.state;
        List<BakedQuad> quads = new ArrayList<>();

        ITrackPath path = path(state);

        if (path == null) {
            IBakedModel missing = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(null);
            return missing.getQuads(null, null, 0);
        }

        quads.addAll(generateSleepers(state, path));
        quads.addAll(generateRails(state, path));
        generateExtra(quads, state, path);
        return quads;
    }

    @SuppressWarnings("static-method")
    protected List<BakedQuad> generateSleepers(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers(), false);
    }

    @SuppressWarnings("static-method")
    protected List<BakedQuad> generateRails(IBlockState state, ITrackPath path) {
        return CommonModelSpriteCache.generateRails(path, CommonModelSpriteCache.INSTANCE.spriteVanillaRails(false));
    }

    public List<BakedQuad> getOrCreate(IBlockState state) {
        BlockStateKeyWrapper wrapper = new BlockStateKeyWrapper(state);
        return modelCache.getUnchecked(wrapper);
    }

    public abstract ITrackPath path(IBlockState state);

    public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {}
}
