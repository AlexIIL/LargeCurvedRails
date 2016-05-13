package alexiil.mc.mod.traincraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
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
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mc.mod.traincraft.api.AddonManager;
import alexiil.mc.mod.traincraft.api.TrainCraftAPI;
import alexiil.mc.mod.traincraft.api.lib.MathUtil;
import alexiil.mc.mod.traincraft.api.track.ITrackPlacer.EnumTrackRequirement;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.api.train.AlignmentFailureException;
import alexiil.mc.mod.traincraft.block.*;
import alexiil.mc.mod.traincraft.client.model.*;
import alexiil.mc.mod.traincraft.client.render.RenderRollingStockBase;
import alexiil.mc.mod.traincraft.client.render.SmoothFaceRenderer;
import alexiil.mc.mod.traincraft.component.ComponentCart;
import alexiil.mc.mod.traincraft.component.ComponentSmallWheel;
import alexiil.mc.mod.traincraft.entity.EntityGenericRollingStock;
import alexiil.mc.mod.traincraft.item.ItemBlockSeperatedTrack;
import alexiil.mc.mod.traincraft.item.ItemPlacableTrain;

public class ProxyClient extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        TrainCraftAPI.SPRITE_GETTER = CommonModelSpriteCache.INSTANCE;

        RenderingRegistry.registerEntityRenderingHandler(EntityGenericRollingStock.class, RenderRollingStockBase.Factory.INSTANCE);
        OBJLoader.INSTANCE.addDomain("traincraft");
        for (TCBlocks b : TCBlocks.values()) {
            Block block = b.getBlock();
            if (block instanceof BlockAbstractTrack) {
                ModelLoader.setCustomStateMapper(b.getBlock(), VoidStateMapper.INSTANCE);
            }
        }

        Block[] vanillaTracks = { Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL };

        for (Block rail : vanillaTracks) {
            ModelLoader.setCustomStateMapper(rail, VoidStateMapper.INSTANCE);
        }
    }

    @SubscribeEvent
    public void modelBake(ModelBakeEvent bake) {
        RenderRollingStockBase.clearModelMap();
        CommonModelSpriteCache.INSTANCE.clearModelMap();

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

        Block[] vanillaTracks = { Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL };

        for (Block rail : vanillaTracks) {
            ModelResourceLocation mrl = new ModelResourceLocation(Block.REGISTRY.getNameForObject(rail).toString());
            bake.getModelRegistry().putObject(mrl, TrackVanillaBlockModel.create(rail));
        }
        AddonManager.INSTANCE.modelBake(bake);
    }

    @SubscribeEvent
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        ComponentSmallWheel.textureStitchPre(event);
        ComponentCart.textureStitchPre(event);
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
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("traincraft");
        renderFakeTrain(event);
        renderFakeItemBlock(event);
        renderDebug(event);
        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderFakeTrain(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().getRenderManager().renderViewEntity == null) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getHeldItemMainhand() == null || player.getHeldItemMainhand().stackSize == 0) return;
        Item item = player.getHeldItemMainhand().getItem();
        if (!(item instanceof ItemPlacableTrain)) return;

        Minecraft.getMinecraft().mcProfiler.startSection("fake_train");

        ItemPlacableTrain place = (ItemPlacableTrain) item;

        EntityGenericRollingStock rollingStock = place.createRollingStock(player.worldObj);
        if (rollingStock != null) {
            GL11.glPushMatrix();
            Vec3d lookVec = player.getLook(event.getPartialTicks()).normalize();
            Vec3d lookFrom = player.getPositionEyes(event.getPartialTicks());
            try {
                if (rollingStock.alignFromPlayer(lookVec, lookFrom, true)) {
                    RenderRollingStockBase.enableCustomColour(0, 1, 0);
                } else {
                    RenderRollingStockBase.enableCustomColour(1, 1, 1);
                }
            } catch (AlignmentFailureException afe) {
                // Re-create it so it doesn't partially mess up
                rollingStock = place.createRollingStock(player.worldObj);
                Vec3d maxLook = lookFrom.add(MathUtil.scale(lookVec, 4));
                RayTraceResult mop = player.worldObj.rayTraceBlocks(lookFrom, maxLook);
                Vec3d lookingAt = null;
                if (mop != null) lookingAt = mop.hitVec;
                if (lookingAt == null) lookingAt = maxLook;
                RenderRollingStockBase.enableCustomColour(1, 0, 0);
                Vec3d renderOffset = lookingAt;
                GL11.glTranslated(renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);
            }

            Minecraft.getMinecraft().getRenderManager().renderEntityStatic(rollingStock, event.getPartialTicks(), false);
            RenderRollingStockBase.disableCustomColour();

            RenderHelper.disableStandardItemLighting();

            GL11.glPopMatrix();
        }

        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderFakeItemBlock(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().getRenderManager().renderViewEntity == null) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getHeldItemMainhand() == null || player.getHeldItemMainhand().stackSize == 0) return;
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

        float hitX = (float) (hitVec.xCoord - hitPos.getX());
        float hitY = (float) (hitVec.yCoord - hitPos.getY());
        float hitZ = (float) (hitVec.zCoord - hitPos.getZ());

        hitPos = hitPos.offset(side);

        TrackBehaviourStateful state = track.statefulState(player.worldObj, hitPos, player, player.getHeldItemMainhand(), side, hitX, hitY, hitZ);
        if (state == null) return;

        // FIXME: This check doesn't work. Also the "hitPos" is actually wrong.
        EnumTrackRequirement req = TrackPlacer.INSTANCE.checkSlaves(state.getSlaveOffsets(), player.worldObj, hitPos);

        TrackModelWrapper[] wrappers = { new TrackModelWrapper(state.getPath(), state.getModel()) };
        IBakedModel model = TrackGenericBlockModel_NEW_.makeModel(wrappers);

        float[] col = { 1, 1, 1 };

        if (req == EnumTrackRequirement.GROUND_BELOW) col = new float[] { 0, 0, 0.6f };
        else if (req == EnumTrackRequirement.SPACE_ABOVE) col = new float[] { 0, 0, 0 };
        else if (req == EnumTrackRequirement.OTHER) col = new float[] { 0.6f, 0, 0 };

        Vec3d diff = new Vec3d(hitPos);
        diff = diff.subtract(player.getPositionEyes(event.getPartialTicks()));
        diff = diff.addVector(0, 1 + player.getEyeHeight(), 0);

        GlStateManager.disableTexture2D();

        SmoothFaceRenderer.renderModelMultColour(model, MatrixUtil.translation(diff), col[0], col[1], col[2]);

        GlStateManager.enableTexture2D();
    }

    private static void renderDebug(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null) return;
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
        BlockPos around = new BlockPos(Minecraft.getMinecraft().thePlayer.getPositionVector());
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World world = DimensionManager.getWorld(Minecraft.getMinecraft().theWorld.provider.getDimension());
        if (world == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("debug");

        List<ITrackPath> drawn = new ArrayList<>();

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();

        GL11.glLineWidth(3);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;

        vb.begin(GL11.GL_LINES, format);
        Vec3d interp = player.getPositionEyes(event.getPartialTicks());
        interp = interp.addVector(0, -player.getEyeHeight(), 0);
        vb.setTranslation(-interp.xCoord, -interp.yCoord, -interp.zCoord);

        final int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos offset = around.add(x, y, z);
                    List<BehaviourWrapper> paths = TrainCraftAPI.TRACK_PROVIDER.getTracksAsList(world, offset, world.getBlockState(offset));
                    for (BehaviourWrapper behaviourWrapper : paths) {
                        if (behaviourWrapper == null) continue;
                        ITrackPath path = behaviourWrapper.getPath();
                        if (path == null) continue;
                        if (drawn.contains(path)) continue;
                        drawn.add(path);
                        int steps = (int) (path.length() / STEP_DIST);
                        for (int s = 0; s < steps; s++) {
                            double pos = s / (double) steps;
                            Vec3d point = path.interpolate(pos);

                            vb.pos(point.xCoord, point.yCoord, point.zCoord).color(255, 0, 0, 255).endVertex();

                            Vec3d dir = path.direction(pos);
                            dir = new Vec3d(dir.xCoord * STEP_DIST, dir.yCoord * STEP_DIST, dir.zCoord * STEP_DIST);
                            Vec3d point2 = point.add(dir);

                            vb.pos(point2.xCoord, point2.yCoord, point2.zCoord).color(255, 0, 0, 255).endVertex();
                        }

                        BlockPos c = path.creatingBlock();
                        vb.pos(c.getX() + 0.5, c.getY() + 0.3, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();
                        vb.pos(c.getX() + 0.5, c.getY() + 0.7, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();

                        Vec3d s = path.start();
                        vb.pos(s.xCoord, s.yCoord, s.zCoord).color(0, 255, 0, 255).endVertex();
                        vb.pos(s.xCoord, s.yCoord + 0.3, s.zCoord).color(0, 255, 0, 255).endVertex();

                        Vec3d e = path.end();
                        vb.pos(e.xCoord, e.yCoord, e.zCoord).color(0, 255, 0, 255).endVertex();
                        vb.pos(e.xCoord, e.yCoord + 0.3, e.zCoord).color(0, 255, 0, 255).endVertex();

                        path.renderInfo(vb);
                    }
                }
            }
        }
        tess.draw();
        vb.setTranslation(0, 0, 0);
        GL11.glLineWidth(2);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();

        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
