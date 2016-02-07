package alexiil.mods.traincraft.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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

    private static final Map<ResourceLocation, Integer> stockModelMap = new HashMap<>();
    private static final Map<ResourceLocation, IBakedModel> stockModelBakedMap = new HashMap<>();

    protected RenderRollingStockBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRollingStockBase entity) {
        return null;
    }

    public static void clearModelMap() {
        stockModelMap.values().forEach(v -> GLAllocation.deleteDisplayLists(v));
        stockModelMap.clear();
        stockModelBakedMap.clear();
    }

    public static IBakedModel bakeModel(ResourceLocation location) {
        if (!stockModelBakedMap.containsKey(location)) {
            IModel model;
            try {
                model = OBJLoader.instance.loadModel(location);
            } catch (IOException e) {
                e.printStackTrace();
                model = ModelLoaderRegistry.getMissingModel();
            }
            IBakedModel baked = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, TrainRegistry.INSTANCE.getSpriteFunction());
            stockModelBakedMap.put(location, baked);
        }
        return stockModelBakedMap.get(location);
    }

    public static Integer glModel(ResourceLocation location) {
        if (!stockModelMap.containsKey(location)) {
            IBakedModel baked = bakeModel(location);

            int glList = GL11.glGenLists(1);
            GL11.glNewList(glList, GL11.GL_COMPILE);

            Matrix4f identity = new Matrix4f();
            identity.setIdentity();

            SmoothFaceRenderer.renderModel(baked, identity);

            // BlockModelRenderer renderer =
            // Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
            // renderer.renderModelBrightnessColor(baked, 1, 1, 1, 1);

            GL11.glEndList();
            stockModelMap.put(location, glList);
        }
        return stockModelMap.get(location);
    }

    public static void renderModel(ResourceLocation location) {
        SmoothFaceRenderer.renderDisplayList(glModel(location));
    }

    public static void renderModelAnimated(ResourceLocation location, Matrix4f transform) {
        SmoothFaceRenderer.renderModel(bakeModel(location), transform);
    }

    @Override
    public void doRender(EntityRollingStockBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();

        /* If we are on a track the ignore the actual position of the entity and use whatever rail we are currently
         * on */
        Vec3 interpPlayerPos = renderManager.livingPlayer.getPositionEyes(partialTicks);
        interpPlayerPos = interpPlayerPos.subtract(0, renderManager.livingPlayer.getEyeHeight(), 0);

        /* Bit hacky, but this basically ignores the position of this entity so we can ignore it entierly and just use
         * our own positions */
        x = -interpPlayerPos.xCoord;
        y = -interpPlayerPos.yCoord;
        z = -interpPlayerPos.zCoord;

        GlStateManager.translate(x, y, z);

        GlStateManager.color(1, 1, 1);

        RenderHelper.disableStandardItemLighting();

        renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        entity.mainComponent.render(entity, partialTicks);

        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }

}