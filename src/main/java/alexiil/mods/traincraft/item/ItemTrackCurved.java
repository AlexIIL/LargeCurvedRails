package alexiil.mods.traincraft.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.BlockTrackCurvedHalf;
import alexiil.mods.traincraft.block.TCBlocks;
import alexiil.mods.traincraft.track.TrackBehaviourCurvedHalfState;

public class ItemTrackCurved extends ItemBlockSeperatedTrack<BlockTrackCurvedHalf> {

    public ItemTrackCurved(TCBlocks block) {
        super((BlockTrackCurvedHalf) block.getBlock());
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        IBlockState state = seperated.getDefaultState();
        EnumFacing horizontal = player.getHorizontalFacing();
        state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_FACING, horizontal);
        float x = ((hitX % 1) + 1) % 1;
        float z = ((hitZ % 1) + 1) % 1;
        if (horizontal.getAxis() == Axis.X) {
            if (z > 0.5) {
                state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_DIRECTION, true);
            } else {
                state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_DIRECTION, false);
            }
        } else {// Z
            if (x > 0.5) {
                state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_DIRECTION, true);
            } else {
                state = state.withProperty(BlockTrackCurvedHalf.PROPERTY_DIRECTION, false);
            }
        }
        return state;
    }

    @Override
    public TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ) {
        TrackBehaviourCurvedHalfState state = new TrackBehaviourCurvedHalfState(world, pos, seperated.curve.halfFactory);
        EnumFacing horizontal = player.getHorizontalFacing();
        float x = ((hitX % 1) + 1) % 1;
        float z = ((hitZ % 1) + 1) % 1;
        if (horizontal.getAxis() == Axis.X) {
            if (z > 0.5) {
                state.setDir(horizontal, true);
            } else {
                state.setDir(horizontal, false);
            }
        } else {// Z
            if (x > 0.5) {
                state.setDir(horizontal, true);
            } else {
                state.setDir(horizontal, false);
            }
        }
        return state;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            // if (!world.isRemote) TrackJoiner.INSTANCE.tryJoin(world, pos);
            return true;
        } else return false;
    }
}
