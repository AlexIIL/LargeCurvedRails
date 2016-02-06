package alexiil.mods.traincraft.client.model;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathProvider;

public abstract class TrackVanillaBlockModel extends TrackGenericBlockModel {
    public static final int CURVED_RAIL_GAP = 16;

    public static TrackVanillaBlockModel create(Block block) {
        if (block == Blocks.activator_rail) return new ActivatorModel();
        else if (block == Blocks.detector_rail) return new DetectorModel();
        else if (block == Blocks.golden_rail) return new GoldenModel();
        else return new NormalModel();
    }

    @Override
    public ITrackPath path(IBlockState state) {
        return TrackPathProvider.getVanillaTrack(state);
    }

    public static class NormalModel extends TrackVanillaBlockModel {
        public NormalModel() {}

        @Override
        protected List<BakedQuad> generateRails(IBlockState state, ITrackPath path) {
            EnumRailDirection dir = state.getValue(BlockRail.SHAPE);
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.railSprite(false);
            if (dir.isAscending() || dir.getMetadata() < 3) {
                return CommonModelSpriteCache.generateRails(path, sprite);
            }
            return CommonModelSpriteCache.generateRails(path, sprite, CURVED_RAIL_GAP);
        }
    }

    public static class DetectorModel extends TrackVanillaBlockModel {
        public DetectorModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {

        }
    }

    public static class ActivatorModel extends TrackVanillaBlockModel {
        public ActivatorModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {

        }
    }

    public static class GoldenModel extends TrackVanillaBlockModel {
        public GoldenModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {

        }
    }
}
