package alexiil.mods.traincraft.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.tile.TileTrackExtraData;

public class BlockTrackMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTrackExtraData();
    }
}
