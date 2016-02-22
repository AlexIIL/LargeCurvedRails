package alexiil.mods.traincraft.track;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mods.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.api.train.IRollingStock;

public class TrackBehaviourPointerNative extends TrackBehaviourNative {
    private final TrackBehaviour pointedTo;
    private final BlockPos masterPos;
    private final IBlockState masterState;

    public TrackBehaviourPointerNative(TrackBehaviour pointedTo, BlockPos masterPos, IBlockState masterState) {
        this.pointedTo = pointedTo;
        this.masterPos = masterPos;
        this.masterState = masterState;
    }

    @Override
    public ITrackPath getPath(IBlockAccess access, BlockPos pos, IBlockState state) {
        return pointedTo.getPath(access, masterPos, masterState);
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        return pointedTo.getIdentifier(world, masterPos, masterState);
    }

    @Override
    public void onStockPass(World world, BlockPos pos, IBlockState state, IRollingStock stock) {
        pointedTo.onStockPass(world, masterPos, masterState, stock);
    }

    @Override
    public boolean isValid(IBlockAccess access, BlockPos pos, IBlockState state) {
        return pointedTo.isValid(access, masterPos, access.getBlockState(masterPos));
    }

    public BehaviourWrapper getPointedTo(World world) {
        return new BehaviourWrapper(pointedTo, world, masterPos);
    }

    @Override
    public TrackBehaviourStateful convertToStateful(World world, BlockPos pos, IBlockState state) {
        return null;
    }
}
