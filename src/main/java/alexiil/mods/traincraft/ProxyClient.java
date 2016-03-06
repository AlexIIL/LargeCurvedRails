package alexiil.mods.traincraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mods.traincraft.api.AddonManager;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.api.lib.MathUtil;
import alexiil.mods.traincraft.api.track.ITrackPlacer.EnumTrackRequirement;
import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mods.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.AlignmentFailureException;
import alexiil.mods.traincraft.block.*;
import alexiil.mods.traincraft.client.model.*;
import alexiil.mods.traincraft.client.render.RenderRollingStockBase;
import alexiil.mods.traincraft.client.render.SmoothFaceRenderer;
import alexiil.mods.traincraft.component.ComponentCart;
import alexiil.mods.traincraft.component.ComponentSmallWheel;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;
import alexiil.mods.traincraft.item.ItemBlockSeperatedTrack;
import alexiil.mods.traincraft.item.ItemPlacableTrain;

public class ProxyClient extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        TrainCraftAPI.SPRITE_GETTER = CommonModelSpriteCache.INSTANCE;

        RenderingRegistry.registerEntityRenderingHandler(EntityGenericRollingStock.class, RenderRollingStockBase.Factory.INSTANCE);
        OBJLoader.instance.addDomain("traincraft");
        for (TCBlocks b : TCBlocks.values()) {
            Block block = b.getBlock();
            if (block instanceof BlockAbstractTrack) {
                ModelLoader.setCustomStateMapper(b.getBlock(), VoidStateMapper.INSTANCE);
            }
        }

        Block[] vanillaTracks = { Blocks.rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail };

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
                bake.modelRegistry.putObject(mrl, new TrackCurvedHalfBlockModel(curved));
            }

            if (b.getBlock() instanceof BlockTrackPointer) {
                bake.modelRegistry.putObject(mrl, TrackPointerBlockModel.INSTANCE);
            }

            if (b.getBlock() instanceof BlockTrackAscending) {
                BlockTrackAscending ascending = (BlockTrackAscending) b.getBlock();
                bake.modelRegistry.putObject(mrl, new TrackAscendingBlockModel(ascending));
            }

            if (b.getBlock() instanceof BlockTrackStraight) {
                bake.modelRegistry.putObject(mrl, new TrackStraightBlockModel());
            }

            if (b.getBlock() instanceof BlockTrackCurvedFull) {
                BlockTrackCurvedFull curved = (BlockTrackCurvedFull) b.getBlock();
                bake.modelRegistry.putObject(mrl, new TrackCurvedFullBlockModel(curved));
            }

            if (b.getBlock() instanceof BlockTrackMultiple) {
                bake.modelRegistry.putObject(mrl, TrackGenericBlockModel_NEW_.INSTANCE);
            }
        }

        Block[] vanillaTracks = { Blocks.rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail };

        for (Block rail : vanillaTracks) {
            ModelResourceLocation mrl = new ModelResourceLocation(Block.blockRegistry.getNameForObject(rail).toString());
            bake.modelRegistry.putObject(mrl, TrackVanillaBlockModel.create(rail));
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
        if (Minecraft.getMinecraft().getRenderManager().livingPlayer == null) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getHeldItem() == null || player.getHeldItem().stackSize == 0) return;
        Item item = player.getHeldItem().getItem();
        if (!(item instanceof ItemPlacableTrain)) return;

        Minecraft.getMinecraft().mcProfiler.startSection("fake_train");

        ItemPlacableTrain place = (ItemPlacableTrain) item;

        EntityGenericRollingStock rollingStock = place.createRollingStock(player.worldObj);
        if (rollingStock != null) {
            GL11.glPushMatrix();
            Vec3 lookVec = player.getLook(event.partialTicks).normalize();
            Vec3 lookFrom = player.getPositionEyes(event.partialTicks);
            try {
                if (rollingStock.alignFromPlayer(lookVec, lookFrom, true)) {
                    RenderRollingStockBase.enableCustomColour(0, 1, 0);
                } else {
                    RenderRollingStockBase.enableCustomColour(1, 1, 1);
                }
            } catch (AlignmentFailureException afe) {
                // Re-create it so it doesn't partially mess up
                rollingStock = place.createRollingStock(player.worldObj);
                Vec3 maxLook = lookFrom.add(MathUtil.scale(lookVec, 4));
                MovingObjectPosition mop = player.worldObj.rayTraceBlocks(lookFrom, maxLook);
                Vec3 lookingAt = null;
                if (mop != null) lookingAt = mop.hitVec;
                if (lookingAt == null) lookingAt = maxLook;
                RenderRollingStockBase.enableCustomColour(1, 0, 0);
                Vec3 renderOffset = lookingAt;
                GL11.glTranslated(renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);
            }

            Minecraft.getMinecraft().getRenderManager().renderEntitySimple(rollingStock, event.partialTicks);
            RenderRollingStockBase.disableCustomColour();

            RenderHelper.disableStandardItemLighting();

            GL11.glPopMatrix();
        }

        Minecraft.getMinecraft().mcProfiler.endSection();
    }

    private static void renderFakeItemBlock(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().getRenderManager().livingPlayer == null) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getHeldItem() == null || player.getHeldItem().stackSize == 0) return;
        Item item = player.getHeldItem().getItem();
        if (!(item instanceof ItemBlockSeperatedTrack<?>)) return;
        ItemBlockSeperatedTrack<?> track = (ItemBlockSeperatedTrack<?>) item;

        if (Minecraft.getMinecraft().objectMouseOver == null || Minecraft.getMinecraft().objectMouseOver.typeOfHit != MovingObjectType.BLOCK) {
            return;
        }
        MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;

        BlockPos hitPos = mop.getBlockPos();
        EnumFacing side = mop.sideHit;
        Vec3 hitVec = mop.hitVec;

        float hitX = (float) (hitVec.xCoord - hitPos.getX());
        float hitY = (float) (hitVec.yCoord - hitPos.getY());
        float hitZ = (float) (hitVec.zCoord - hitPos.getZ());

        TrackBehaviourStateful state = track.statefulState(player.worldObj, hitPos, player, player.getHeldItem(), side, hitX, hitY, hitZ);
        if (state == null) return;

        // FIXME: This check doesn't work. Also the "hitPos" is actually wrong.
        EnumTrackRequirement req = TrackPlacer.INSTANCE.checkSlaves(state.getSlaveOffsets(), player.worldObj, hitPos);

        TrackModelWrapper[] wrappers = { new TrackModelWrapper(state.getPath(), state.getModel()) };
        IBakedModel model = TrackGenericBlockModel_NEW_.makeModel(wrappers);

        float[] col = { 1, 1, 1 };

        if (req == EnumTrackRequirement.GROUND_BELOW) col = new float[] { 0, 0, 0.6f };
        else if (req == EnumTrackRequirement.SPACE_ABOVE) col = new float[] { 0, 0, 0 };
        else if (req == EnumTrackRequirement.OTHER) col = new float[] { 0.6f, 0, 0 };

        Vec3 diff = new Vec3(hitPos);
        diff = diff.subtract(player.getPositionEyes(event.partialTicks));
        diff = diff.addVector(0, 1 + player.getEyeHeight(), 0);

        GlStateManager.disableTexture2D();

        SmoothFaceRenderer.renderModelMultColour(model, MatrixUtil.translation(diff), col[0], col[1], col[2]);

        GlStateManager.enableTexture2D();
    }

    private static void renderDebug(RenderWorldLastEvent event) {
        if (MinecraftServer.getServer() == null) return;
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
        BlockPos around = new BlockPos(Minecraft.getMinecraft().thePlayer.getPositionVector());
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World world = MinecraftServer.getServer().worldServerForDimension(Minecraft.getMinecraft().theWorld.provider.getDimensionId());
        if (world == null) return;
        Minecraft.getMinecraft().mcProfiler.startSection("debug");

        List<ITrackPath> drawn = new ArrayList<>();

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();

        GL11.glLineWidth(3);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;

        wr.begin(GL11.GL_LINES, format);
        Vec3 interp = player.getPositionEyes(event.partialTicks);
        interp = interp.addVector(0, -player.getEyeHeight(), 0);
        wr.setTranslation(-interp.xCoord, -interp.yCoord, -interp.zCoord);

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
                            Vec3 point = path.interpolate(pos);

                            wr.pos(point.xCoord, point.yCoord, point.zCoord).color(255, 0, 0, 255).endVertex();

                            Vec3 dir = path.direction(pos);
                            dir = new Vec3(dir.xCoord * STEP_DIST, dir.yCoord * STEP_DIST, dir.zCoord * STEP_DIST);
                            Vec3 point2 = point.add(dir);

                            wr.pos(point2.xCoord, point2.yCoord, point2.zCoord).color(255, 0, 0, 255).endVertex();
                        }

                        BlockPos c = path.creatingBlock();
                        wr.pos(c.getX() + 0.5, c.getY() + 0.3, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();
                        wr.pos(c.getX() + 0.5, c.getY() + 0.7, c.getZ() + 0.5).color(0, 0, 0, 255).endVertex();

                        Vec3 s = path.start();
                        wr.pos(s.xCoord, s.yCoord, s.zCoord).color(0, 255, 0, 255).endVertex();
                        wr.pos(s.xCoord, s.yCoord + 0.3, s.zCoord).color(0, 255, 0, 255).endVertex();

                        Vec3 e = path.end();
                        wr.pos(e.xCoord, e.yCoord, e.zCoord).color(0, 255, 0, 255).endVertex();
                        wr.pos(e.xCoord, e.yCoord + 0.3, e.zCoord).color(0, 255, 0, 255).endVertex();

                        path.renderInfo(wr);
                    }
                }
            }
        }
        tess.draw();
        wr.setTranslation(0, 0, 0);
        GL11.glLineWidth(2);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();

        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
