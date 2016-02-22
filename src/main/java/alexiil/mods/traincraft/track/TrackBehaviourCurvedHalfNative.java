package alexiil.mods.traincraft.track;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;

public class TrackBehaviourCurvedHalfNative extends TrackBehaviourNative {
    private final Curve curve;

    public TrackBehaviourCurvedHalfNative(Curve curve) {
        this.curve = curve;
    }

    @Override
    public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() != curve.halfBlock) return null;
        EnumFacing face = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurvedHalf.PROPERTY_DIRECTION);
        TrackBehaviourCurvedHalfState track = new TrackBehaviourCurvedHalfState(world, pos, curve.halfFactory);
        track.setDir(face, positive);
        return track;
    }

    @Override
    public ITrackPath getPath(IBlockAccess access, BlockPos pos, IBlockState state) {
        if (state.getBlock() != curve.halfBlock) {
            TrainCraft.trainCraftLog.info(" state.getBlock() = " + state.getBlock());
            TrainCraft.trainCraftLog.info(" curve.halfBlock = " + curve.halfBlock);
            return null;
        }
        EnumFacing face = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurvedHalf.PROPERTY_DIRECTION);
        return curve.halfFactory.getPath(face, positive).offset(pos);
    }

    @Override
    public boolean isValid(IBlockAccess access, BlockPos pos, IBlockState state) {
        return state.getBlock() == curve.halfBlock;
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        return new TrackIdentifier(world.provider.getDimensionId(), pos, "");// FIXME!
    }

    @Override
    public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {}
}
