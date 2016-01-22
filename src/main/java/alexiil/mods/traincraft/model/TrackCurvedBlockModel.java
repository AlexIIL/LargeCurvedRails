package alexiil.mods.traincraft.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.ISmartBlockModel;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackCurved;

public class TrackCurvedBlockModel implements ISmartBlockModel {
    private Map<IBlockState, IBakedModel> cache = new HashMap<>();
    private final BlockTrackCurved curved;

    public TrackCurvedBlockModel(BlockTrackCurved curved) {
        this.curved = curved;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        if (!cache.containsKey(state)) {
            EnumFacing facing = state.getValue(BlockTrackCurved.PROPERTY_FACING);
            boolean positive = state.getValue(BlockTrackCurved.PROPERTY_DIRECTION);
            ITrackPath path = curved.path(positive, facing);
            IBakedModel baked = CurvedModelGenerator.INSTANCE.generateModelFor(path, !positive);
            cache.put(state, baked);
        }
        return cache.get(state);
    }

    @Override
    public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }
}
