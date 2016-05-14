package alexiil.mc.mod.traincraft.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelProperty;
import alexiil.mc.mod.traincraft.api.track.model.TrackModelWrapper;
import alexiil.mc.mod.traincraft.tile.TileTrackMultiple;
import alexiil.mc.mod.traincraft.tile.TileTrackMultiplePoints;

public class BlockTrackMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    public static final PropertyBool POINTS = PropertyBool.create("points");

    public BlockTrackMultiple() {}

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty<?>[] props = { POINTS };
        IUnlistedProperty<?>[] unlisted = { TrackModelProperty.INSTANCE };
        return new ExtendedBlockState(this, props, unlisted);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean points = (meta & 1) == 1;
        return getDefaultState().withProperty(POINTS, points);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(POINTS)) meta = 1;
        return meta;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
        TileEntity tile = access.getTileEntity(pos);
        if (!(tile instanceof TileTrackMultiple) || !(state instanceof IExtendedBlockState)) return state;
        TileTrackMultiple mult = (TileTrackMultiple) tile;
        IExtendedBlockState ext = (IExtendedBlockState) state;
        List<TrackModelWrapper> allWrappers = new ArrayList<>();
        for (BehaviourWrapper wrapper : mult.getContainedBehaviours()) {
            allWrappers.add(new TrackModelWrapper(wrapper.getPath(), wrapper.behaviour().getModel()));
        }
        ext = ext.withProperty(TrackModelProperty.INSTANCE, allWrappers.toArray(new TrackModelWrapper[allWrappers.size()]));
        return ext;
    }

    @Override
    public BehaviourWrapper currentBehaviour(World world, BlockPos pos, IBlockState state, Vec3d from) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple mult = (TileTrackMultiple) tile;
            return mult.currentBehaviour(from);
        }
        return null;
    }

    @Override
    public Stream<BehaviourWrapper> behaviours(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileTrackMultiple) {
            TileTrackMultiple mult = (TileTrackMultiple) tile;
            return mult.getWrappedBehaviours().stream();
        }
        return Stream.empty();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        IBlockState state = getStateFromMeta(meta);
        if (state.getValue(POINTS)) return new TileTrackMultiplePoints();
        return new TileTrackMultiple();
    }
}
