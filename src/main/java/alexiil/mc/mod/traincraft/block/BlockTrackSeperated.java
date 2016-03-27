package alexiil.mc.mod.traincraft.block;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.TrainCraft;
import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.block.BlockTrackPointer.IllegalPathException;
import alexiil.mc.mod.traincraft.tile.TileTrackMultiple;

public abstract class BlockTrackSeperated extends BlockAbstractTrackSingle {
    public BlockTrackSeperated(IProperty<?>... properties) {
        super(properties);
    }

    /** Tests to see if the given block is a slave to this master block. This tests both the offset position relative to
     * this {@link #getSlaveOffsets(IBlockState)} and whether the target slave is actually a valid slave that points to
     * this. */
    public final boolean isSlave(World world, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState) {
        if (masterPos.equals(slavePos)) return true;

        if (!isSlaveOffset(world, masterPos, masterState, slavePos)) return false;

        if (slaveState.getBlock() instanceof BlockTrackPointer) {
            BlockTrackPointer pointer = (BlockTrackPointer) slaveState.getBlock();
            try {
                BlockPos master = pointer.findMaster(world, slavePos, slaveState);
                if (master.equals(masterPos)) return true;
                TrainCraft.trainCraftLog.info(master + " != " + masterPos);
            } catch (IllegalPathException e) {
                e.printStackTrace();
                return false;
            }
        } else if (slaveState.getBlock() instanceof BlockTrackMultiple) {
            TileEntity tile = world.getTileEntity(slavePos);
            if (tile instanceof TileTrackMultiple) {
                TileTrackMultiple multi = (TileTrackMultiple) tile;
                for (BehaviourWrapper wrapper : multi.getWrappedBehaviours()) {
                    if (wrapper.pos().equals(masterState)) return true;
                }
            }
        }
        return false;
    }

    /** Checks to see if the given block position is an offset of this one. */
    public final boolean isSlaveOffset(World world, BlockPos masterPos, IBlockState masterState, BlockPos slavePos) {
        return getSlaveOffsets(masterState).contains(slavePos.subtract(masterPos));
    }

    public abstract Set<BlockPos> getSlaveOffsets(IBlockState state);

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (world.isRemote) return;
        for (BlockPos slave : getSlaveOffsets(state)) {
            if (!isSlave(world, pos, state, pos.add(slave), world.getBlockState(pos.add(slave)))) {
                world.destroyBlock(pos, true);
                return;
            }
        }
    }
}
