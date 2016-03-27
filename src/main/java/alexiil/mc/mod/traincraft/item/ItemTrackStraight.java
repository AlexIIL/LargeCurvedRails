package alexiil.mc.mod.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.TrackPlacer;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.block.BlockTrackStraight;
import alexiil.mc.mod.traincraft.block.EnumDirection;
import alexiil.mc.mod.traincraft.block.TCBlocks;
import alexiil.mc.mod.traincraft.track.TrackBehaviourStraightState;

public class ItemTrackStraight extends ItemBlockTrack {
    public ItemTrackStraight(Block block) {
        super(block);
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        EnumFacing entFacing = player.getHorizontalFacing();
        IBlockState state = TCBlocks.TRACK_STRAIGHT.getBlock().getDefaultState();
        if (entFacing.getAxis() == Axis.X) return state.withProperty(BlockTrackStraight.TRACK_DIRECTION, EnumDirection.EAST_WEST);
        else return state.withProperty(BlockTrackStraight.TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
    }

    @Override
    public TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ) {
        TrackBehaviourStraightState state = new TrackBehaviourStraightState(world, pos);
        EnumFacing entFacing = player.getHorizontalFacing();
        if (entFacing.getAxis() == Axis.X) state.setDir(EnumDirection.EAST_WEST);
        else state.setDir(EnumDirection.NORTH_SOUTH);
        return state;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        IBlockState targetState = targetState(world, pos, player, stack, side, hitX, hitY, hitZ);

        if (canPlaceTrack(world, pos, player, side, stack, pos) == null) {
            return world.setBlockState(pos, targetState);
        } else {
            TrackBehaviourStateful stateful = statefulState(world, pos, player, stack, side, hitX, hitY, hitZ);
            if (stateful == null) return false;
            return TrackPlacer.INSTANCE.tryPlaceTrack(stateful, world, pos);
        }
    }
}
