package alexiil.mods.traincraft.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

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
    private static Map<EnumFacing, Matrix4f> rotationMatricies = new HashMap<>();
    private Map<IBlockState, IBakedModel> cache = new HashMap<>();
    private final BlockTrackCurved curved;

    static {
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            Matrix4f translate = new Matrix4f();
            translate.setIdentity();
            translate.setTranslation(new Vector3f(0.5f, 0, 0.5f));

            final float angle;

            // @formatter:off
                 if (face == EnumFacing.NORTH) angle =   0f * (float) Math.PI / 180f;
            else if (face == EnumFacing.EAST ) angle =  90f * (float) Math.PI / 180f;
            else if (face == EnumFacing.SOUTH) angle = 180f * (float) Math.PI / 180f;
            else if (face == EnumFacing.WEST ) angle = 270f * (float) Math.PI / 180f;
            else  angle =   0f;
            // @formatter:on

            Matrix4f rotate = new Matrix4f();
            rotate.setIdentity();
            rotate.setRotation(new AxisAngle4f(0, 1, 0, angle));

            Matrix4f translateBack = new Matrix4f();
            translateBack.setIdentity();
            translateBack.setTranslation(new Vector3f(-0.5f, 0, -0.5f));

            Matrix4f total = new Matrix4f();
            total.setIdentity();
            total.mul(translateBack);
            total.mul(rotate);
            total.mul(translate);
            rotationMatricies.put(face, total);
        }
    }

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
            cache.put(state, ModelUtil.multiplyMatrix(baked, rotationMatricies.get(facing)));
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
