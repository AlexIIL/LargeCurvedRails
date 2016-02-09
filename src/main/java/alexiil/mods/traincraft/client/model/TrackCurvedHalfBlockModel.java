package alexiil.mods.traincraft.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;

public class TrackCurvedHalfBlockModel extends TrackGenericBlockModel {
    private final BlockTrackCurvedHalf curved;

    public TrackCurvedHalfBlockModel(BlockTrackCurvedHalf curved) {
        this.curved = curved;
    }

    @Override
    public ITrackPath path(IBlockState state) {
        EnumFacing facing = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurvedHalf.PROPERTY_DIRECTION);
        return curved.path(positive, facing);
    }
}
