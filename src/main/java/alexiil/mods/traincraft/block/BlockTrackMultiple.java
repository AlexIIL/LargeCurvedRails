package alexiil.mods.traincraft.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.IExtendedBlockState;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.api.track.model.TrackModelProperty;
import alexiil.mods.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mods.traincraft.tile.TileTrackMultiple;
import alexiil.mods.traincraft.tile.TileTrackMultiplePoints;

public class BlockTrackMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    public static final PropertyBool TICKABLE = PropertyBool.create("tickable");
    public static final PropertyBool POINTS = PropertyBool.create("points");

    public BlockTrackMultiple() {
        super(TICKABLE, POINTS, TrackModelProperty.INSTANCE);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
        TileEntity tile = access.getTileEntity(pos);
        if (!(tile instanceof TileTrackMultiple) || !(state instanceof IExtendedBlockState)) return state;
        TileTrackMultiple mult = (TileTrackMultiple) tile;
        IExtendedBlockState ext = (IExtendedBlockState) state;
        List<TrackModelWrapper> allWrappers = new ArrayList<>();
        for (BehaviourWrapper wrapper : mult.getWrappedBehaviours()) {
            allWrappers.add(new TrackModelWrapper(wrapper.getPath(), wrapper.behaviour().getModel()));
        }
        ext = ext.withProperty(TrackModelProperty.INSTANCE, allWrappers.toArray(new TrackModelWrapper[allWrappers.size()]));
        return ext;
    }

    @Override
    public BehaviourWrapper currentBehaviour(World world, BlockPos pos, IBlockState state, Vec3 from) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple mult = (TileTrackMultiple) tile;
            return mult.currentBehaviour(from);
        }
        return null;
    }

    @Override
    public Collection<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple mult = (TileTrackMultiple) tile;
            return mult.getWrappedBehaviours();
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
