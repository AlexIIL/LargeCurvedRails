package alexiil.mods.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import alexiil.mods.traincraft.block.BlockTrackCurved;

public class ItemTrackCurved extends ItemBlockSeperatedTrack<BlockTrackCurved> {
    public ItemTrackCurved(Block block) {
        super((BlockTrackCurved) block);
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        IBlockState state = seperated.getDefaultState();
        EnumFacing horizontal = player.getHorizontalFacing();
        state = state.withProperty(BlockTrackCurved.PROPERTY_FACING, horizontal);
        float x = ((hitX % 1) + 1) % 1;
        float z = ((hitZ % 1) + 1) % 1;
        if (horizontal.getAxis() == Axis.X) {
            if (z > 0.5) {
                state = state.withProperty(BlockTrackCurved.PROPERTY_DIRECTION, true);
            } else {
                state = state.withProperty(BlockTrackCurved.PROPERTY_DIRECTION, false);
            }
        } else {// Z
            if (x > 0.5) {
                state = state.withProperty(BlockTrackCurved.PROPERTY_DIRECTION, true);
            } else {
                state = state.withProperty(BlockTrackCurved.PROPERTY_DIRECTION, false);
            }
        }
        return state;
    }
}
