package alexiil.mods.traincraft.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import alexiil.mods.traincraft.api.track.ITrackPath;
import alexiil.mods.traincraft.block.BlockTrackCurvedFull;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;

public class TrackCurvedFullBlockModel extends TrackGenericBlockModel {
    private final BlockTrackCurvedFull curved;

    public TrackCurvedFullBlockModel(BlockTrackCurvedFull curved) {
        this.curved = curved;
    }

    @Override
    public ITrackPath path(IBlockState state) {
        EnumFacing facing = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        return curved.path(facing);
    }
}
