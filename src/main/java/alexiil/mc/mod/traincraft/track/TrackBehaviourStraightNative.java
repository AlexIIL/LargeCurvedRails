package alexiil.mc.mod.traincraft.track;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockAbstractTrack;
import alexiil.mc.mod.traincraft.block.TCBlocks;

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
        return state.getValue(BlockAbstractTrack.TRACK_DIRECTION).path;
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        return new TrackIdentifier(world.provider.getDimension(), pos, IDENTIFIER);
    }

    @Override
    public void onMinecartPass(World world, BlockPos pos, IBlockState state, EntityMinecart cart) {}

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
        return TrackBehaviour.SINGLE_BLOCK_SLAVES;
    }
}
