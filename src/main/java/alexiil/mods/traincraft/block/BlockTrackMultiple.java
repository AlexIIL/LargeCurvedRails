package alexiil.mods.traincraft.block;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.tile.TileTrackMultiplePoints;
import alexiil.mods.traincraft.tile.TileTrackMultiple;

public class BlockTrackMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    public static final PropertyBool TICKABLE = PropertyBool.create("tickable");
    public static final PropertyBool POINTS = PropertyBool.create("points");

    public BlockTrackMultiple() {
        super(TICKABLE, POINTS);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        return new ITrackPath[0];
    }

    @Override
    public Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state) {
        TileEntity tile = access.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple mult = (TileTrackMultiple) tile;
            return mult.getBehavioursNonStateful();
        }
        return Collections.emptyList();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        IBlockState state = getStateFromMeta(meta);
        if (state.getValue(TICKABLE)) return new TileTrackMultiplePoints();
        return new TileTrackMultiple();
    }
}
