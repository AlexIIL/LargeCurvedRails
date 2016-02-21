package alexiil.mods.traincraft.client.model;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;

import alexiil.mods.traincraft.TrackPathProvider;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.client.model.CommonModelSpriteCache.GenerateRailsArguments;

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
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.spriteVanillaRail(false);
            if (dir.isAscending() || dir.getMetadata() < 3) {
                return CommonModelSpriteCache.generateRails(path, sprite);
            }
            GenerateRailsArguments args = new GenerateRailsArguments(path, sprite);
            return CommonModelSpriteCache.generateRails(args.railGap(CURVED_RAIL_GAP));
        }
    }

    public static class DetectorModel extends TrackVanillaBlockModel {
        public DetectorModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {
            boolean powered = state.getValue(BlockRailDetector.POWERED);
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.spriteVanillaExtras();

            // Redstone Block
            GenerateRailsArguments args = new GenerateRailsArguments(path, sprite).width(2 / 16.0).radius(0).yOffset(-0.5 / 16.0).left(false);
            if (powered) args.uMin(6).uMax(8);
            else args.uMin(4).uMax(6);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

            // Detecting block

        }
    }

    public static class ActivatorModel extends TrackVanillaBlockModel {
        public ActivatorModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {
            boolean powered = state.getValue(BlockRailPowered.POWERED);
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.spriteVanillaExtras();

            // Left rail
            GenerateRailsArguments args = new GenerateRailsArguments(path, sprite).width(1 / 16.0).radius(3.5 / 16.0).right(false);
            if (powered) args.uMin(6).uMax(7);
            else args.uMin(4).uMax(5);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

            // Right rail
            args = new GenerateRailsArguments(path, sprite).width(1 / 16.0).radius(3.5 / 16.0).left(false);
            if (powered) args.uMin(7).uMax(8);
            else args.uMin(5).uMax(6);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

            // Redstone Block
            args = new GenerateRailsArguments(path, sprite).width(2 / 16.0).radius(0).yOffset(-0.5 / 16.0).left(false);
            if (powered) args.uMin(6).uMax(8);
            else args.uMin(4).uMax(6);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

        }
    }

    public static class GoldenModel extends TrackVanillaBlockModel {
        public GoldenModel() {}

        @Override
        public void generateExtra(List<BakedQuad> quads, IBlockState state, ITrackPath path) {
            boolean powered = state.getValue(BlockRailPowered.POWERED);
            TextureAtlasSprite sprite = CommonModelSpriteCache.INSTANCE.spriteVanillaExtras();

            GenerateRailsArguments args = new GenerateRailsArguments(path, sprite).left(false).width(1 / 16.0).radius(3.5 / 16.0);
            if (powered) args.uMin(2).uMax(3);
            else args.uMin(0).uMax(1);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

            args = new GenerateRailsArguments(path, sprite).right(false).width(1 / 16.0).radius(3.5 / 16.0);
            if (powered) args.uMin(3).uMax(4);
            else args.uMin(1).uMax(2);
            quads.addAll(CommonModelSpriteCache.generateRails(args));

            // Redstone Block
            args = new GenerateRailsArguments(path, sprite).width(2 / 16.0).radius(0).yOffset(-0.5 / 16.0).left(false);
            if (powered) args.uMin(6).uMax(8);
            else args.uMin(4).uMax(6);
            quads.addAll(CommonModelSpriteCache.generateRails(args));
        }
    }
}
