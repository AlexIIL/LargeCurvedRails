package alexiil.mods.traincraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class BlockTrackSeperated extends BlockAbstractTrack {
    public BlockTrackSeperated(IProperty<?>... properties) {
        super(properties);
    }

    /** Tests to see if the given block is a slave to this master block. */
    public abstract boolean isSlave(IBlockAccess access, BlockPos masterPos, IBlockState masterState, BlockPos slavePos, IBlockState slaveState);
}
