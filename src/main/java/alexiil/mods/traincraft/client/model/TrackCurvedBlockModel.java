package alexiil.mods.traincraft.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackCurved;

public class TrackCurvedBlockModel extends TrackGenericBlockModel {
    private final BlockTrackCurved curved;

    public TrackCurvedBlockModel(BlockTrackCurved curved) {
        this.curved = curved;
    }

    @Override
    public ITrackPath path(IBlockState state) {
        EnumFacing facing = state.getValue(BlockTrackCurved.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurved.PROPERTY_DIRECTION);
        return curved.path(positive, facing);
    }
}
