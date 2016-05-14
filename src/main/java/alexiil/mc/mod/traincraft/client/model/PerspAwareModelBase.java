package alexiil.mc.mod.traincraft.client.model;

import java.util.List;

import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;

public class PerspAwareModelBase implements IPerspectiveAwareModel {
    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<TransformType, TRSRTransformation> transforms;

    public PerspAwareModelBase(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<TransformType, TRSRTransformation> transforms) {
        this.quads = quads == null ? ImmutableList.<BakedQuad> of() : quads;
        this.particle = particle;
        if (transforms == null) {
            this.transforms = ImmutableMap.of();
        } else this.transforms = transforms;
    }

    public static List<BakedQuad> missingModel() {
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
        return model.getQuads(Blocks.AIR.getDefaultState(), null, 0);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return side == null ? quads : ImmutableList.of();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
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
        return particle;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }
}
