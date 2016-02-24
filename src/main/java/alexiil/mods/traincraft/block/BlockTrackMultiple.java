package alexiil.mods.traincraft.block;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mods.traincraft.tile.TileTrackMultiple;
import alexiil.mods.traincraft.tile.TileTrackMultiplePoints;

public class BlockTrackMultiple extends BlockAbstractTrack implements ITileEntityProvider {
    public static final PropertyBool TICKABLE = PropertyBool.create("tickable");
    public static final PropertyBool POINTS = PropertyBool.create("points");

    public BlockTrackMultiple() {
        super(TICKABLE, POINTS);
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
