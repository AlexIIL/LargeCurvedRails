package alexiil.mods.traincraft.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import alexiil.mods.traincraft.block.BlockTrackPointer;
import alexiil.mods.traincraft.block.BlockTrackPointer.EnumOffset;
import alexiil.mods.traincraft.block.BlockTrackSeperated;
import alexiil.mods.traincraft.block.TCBlocks;

public abstract class ItemBlockSeperatedTrack<T extends BlockTrackSeperated> extends ItemBlockTrack {
    protected final T seperated;

    public ItemBlockSeperatedTrack(T block) {
        super(block);
        this.seperated = block;
    }

    @Override
    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = new HashMap<>();
        List<BlockPos> offsets = seperated.getSlaveOffsets(targetState);
        for (BlockPos p : offsets) {
            setters.put(p, (world, pos) -> {
                EnumOffset offset = calculateOffsetTo(p, offsets, BlockPos.ORIGIN);
                world.setBlockState(pos, TCBlocks.TRACK_POINTER.getBlock().getDefaultState().withProperty(BlockTrackPointer.PROP_OFFSET, offset), 2);
            });
        }
        setters.put(BlockPos.ORIGIN, (world, pos) -> {
            world.setBlockState(pos, targetState, 2);
        });
        return setters;
    }
}
