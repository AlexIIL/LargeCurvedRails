package alexiil.mc.mod.traincraft.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedFull;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedHalf;

public class TrackCurvedFullBlockModel extends TrackGenericBlockModel {
    private final BlockTrackCurvedFull curved;

    public TrackCurvedFullBlockModel(BlockTrackCurvedFull curved) {
        this.curved = curved;
    }

    @Override
    public ITrackPath path(IBlockState state) {
        EnumFacing facing = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        return curved.curve.fullFactory.getPath(facing);
    }
}
