package alexiil.mc.mod.traincraft.client.model;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockAbstractTrack;
import alexiil.mc.mod.traincraft.block.EnumDirection;

public class TrackStraightBlockModel extends TrackGenericBlockModel {
    @Override
    public ITrackPath path(IBlockState state) {
        EnumDirection dir = state.getValue(BlockAbstractTrack.TRACK_DIRECTION);
        return dir.path;
    }

    @Override
    protected List<BakedQuad> generateSleepers(IBlockState state, ITrackPath path) {
        EnumDirection dir = state.getValue(BlockAbstractTrack.TRACK_DIRECTION);
        boolean reverse = dir == EnumDirection.NORTH_WEST || dir == EnumDirection.NORTH_EAST;
        return CommonModelSpriteCache.generateSleepers(path, CommonModelSpriteCache.INSTANCE.loadSleepers(), reverse);
    }
}
