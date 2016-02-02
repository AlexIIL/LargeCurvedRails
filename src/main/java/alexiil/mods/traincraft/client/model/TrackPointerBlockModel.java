package alexiil.mods.traincraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;

import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.TRSRTransformation;

public class TrackPointerBlockModel extends PerspAwareModelBase implements ISmartBlockModel {
    public static final TrackPointerBlockModel INSTANCE = new TrackPointerBlockModel();

    public TrackPointerBlockModel(VertexFormat format, ImmutableList<BakedQuad> quads, TextureAtlasSprite particle,
            ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(format, quads, particle, transforms);
    }

    private TrackPointerBlockModel() {
        super(DefaultVertexFormats.BLOCK, null, null, null);
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        return new PerspAwareModelBase(DefaultVertexFormats.BLOCK, null, null, null);
    }
}
