package alexiil.mods.traincraft.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import alexiil.mods.traincraft.TrackPlacer;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mods.traincraft.block.BlockTrackPointer.EnumOffset;

public abstract class ItemBlockTrack extends ItemBlockTrainCraft {
    public ItemBlockTrack(Block block) {
        super(block);
    }

    /* Same as the one in ItemBlock, but has an additional check to not modify the block position if we are looking at a
     * track */
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();

        // Our addition, we don't care what the defaults are as we know if we are allowed to do this or not.
        boolean replacingTrack = TrackPlacer.INSTANCE.isUpgradableTrack(world, pos);

        if (!block.isReplaceable(world, pos) && !replacingTrack) {
            pos = pos.offset(side);
        }

        if (stack.stackSize == 0) {
            return false;
        } else if (!playerIn.canPlayerEdit(pos, side, stack)) {
            return false;
        } else if (replacingTrack || world.canBlockBePlaced(this.block, pos, false, side, (Entity) null, stack)) {
            int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = this.block.onBlockPlaced(world, pos, side, hitX, hitY, hitZ, i, playerIn);

            if (placeBlockAt(stack, playerIn, world, pos, side, hitX, hitY, hitZ, iblockstate1)) {
                world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, this.block.stepSound.getPlaceSound(),
                        (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
                --stack.stackSize;
            }

            return true;
        } else return false;
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
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {

        IBlockState targetState = targetState(world, pos, player, stack, side, hitX, hitY, hitZ);
        Map<BlockPos, IBlockSetter> setters = getTrackBlockSetters(targetState, stack);

        for (BlockPos p : setters.keySet()) {
            EnumTrackRequirement req = canPlaceTrack(world, pos.add(p), player, side, stack, pos);
            if (req != null) {
                TrackBehaviourStateful stateful = statefulState(world, pos, player, stack, side, hitX, hitY, hitZ);
                if (stateful == null) return false;
                return TrackPlacer.INSTANCE.tryPlaceTrackAndSlaves(stateful, world, pos);
            }
        }

        // Place the blocks
        for (Entry<BlockPos, IBlockSetter> entry : setters.entrySet()) {
            BlockPos p = entry.getKey();
            IBlockSetter setter = entry.getValue();
            setter.placeBlockAt(world, pos.add(p));
        }

        return true;
    }

    public final EnumTrackRequirement canPlaceTrack(World world, BlockPos pos, EntityPlayer player, EnumFacing side, ItemStack stack,
            BlockPos origin) {
        IBlockState currentState = world.getBlockState(pos);
        Block block = currentState.getBlock();
        if (!block.isReplaceable(world, pos)) {
            return EnumTrackRequirement.OTHER;
        } else if (stack.stackSize == 0) {
            return EnumTrackRequirement.OTHER;
        } else if (!player.canPlayerEdit(pos, side, stack)) {
            return EnumTrackRequirement.OTHER;
        } else if (!world.canBlockBePlaced(this.block, pos, false, side, (Entity) null, stack)) {
            return EnumTrackRequirement.OTHER;
        }
        return canPlaceTrack(world, pos, origin);
    }

    @SuppressWarnings("static-method")
    protected EnumTrackRequirement canPlaceTrack(World world, BlockPos pos, BlockPos origin) {
        IBlockState below = world.getBlockState(pos.down());
        if (!below.getBlock().isSideSolid(world, pos.down(), EnumFacing.UP)) return EnumTrackRequirement.GROUND_BELOW;
        if (!world.isAirBlock(pos.up())) return EnumTrackRequirement.SPACE_ABOVE;
        return null;
    }

    protected abstract IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ);

    protected abstract TrackBehaviourStateful statefulState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side,
            float hitX, float hitY, float hitZ);

    public interface IBlockSetter {
        void placeBlockAt(World world, BlockPos pos);
    }

    public enum EnumTrackRequirement {
        GROUND_BELOW,
        SPACE_ABOVE,
        OTHER
    }
}
