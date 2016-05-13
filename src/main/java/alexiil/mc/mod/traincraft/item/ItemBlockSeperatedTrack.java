package alexiil.mc.mod.traincraft.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.TrackPlacer;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.behaviour.TrackBehaviour.TrackBehaviourStateful;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer.EnumOffset;
import alexiil.mc.mod.traincraft.block.BlockTrackSeperated;
import alexiil.mc.mod.traincraft.block.TCBlocks;

public abstract class ItemBlockSeperatedTrack<T extends BlockTrackSeperated> extends ItemBlockTrack {
    public final T seperated;

    public ItemBlockSeperatedTrack(T block) {
        super(block);
        this.seperated = block;
    }

    @Override
    protected Map<BlockPos, IBlockSetter> getTrackBlockSetters(IBlockState targetState, ItemStack stack) {
        Map<BlockPos, IBlockSetter> setters = new HashMap<>();
        Set<BlockPos> offsets = seperated.getSlaveOffsets(targetState);
        for (BlockPos p : offsets) {
            if (p.equals(BlockPos.ORIGIN)) continue;
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

    // public EnumTrackRequirement canPlaceTrack(World world, BlockPos pos) {
    //
    // }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            IBlockState newState) {
        IBlockState targetState = targetState(world, pos, player, stack, side, hitX, hitY, hitZ);

        Set<BlockPos> slaves = getTrackBlockSetters(targetState, stack).keySet();

        if (!TrackPlacer.INSTANCE.canPlaceSlaves(slaves, world, pos)) return false;
        if (canPlaceTrack(world, pos, player, side, stack, pos) == null) {
            world.setBlockState(pos, targetState, 2);
            BehaviourWrapper wrapper = seperated.singleBehaviour(world, pos, targetState);
            if (wrapper == null) {
                world.setBlockToAir(pos);
                return false;
            }
            TrackPlacer.INSTANCE.placeSlaves(wrapper.behaviour(), world, pos);
            for (BlockPos p : slaves) {
                world.notifyBlockOfStateChange(pos.add(p), Blocks.air);
            }
            return true;
        } else {
            TrackBehaviourStateful stateful = statefulState(world, pos, player, stack, side, hitX, hitY, hitZ);
            if (stateful == null) return false;
            if (!TrackPlacer.INSTANCE.tryPlaceTrackAndSlaves(stateful, world, pos)) return false;
            for (BlockPos p : slaves) {
                world.notifyBlockOfStateChange(pos.add(p), Blocks.air);
            }
            return true;
        }
    }
}
