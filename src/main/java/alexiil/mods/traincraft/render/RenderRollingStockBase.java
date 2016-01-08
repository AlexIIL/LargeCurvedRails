package alexiil.mods.traincraft.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import alexiil.mods.traincraft.TrainRegistry;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public class RenderRollingStockBase extends Render<EntityRollingStockBase> {
    public enum Factory implements IRenderFactory<EntityRollingStockBase> {
        INSTANCE;

        @Override
        public Render<? super EntityRollingStockBase> createRenderFor(RenderManager manager) {
            return new RenderRollingStockBase(manager);
        }
    }

    private static final Map<ResourceLocation, IBakedModel> stockModelMap = new HashMap<>();

    protected RenderRollingStockBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRollingStockBase entity) {
        return null;
    }

    public static void clearModelMap() {
        stockModelMap.clear();
    }

    private static IBakedModel getModel(EntityRollingStockBase entity) {
        ResourceLocation location = entity.getModelLocation();
        if (!stockModelMap.containsKey(location)) {
            IModel model;
            try {
                model = OBJLoader.instance.loadModel(location);
            } catch (IOException e) {
                e.printStackTrace();
                model = ModelLoaderRegistry.getMissingModel();
            }
            IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, TrainRegistry.INSTANCE.getSpriteFunction());
            stockModelMap.put(location, baked);
        }
        return stockModelMap.get(location);
    }

    @Override
    public void doRender(EntityRollingStockBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();

        /* If we are on a track the ignore the actual position of the entity and use whatever rail we are currently
         * on */
        if (entity.isOnTrack()) {
            Vec3 interpPlayerPos = renderManager.livingPlayer.getPositionEyes(partialTicks);
            interpPlayerPos = interpPlayerPos.subtract(0, renderManager.livingPlayer.getEyeHeight(), 0);

            /* Bit hacky, but this basically ignores the position of this entity so we can ignore it entierly and just
             * use our own positions */
            x = -interpPlayerPos.xCoord;
            y = -interpPlayerPos.yCoord;
            z = -interpPlayerPos.zCoord;
            GlStateManager.translate(x, y, z);

            Vec3 actualPos = entity.getInterpolatedPosition(partialTicks);
            GlStateManager.translate(actualPos.xCoord, actualPos.yCoord, actualPos.zCoord);
        } else {
            GlStateManager.translate(x, y, z);
        }

        Vec3 lookVec = entity.getInterpolatedDirection(partialTicks);

        double tan = Math.atan2(lookVec.xCoord, lookVec.zCoord);
        // The tan is in radians but OpenGL uses degrees
        GL11.glRotated(tan * (360 / Math.PI / 2), 0, 1, 0);

        IBakedModel model = getModel(entity);
        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        renderer.renderModelBrightnessColor(model, entity.getBrightness(partialTicks), 1, 1, 1);

        GlStateManager.popMatrix();
    }

}
