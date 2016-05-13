package alexiil.mc.mod.traincraft.block;

import java.util.Set;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.track.Curve;

public class BlockTrackCurvedHalf extends BlockTrackSeperated {
    /** Designates whether this track goes in a positive direction after this or a negative direction: if
     * {@link #PROPERTY_FACING} was {@link EnumFacing#NORTH} (-Z) then if this was true this would curve into positive X
     * values. (So it would got NORTH_TO_NORTH_EAST */
    public static final PropertyBool PROPERTY_DIRECTION = PropertyBool.create("direction");
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public final Curve curve;

    public BlockTrackCurvedHalf(Curve curve) {
        this.curve = curve;
        curve.halfBlock = this;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROPERTY_FACING, PROPERTY_DIRECTION);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(PROPERTY_FACING).getHorizontalIndex();
        if (state.getValue(PROPERTY_DIRECTION)) meta |= 4;
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean direction = (meta & 4) == 4;
        EnumFacing face = EnumFacing.getHorizontal(meta & 3);
        return getDefaultState().withProperty(PROPERTY_DIRECTION, direction).withProperty(PROPERTY_FACING, face);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state) {
        return new BehaviourWrapper(curve.halfNative, world, pos);
    }

    @Override
    public Set<BlockPos> getSlaveOffsets(IBlockState state) {
        return curve.halfFactory.getSlaves(state.getValue(PROPERTY_FACING), state.getValue(PROPERTY_DIRECTION));
    }
}
