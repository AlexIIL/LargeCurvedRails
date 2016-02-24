package alexiil.mods.traincraft.item;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.BlockTrackAscending;
import alexiil.mods.traincraft.block.EnumDirection;
import alexiil.mods.traincraft.block.TCBlocks;

public class ItemTrackAscendingAxis extends ItemBlockSeperatedTrack<BlockTrackAscending> {
    public ItemTrackAscendingAxis(Block block) {
        super((BlockTrackAscending) block);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        // TODO Auto-generated method stub
        super.getSubItems(itemIn, tab, subItems);
    }

    @Override
    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = super.getTrackBlockSetters(targetState, stack);
        EnumDirection dir = targetState.getValue(BlockTrackAscending.TRACK_DIRECTION);
        boolean b = targetState.getValue(BlockTrackAscending.ASCEND_DIRECTION);
        EnumFacing matFace = b ? dir.from : dir.to;
        if (matFace.getAxis() == Axis.Z) matFace = matFace.getOpposite();
        setters.put(BlockPos.ORIGIN.offset(matFace), (world, pos) -> {
            world.setBlockState(pos, getMaterialState(stack));
        });
        return setters;
    }

    protected static IBlockState getMaterialState(ItemStack stack) {
        return TCBlocks.TRACK_POINTER_ASCENDING.getBlock().getStateFromMeta(stack.getItemDamage());
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        EnumFacing face = player.getHorizontalFacing();
        IBlockState state = seperated.getDefaultState();
        if (face == EnumFacing.EAST) {
            state = state.withProperty(BlockTrackAscending.TRACK_DIRECTION, EnumDirection.EAST_WEST);
        } else if (face == EnumFacing.WEST) {
            state = state.withProperty(BlockTrackAscending.TRACK_DIRECTION, EnumDirection.EAST_WEST);
            state = state.withProperty(BlockTrackAscending.ASCEND_DIRECTION, false);
        } else if (face == EnumFacing.SOUTH) {
            state = state.withProperty(BlockTrackAscending.TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
        } else if (face == EnumFacing.NORTH) {
            state = state.withProperty(BlockTrackAscending.TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
            state = state.withProperty(BlockTrackAscending.ASCEND_DIRECTION, false);
        }
        return state;
    }

    @Override
    protected TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ) {
        return null; // No state for this (it cannot overlap so there is no point)
    }
    
    
    @Override
    protected EnumTrackRequirement canPlaceTrack(World world, BlockPos pos, BlockPos origin) {
        double length = seperated.length / 2;
        length *= length;
        if (origin.distanceSq(pos) < length) {
            IBlockState below = world.getBlockState(pos.down());
            if (!below.getBlock().isSideSolid(world, pos.down(), EnumFacing.UP)) return EnumTrackRequirement.GROUND_BELOW;
        }
        if (!world.isAirBlock(pos.up())) return EnumTrackRequirement.SPACE_ABOVE;
        return null;
    }
}
