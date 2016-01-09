package alexiil.mods.traincraft.block;

import java.util.Locale;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathStraight;
import alexiil.mods.traincraft.api.Train;
import alexiil.mods.traincraft.api.TrainCraftAPI;
import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.entity.EntityRollingStockPulled;

public class BlockStraightTrack extends BlockAbstractTrack {
    public enum EnumDirection implements IStringSerializable {
        // AXIS ALIGNED SECTIONS
        NORTH_SOUTH,
        EAST_WEST,
        // DIAGONAL DIRECTIONS
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST;

        private TrackPathStraight path;

        static {
            BlockPos creator = new BlockPos(0, 0, 0);

            Vec3 north = new Vec3(0.5, 0, 0);
            Vec3 south = new Vec3(0.5, 0, 1);
            Vec3 west = new Vec3(0, 0, 0.5);
            Vec3 east = new Vec3(1, 0, 0.5);

            NORTH_SOUTH.path = new TrackPathStraight(north, south, creator);
            EAST_WEST.path = new TrackPathStraight(east, west, creator);

            NORTH_EAST.path = new TrackPathStraight(north, east, creator);
            NORTH_WEST.path = new TrackPathStraight(north, west, creator);
            SOUTH_EAST.path = new TrackPathStraight(south, east, creator);
            SOUTH_WEST.path = new TrackPathStraight(south, west, creator);
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final PropertyEnum<EnumDirection> TRACK_DIRECTION = PropertyEnum.create("facing", EnumDirection.class);

    private static final float HEIGHT = 0.125f;
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, HEIGHT, 1);

    public BlockStraightTrack() {
        super(TRACK_DIRECTION);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY,
            float hitZ) {
        // Temp
        if (world.isRemote) return true;
        EntityRollingStockBase entity = new EntityRollingStockPulled(world);
        ITrackPath path = paths(world, pos, state)[0];
        entity.setPosition(path.start().xCoord, path.start().yCoord, path.start().zCoord);
        world.spawnEntityInWorld(entity);
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
        setBlockBounds(0, 0, 0, 1, HEIGHT, 1);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        EnumDirection dir = state.getValue(TRACK_DIRECTION);
        ITrackPath path = dir.path.offset(pos);
        return new ITrackPath[] { path };
    }
}