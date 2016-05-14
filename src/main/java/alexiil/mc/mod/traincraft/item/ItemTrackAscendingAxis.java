package alexiil.mc.mod.traincraft.item;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.track.ITrackPlacer.EnumTrackRequirement;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockTrackAscending;
import alexiil.mc.mod.traincraft.block.EnumDirection;
import alexiil.mc.mod.traincraft.block.TCBlocks;

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
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        IBlockState targetState = targetState(world, pos, player, stack, side, hitX, hitY, hitZ);

        Map<BlockPos, IBlockSetter> setters = getTrackBlockSetters(targetState, stack);
        for (BlockPos p : setters.keySet()) {
            BlockPos offset = p.add(pos);
            IBlockState at = world.getBlockState(offset);
            if (!at.getBlock().isReplaceable(world, offset)) return false;
        }
        for (BlockPos s : setters.keySet()) {
            BlockPos offset = s.add(pos);
            setters.get(s).placeBlockAt(world, offset);
        }
        // for (BlockPos s : setters.keySet()) {
        // world.notifyBlockOfStateChange(pos.add(s), Blocks.AIR);
        // }
        return true;
    }

    @Override
    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = super.getTrackBlockSetters(targetState, stack);
        EnumDirection dir = targetState.getValue(BlockTrackAscending.TRACK_DIRECTION);
        boolean b = targetState.getValue(BlockTrackAscending.ASCEND_DIRECTION);
        EnumFacing matFace = b ? dir.from : dir.to;
        if (matFace.getAxis() == Axis.Z) matFace = matFace.getOpposite();
        IBlockState matState = getMaterialState(stack);
        setters.put(BlockPos.ORIGIN.offset(matFace), (world, pos) -> {
            world.setBlockState(pos, matState, 2);
        });
        return setters;
    }

    protected static IBlockState getMaterialState(ItemStack stack) {
        return TCBlocks.TRACK_POINTER_ASCENDING.getBlock().getStateFromMeta(stack.getItemDamage());
    }

    @Override
    protected IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
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
    public TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        return null; // No state for this (it cannot overlap so there is no point)
    }

    @Override
    public ITrackPath getPreviewPath(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        return seperated.path(targetState(world, pos, player, stack, side, hitX, hitY, hitZ));
    }

    @Override
    protected EnumTrackRequirement canPlaceTrackInternal(World world, BlockPos pos, EntityPlayer player, EnumFacing side, ItemStack stack) {
        double length = seperated.length;
        EnumFacing playerDir = player.getHorizontalFacing();
        for (int i = 1; i < length; i++) {
            BlockPos toCheck = pos.offset(playerDir, i);
            if (!world.isAirBlock(toCheck)) return EnumTrackRequirement.SPACE_ABOVE;
            if (!world.isAirBlock(toCheck.up())) return EnumTrackRequirement.SPACE_ABOVE;
            if (i < length / 2) {
                IBlockState below = world.getBlockState(toCheck.down());
                if (!below.getBlock().isSideSolid(below, world, toCheck.down(), EnumFacing.UP)) {
                    return EnumTrackRequirement.GROUND_BELOW;
                }
            }
        }
        return null;
    }
}
