package alexiil.mods.traincraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.block.BlockAbstractTrack.EnumDirection;
import alexiil.mods.traincraft.block.BlockTrackStraight;
import alexiil.mods.traincraft.block.TCBlocks;

public class ItemTrackDiagonal extends ItemBlockTrainCraft {
    public ItemTrackDiagonal(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        float angle = player.rotationYaw;
        angle = (angle % 360 + 360) % 360;

        if (side != null && side != EnumFacing.UP) {
            if (side == EnumFacing.DOWN) {
                hitX = 1 - hitX;
                hitY = 1 - hitY;
            }
            // @formatter:off
            else if (side == EnumFacing.SOUTH) hitZ = 1;
            else if (side == EnumFacing.NORTH) hitZ = 0;
            else if (side == EnumFacing.WEST ) hitX = 0;
            else /*  side == EnumFacing.EAST*/ hitX = 1;
            // @formatter:on
        }

        EnumDirection direction;
        if ((angle >= 0 && angle <= 90) || (angle >= 180 && angle <= 270))

        {
            float graphT = hitX + hitZ - 1;
            if (graphT > 0) direction = EnumDirection.SOUTH_EAST;
            else direction = EnumDirection.NORTH_WEST;
        } else if ((angle >= 90 && angle <= 180) || (angle >= 270 && angle <= 360))

        {
            float graphT = hitZ - hitX;
            if (graphT > 0) direction = EnumDirection.SOUTH_WEST;
            else direction = EnumDirection.NORTH_EAST;
        } else

        {
            TrainCraft.trainCraftLog.warn("Could not place a block with an angle of " + angle);
            return false;
        }

        IBlockState actualState = TCBlocks.STRAIGHT_TRACK.getBlock().getDefaultState();
        actualState = actualState.withProperty(BlockTrackStraight.TRACK_DIRECTION, direction);
        world.setBlockState(pos, actualState);
        return true;
    }
}
