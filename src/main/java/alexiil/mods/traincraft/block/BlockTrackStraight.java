package alexiil.mods.traincraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.AlignmentFailureException;
import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.entity.EntityRollingStockCart;
import alexiil.mods.traincraft.entity.EntitySmallSteamLocomotive;

public class BlockTrackStraight extends BlockAbstractTrack {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

    public BlockTrackStraight() {
        super(TRACK_DIRECTION);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            EntityLivingBase placer) {
        EnumFacing entFacing = placer.getHorizontalFacing();
        if (entFacing.getAxis() == Axis.X) return getDefaultState().withProperty(TRACK_DIRECTION, EnumDirection.EAST_WEST);
        else return getDefaultState().withProperty(TRACK_DIRECTION, EnumDirection.NORTH_SOUTH);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        // Temp
        if (world.isRemote) return true;
        EntityRollingStockBase entity;
        if (player.isSneaking()) entity = new EntitySmallSteamLocomotive(world);
        else entity = new EntityRollingStockCart(world);
        try {
            entity.alignToBlock(pos);
            world.spawnEntityInWorld(entity);
        } catch (AlignmentFailureException e) {
            // In the future this will display a notification to the player that something went wrong, but for the
            // moment we will leave it as-is
            return false;
        }
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        setBlockBounds(0, 0, 0, 1, TRACK_HEIGHT, 1);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        EnumDirection dir = state.getValue(TRACK_DIRECTION);
        ITrackPath path = dir.path.offset(pos);
        return new ITrackPath[] { path };
    }
}
