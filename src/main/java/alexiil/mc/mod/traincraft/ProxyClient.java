package alexiil.mc.mod.traincraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mc.mod.traincraft.api.AddonManager;
import alexiil.mc.mod.traincraft.api.TrainCraftAPI;
import alexiil.mc.mod.traincraft.api.track.ITrackPlacer.EnumTrackRequirement;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockAbstractTrack;
import alexiil.mc.mod.traincraft.block.BlockTrackAscending;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedFull;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedHalf;
import alexiil.mc.mod.traincraft.block.BlockTrackMultiple;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer;
import alexiil.mc.mod.traincraft.block.BlockTrackStraight;
import alexiil.mc.mod.traincraft.block.TCBlocks;
import alexiil.mc.mod.traincraft.client.model.CommonModelSpriteCache;
import alexiil.mc.mod.traincraft.client.model.MatrixUtil;
import alexiil.mc.mod.traincraft.client.model.TrackAscendingBlockModel;
import alexiil.mc.mod.traincraft.client.model.TrackCurvedFullBlockModel;
import alexiil.mc.mod.traincraft.client.model.TrackCurvedHalfBlockModel;
import alexiil.mc.mod.traincraft.client.model.TrackGenericBlockModel_NEW_;
import alexiil.mc.mod.traincraft.client.model.TrackPointerBlockModel;
import alexiil.mc.mod.traincraft.client.model.TrackStraightBlockModel;
import alexiil.mc.mod.traincraft.client.model.TrackVanillaBlockModel;
import alexiil.mc.mod.traincraft.client.model.VoidStateMapper;
import alexiil.mc.mod.traincraft.client.render.SmoothFaceRenderer;
import alexiil.mc.mod.traincraft.item.ItemBlockSeperatedTrack;

public class ProxyClient extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        TrainCraftAPI.SPRITE_GETTER = CommonModelSpriteCache.INSTANCE;

        // RenderingRegistry.registerEntityRenderingHandler(EntityGenericRollingStock.class,
        // RenderRollingStockBase.Factory.INSTANCE);
        OBJLoader.INSTANCE.addDomain("traincraft");
    }

    @SubscribeEvent
    public void modelBake(ModelBakeEvent bake) {
        // RenderRollingStockBase.clearModelMap();
        CommonModelSpriteCache.INSTANCE.clearModelMap();
        
        BlockModelShapes modelShapes = bake.getModelManager().getBlockModelShapes();

        for (TCBlocks b : TCBlocks.values()) {
            Block block = b.getBlock();
            if (block instanceof BlockAbstractTrack) {
                modelShapes.registerBlockWithStateMapper(b.getBlock(), VoidStateMapper.INSTANCE);
            }
        }

        Block[] vanillaTracks = { Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL };

        for (Block rail : vanillaTracks) {
            modelShapes.registerBlockWithStateMapper(rail, VoidStateMapper.INSTANCE);
        }

        for (TCBlocks b : TCBlocks.values()) {
            ModelResourceLocation mrl = new ModelResourceLocation("traincraft:" + b.name().toLowerCase(Locale.ROOT));

            if (b.getBlock() instanceof BlockTrackCurvedHalf) {
                BlockTrackCurvedHalf curved = (BlockTrackCurvedHalf) b.getBlock();
                bake.getModelRegistry().putObject(mrl, new TrackCurvedHalfBlockModel(curved));
            }

            if (b.getBlock() instanceof BlockTrackPointer) {
                bake.getModelRegistry().putObject(mrl, TrackPointerBlockModel.INSTANCE);
            }

            if (b.getBlock() instanceof BlockTrackAscending) {
                BlockTrackAscending ascending = (BlockTrackAscending) b.getBlock();
                bake.getModelRegistry().putObject(mrl, new TrackAscendingBlockModel(ascending));
            }

            if (b.getBlock() instanceof BlockTrackStraight) {
                bake.getModelRegistry().putObject(mrl, new TrackStraightBlockModel());
            }

            if (b.getBlock() instanceof BlockTrackCurvedFull) {
                BlockTrackCurvedFull curved = (BlockTrackCurvedFull) b.getBlock();
                bake.getModelRegistry().putObject(mrl, new TrackCurvedFullBlockModel(curved));
            }

            if (b.getBlock() instanceof BlockTrackMultiple) {
                bake.getModelRegistry().putObject(mrl, TrackGenericBlockModel_NEW_.INSTANCE);
            }
        }

        for (Block rail : vanillaTracks) {
            ModelResourceLocation mrl = new ModelResourceLocation(Block.REGISTRY.getNameForObject(rail).toString());
            bake.getModelRegistry().putObject(mrl, TrackVanillaBlockModel.create(rail));
        }
        AddonManager.INSTANCE.modelBake(bake);
    }

    @SubscribeEvent
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        // ComponentSmallWheel.textureStitchPre(event);
        // ComponentCart.textureStitchPre(event);
        CommonModelSpriteCache.INSTANCE.textureStitchPre(event);
        AddonManager.INSTANCE.textureStitchPre(event);
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post event) {
        AddonManager.INSTANCE.textureStitchPost(event);
        DefaultTrackModel.textureStitchPost();
    }

    private static final double STEP_DIST = 0.3;

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("traincraft");
        // renderFakeTrain(event);
        renderFakeTrackBlock(event);
        renderDebug(event);
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderFakeTrackBlock(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().getRenderManager().renderViewEntity == null) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.getHeldItemMainhand().isEmpty()) return;
        Item item = player.getHeldItemMainhand().getItem();
        if (!(item instanceof ItemBlockSeperatedTrack<?>)) return;
        ItemBlockSeperatedTrack<?> track = (ItemBlockSeperatedTrack<?>) item;

        if (Minecraft.getMinecraft().objectMouseOver == null || Minecraft.getMinecraft().objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        RayTraceResult mop = Minecraft.getMinecraft().objectMouseOver;

        BlockPos hitPos = mop.getBlockPos();
        EnumFacing side = mop.sideHit;
        Vec3d hitVec = mop.hitVec;

        float hitX = (float) (hitVec.x - hitPos.getX());
        float hitY = (float) (hitVec.y - hitPos.getY());
        float hitZ = (float) (hitVec.z - hitPos.getZ());

        hitPos = hitPos.offset(side);

        ITrackPath preview = track.getPreviewPath(player.world, hitPos, player, player.getHeldItemMainhand(), side, hitX, hitY, hitZ);
        EnumTrackRequirement req = track.canPlaceTrack(player.world, hitPos, player, side, player.getHeldItemMainhand());

        TrackModelWrapper[] wrappers = { new TrackModelWrapper(preview, null) };
        List<BakedQuad> model = TrackGenericBlockModel_NEW_.makeModel(wrappers);

        float[] col = { 1, 1, 1 };

        if (req == EnumTrackRequirement.GROUND_BELOW) col = new float[] { 0, 0, 0.6f };
        else if (req == EnumTrackRequirement.SPACE_ABOVE) col = new float[] { 0, 0, 0 };
        else if (req == EnumTrackRequirement.OTHER) col = new float[] { 0.6f, 0, 0 };

        Vec3d diff = new Vec3d(hitPos);
        diff = diff.subtract(player.getPositionEyes(event.getPartialTicks()));
        diff = diff.addVector(0, player.getEyeHeight(), 0);

        GlStateManager.disableTexture2D();

        SmoothFaceRenderer.renderModelMultColour(model, MatrixUtil.translation(diff), col[0], col[1], col[2]);

        GlStateManager.enableTexture2D();
    }

    private static void renderDebug(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().player == null) return;
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
        BlockPos around = new BlockPos(Minecraft.getMinecraft().player.getPositionVector());
        EntityPlayer player = Minecraft.getMinecraft().player;
        World world = DimensionManager.getWorld(Minecraft.getMinecraft().world.provider.getDimension());
        if (world == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("debug");

        List<ITrackPath> drawn = new ArrayList<>();

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GL11.glPushMatrix();

        GL11.glLineWidth(3);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder bb = tess.getBuffer();
        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;

        bb.begin(GL11.GL_LINES, format);
        Vec3d interp = player.getPositionEyes(event.getPartialTicks());
        interp = interp.addVector(0, -player.getEyeHeight(), 0);
        GL11.glTranslated(-interp.x, -interp.y, -interp.z);
        bb.setTranslation(0, 0, 0);

        final int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos offset = around.add(x, y, z);
                    List<BehaviourWrapper> paths = TrainCraftAPI.TRACK_PROVIDER.getTracksAsList(world, offset, world.getBlockState(offset));
                    for (BehaviourWrapper behaviourWrapper : paths) {
                        if (behaviourWrapper == null) continue;
                        ITrackPath path = behaviourWrapper;
                        // if (drawn.contains(path)) continue;
                        // drawn.add(path);
                        int steps = (int) (path.length() / STEP_DIST);
                        for (int s = 0; s < steps; s++) {
                            double pos = s / (double) steps;
                            Vec3d point = path.interpolate(pos);

                            bb.pos(point.x, point.y, point.z).color(255, 0, 0, 255).endVertex();

                            Vec3d dir = path.direction(pos);
                            dir = new Vec3d(dir.x * STEP_DIST, dir.y * STEP_DIST, dir.z * STEP_DIST);
                            Vec3d point2 = point.add(dir);

                            bb.pos(point2.x, point2.y, point2.z).color(255, 0, 0, 255).endVertex();
                        }

                        BlockPos c = behaviourWrapper.pos();
                        bb.pos(c.getX() + 0.5, c.getY() + 0.3, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();
                        bb.pos(c.getX() + 0.5, c.getY() + 0.7, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();

                        Vec3d s = path.start();
                        bb.pos(s.x, s.y, s.z).color(0, 255, 0, 255).endVertex();
                        bb.pos(s.x, s.y + 0.3, s.z).color(0, 255, 0, 255).endVertex();

                        Vec3d e = path.end();
                        bb.pos(e.x, e.y, e.z).color(0, 255, 0, 255).endVertex();
                        bb.pos(e.x, e.y + 0.3, e.z).color(0, 255, 0, 255).endVertex();

                        path.renderInfo(bb);
                    }
                }
            }
        }
        tess.draw();
        bb.setTranslation(0, 0, 0);
        GL11.glPopMatrix();
        GL11.glLineWidth(2);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();

        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
