package alexiil.mods.traincraft.block;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.lib.MCObjectUtils;
import alexiil.mods.traincraft.api.track.behaviour.TrackBehaviour;
import alexiil.mods.traincraft.api.track.path.ITrackPath;
import alexiil.mods.traincraft.tile.TileTrackMultiplePointer;

public class BlockTrackPointerMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    public BlockTrackPointerMultiple(IProperty<?>... properties) {
        super(properties);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileTrackMultiplePointer();
    }

    @Override
    public Collection<TrackBehaviour> behaviours(IBlockAccess access, BlockPos pos, IBlockState state) {
        return Collections.emptyList();
    }

    @Override
    public TrackBehaviour currentBehaviour(IBlockAccess access, BlockPos pos, IBlockState state, Vec3 from) {
        for (TrackBehaviour behaviour : behaviours(access, pos, state)) {
            ITrackPath path = behaviour.getPath(access, pos, state);
            if (MCObjectUtils.equals(from, path.start())) return behaviour;
            if (MCObjectUtils.equals(from, path.end())) return behaviour;
        }
        return null;
    }
}
