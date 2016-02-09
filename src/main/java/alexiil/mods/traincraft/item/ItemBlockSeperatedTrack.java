package alexiil.mods.traincraft.item;

import java.util.HashMap;
import java.util.List;
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

import alexiil.mods.traincraft.block.BlockTrackPointer;
import alexiil.mods.traincraft.block.BlockTrackPointer.EnumOffset;
import alexiil.mods.traincraft.block.BlockTrackSeperated;
import alexiil.mods.traincraft.block.TCBlocks;

public abstract class ItemBlockSeperatedTrack<T extends BlockTrackSeperated> extends ItemBlockTrainCraft {
    protected final T seperated;

    public ItemBlockSeperatedTrack(T block) {
        super(block);
        this.seperated = block;
    }

    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = new HashMap<>();
        List<BlockPos> offsets = seperated.getSlaveOffsets(targetState);
        for (BlockPos p : offsets) {
            setters.put(p, (world, pos) -> {
                EnumOffset offset = calculateOffsetTo(p, offsets, BlockPos.ORIGIN);
                world.setBlockState(pos, TCBlocks.TRACK_POINTER.getBlock().getDefaultState().withProperty(BlockTrackPointer.PROP_OFFSET, offset));
            });
        }
        setters.put(BlockPos.ORIGIN, (world, pos) -> {
            world.setBlockState(pos, targetState);
        });
        return setters;
    }

    private static EnumOffset calculateOffsetTo(BlockPos from, List<BlockPos> via, BlockPos to) {
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

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        IBlockState targetState = targetState(world, pos, player, stack, side, hitX, hitY, hitZ);
        Map<BlockPos, IBlockSetter> setters = getTrackBlockSetters(targetState, stack);

        for (BlockPos p : setters.keySet()) {
            EnumTrackRequirement req = canPlaceTrack(world, pos.add(p), player, side, stack, pos);
            if (req != null) return false;
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

    protected EnumTrackRequirement canPlaceTrack(World world, BlockPos pos, BlockPos origin) {
        IBlockState below = world.getBlockState(pos.down());
        if (!below.getBlock().isSideSolid(world, pos.down(), EnumFacing.UP)) return EnumTrackRequirement.GROUND_BELOW;
        if (!world.isAirBlock(pos.up())) return EnumTrackRequirement.SPACE_ABOVE;
        return null;
    }

    protected abstract IBlockState targetState(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side, float hitX,
            float hitY, float hitZ);

    public interface IBlockSetter {
        void placeBlockAt(World world, BlockPos pos);
    }

    public enum EnumTrackRequirement {
        GROUND_BELOW,
        SPACE_ABOVE,
        OTHER
    }
}
