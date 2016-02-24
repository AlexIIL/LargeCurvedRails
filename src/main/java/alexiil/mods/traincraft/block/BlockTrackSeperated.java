package alexiil.mods.traincraft.block;

import java.util.List;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public abstract class BlockTrackSeperated extends BlockAbstractTrackSingle {
    public BlockTrackSeperated(IProperty<?>... properties) {
        super(properties);
    }

    /** Tests to see if the given block is a slave to this master block. */
    public abstract boolean isSlave(World world, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState);

    public abstract List<BlockPos> getSlaveOffsets(IBlockState state);
}
