package alexiil.mods.traincraft.track;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.block.BlockAbstractTrack;
import alexiil.mods.traincraft.block.TCBlocks;

public class TrackBehaviourStraightNative extends TrackBehaviourNative {
    public static final TrackBehaviourStraightNative INSTANCE = new TrackBehaviourStraightNative();
    private static final String IDENTIFIER = "traincraft:track_straight::native";

    private TrackBehaviourStraightNative() {
        if (INSTANCE != null) throw new IllegalStateException("Reflection not allowed");
    }

    @Override
    public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() != TCBlocks.TRACK_STRAIGHT.getBlock()) return null;
        TrackBehaviourStraightState track = new TrackBehaviourStraightState(world, pos);
        track.setDir(state.getValue(BlockAbstractTrack.TRACK_DIRECTION));
        return track;
    }

    @Override
    public ITrackPath getPath(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() != TCBlocks.TRACK_STRAIGHT.getBlock()) return null;
        return state.getValue(BlockAbstractTrack.TRACK_DIRECTION).path.offset(pos);
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        return new TrackIdentifier(world.provider.getDimensionId(), pos, IDENTIFIER);
    }

    @Override
    public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {}

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
        return TrackBehaviour.SINGLE_BLOCK_SLAVES;
    }
}
