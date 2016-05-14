package alexiil.mc.mod.traincraft.track;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.TrainCraft;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourNative;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackIdentifier;
import alexiil.mc.mod.traincraft.api.track.model.DefaultTrackModel;
import alexiil.mc.mod.traincraft.api.track.model.ITrackModel;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockTrackCurvedHalf;

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
    public ITrackPath getPath(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() != curve.halfBlock) {
            TrainCraft.trainCraftLog.info(" state.getBlock() = " + state.getBlock());
            TrainCraft.trainCraftLog.info(" curve.halfBlock = " + curve.halfBlock);
            return null;
        }
        EnumFacing face = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurvedHalf.PROPERTY_DIRECTION);
        return curve.halfFactory.getPath(face, positive);
    }

    @Override
    public boolean isValid(World world, BlockPos pos, IBlockState state) {
        return state.getBlock() == curve.halfBlock;
    }

    @Override
    public boolean canOverlap(TrackBehaviour otherTrack) {
        return true;
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(World world, BlockPos pos, IBlockState state) {
        EnumFacing face = state.getValue(BlockTrackCurvedHalf.PROPERTY_FACING);
        boolean positive = state.getValue(BlockTrackCurvedHalf.PROPERTY_DIRECTION);
        return curve.halfFactory.getSlaves(face, positive);
    }

    @Override
    public TrackIdentifier getIdentifier(World world, BlockPos pos, IBlockState state) {
        return new TrackIdentifier(world.provider.getDimension(), pos, curve.halfIdentifier + "native");// FIXME!
    }

    @Override
    public void onMinecartPass(World world, BlockPos pos, IBlockState state, EntityMinecart cart) {}

    @Override
    @SideOnly(Side.CLIENT)
    public ITrackModel getModel() {
        return DefaultTrackModel.INSTANCE;
    }
}
