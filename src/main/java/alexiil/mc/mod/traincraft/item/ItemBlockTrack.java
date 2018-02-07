package alexiil.mc.mod.traincraft.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.TrackPlacer;
import alexiil.mc.mod.traincraft.api.track.ITrackPlacer.EnumTrackRequirement;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer.EnumOffset;

public abstract class ItemBlockTrack extends ItemBlockTrainCraft {
    public ItemBlockTrack(Block block) {
        super(block);
    }

    /* Same as the one in ItemBlock, but has an additional check to not modify the block position if we are looking at a
     * track */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
        EnumFacing side, float hitX, float hitY, float hitZ) {
        // Our addition, we don't care what the defaults are as we know if we are allowed to do this or not.
        boolean replacingTrack = TrackPlacer.INSTANCE.isUpgradableTrack(world, pos);

        if (!block.isReplaceable(world, pos) && !replacingTrack)
        {
            pos = pos.offset(side);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, side, itemstack) && world.mayPlace(this.block, pos, false, side, (Entity)null))
        {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, i, player, hand);

            if (placeBlockAt(itemstack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1))
            {
                iblockstate1 = world.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    
    }

    public static EnumOffset calculateOffsetTo(BlockPos from, Collection<BlockPos> via, BlockPos to) {
        double bestOffsetL = Double.MAX_VALUE;
        EnumOffset bestOffset = null;

        for (BlockPos p : via) {
            for (EnumOffset o : EnumOffset.values()) {
                BlockPos offset = from.add(o.offset);
                if (!offset.equals(p)) continue;
                double dist = offset.distanceSq(to);
                if (dist < bestOffsetL) {
                    bestOffset = o;
                    bestOffsetL = dist;
                }
            }
        }
        if (bestOffset == null) throw new NullPointerException("Did not find a good offset for " + from + " via " + via + " to " + to);
        return bestOffset;
    }

    @SuppressWarnings("static-method")
    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = new HashMap<>();
        setters.put(BlockPos.ORIGIN, (world, pos) -> {
            world.setBlockState(pos, targetState);
        });
        return setters;
    }

    @Override
    public abstract boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState);

    public final EnumTrackRequirement canPlaceTrack(World world, BlockPos pos, EntityPlayer player, EnumFacing side, ItemStack stack) {
        IBlockState currentState = world.getBlockState(pos);
        Block block = currentState.getBlock();
        if (!block.isReplaceable(world, pos)) {
            return EnumTrackRequirement.OTHER;
        } else if (stack.isEmpty()) {
            return EnumTrackRequirement.OTHER;
        } else if (!player.canPlayerEdit(pos, side, stack)) {
            return EnumTrackRequirement.OTHER;
        } else if (!world.mayPlace(this.block, pos, false, side, (Entity) null)) {
            return EnumTrackRequirement.OTHER;
        }
        return canPlaceTrackInternal(world, pos, player, side, stack);
    }

    protected EnumTrackRequirement canPlaceTrackInternal(World world, BlockPos pos, EntityPlayer player, EnumFacing side, ItemStack stack) {
        IBlockState below = world.getBlockState(pos.down());
        if (!below.getBlock().isSideSolid(below, world, pos.down(), EnumFacing.UP)) return EnumTrackRequirement.GROUND_BELOW;
        if (!world.isAirBlock(pos.up())) return EnumTrackRequirement.SPACE_ABOVE;
        return null;
    }

    protected abstract IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ);

    public abstract TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ);

    public abstract ITrackPath getPreviewPath(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ);

    public interface IBlockSetter {
        void placeBlockAt(World world, BlockPos pos);
    }
}
