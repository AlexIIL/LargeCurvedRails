package alexiil.mods.traincraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathProvider;
import alexiil.mods.traincraft.block.BlockTrackAscending;
import alexiil.mods.traincraft.block.BlockTrackCurved;
import alexiil.mods.traincraft.block.BlockTrackPointer;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.client.model.*;
import alexiil.mods.traincraft.client.render.RenderRollingStockBase;
import alexiil.mods.traincraft.component.ComponentCart;
import alexiil.mods.traincraft.component.ComponentSmallWheel;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public class ProxyClient extends Proxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityRollingStockBase.class, RenderRollingStockBase.Factory.INSTANCE);
        OBJLoader.instance.addDomain("traincraft");
        for (TCBlocks b : TCBlocks.values()) {
            Block block = b.getBlock();
            if (block instanceof BlockTrackCurved || block instanceof BlockTrackPointer || block instanceof BlockTrackAscending) {
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
        CurvedModelGenerator.INSTANCE.clearModelMap();
        CommonModelSpriteCache.INSTANCE.clearModelMap();

        for (TCBlocks b : TCBlocks.values()) {
            ModelResourceLocation mrl = new ModelResourceLocation("traincraft:" + b.name().toLowerCase(Locale.ROOT));

            if (b.getBlock() instanceof BlockTrackCurved) {
                BlockTrackCurved curved = (BlockTrackCurved) b.getBlock();
                bake.modelRegistry.putObject(mrl, new TrackCurvedBlockModel(curved));
            }

            if (b.getBlock() instanceof BlockTrackPointer) {
                bake.modelRegistry.putObject(mrl, TrackPointerBlockModel.INSTANCE);
            }

            if (b.getBlock() instanceof BlockTrackAscending) {
                BlockTrackAscending ascending = (BlockTrackAscending) b.getBlock();
                bake.modelRegistry.putObject(mrl, new TrackAscendingBlockModel(ascending));
            }
        }

        Block[] vanillaTracks = { Blocks.rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail };

        for (Block rail : vanillaTracks) {
            ModelResourceLocation mrl = new ModelResourceLocation(Block.blockRegistry.getNameForObject(rail).toString());
            bake.modelRegistry.putObject(mrl, TrackVanillaBlockModel.create(rail));
        }
    }

    @SubscribeEvent
    public void textureStitchPre(TextureStitchEvent.Pre event) {
        ComponentSmallWheel.textureStitchPre(event);
        ComponentCart.textureStitchPre(event);
    }

    @SubscribeEvent
    public void textureStitchPost(TextureStitchEvent.Post event) {
        CommonModelSpriteCache.INSTANCE.textureStitchPost(event);
    }

    private static final double STEP_DIST = 0.3;

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        BlockPos around = new BlockPos(Minecraft.getMinecraft().thePlayer.getPositionVector());
        World world = Minecraft.getMinecraft().theWorld;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

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
                    ITrackPath[] paths = TrackPathProvider.getPathsAsArray(world, offset, world.getBlockState(offset));
                    for (ITrackPath path : paths) {
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
    }
}
