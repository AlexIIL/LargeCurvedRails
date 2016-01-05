package alexiil.mods.traincraft.block;

import java.util.Locale;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.ITrackPath;
import alexiil.mods.traincraft.api.TrackPathStraight;

public class BlockStraightTrack extends BlockAbstractTrack {
    public enum EnumDirection implements IStringSerializable {
        NORTH_SOUTH,
        EAST_WEST;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final PropertyEnum<EnumDirection> TRACK_DIRECTION = PropertyEnum.create("facing", EnumDirection.class);

    public BlockStraightTrack() {
        super(TRACK_DIRECTION);
    }

    @Override
    public ITrackPath path(IBlockAccess access, BlockPos pos, EnumFacing approximateDirection) {
        IBlockState state = access.getBlockState(pos);
        if (state.getBlock() != this) return null;
        EnumDirection dir = state.getValue(TRACK_DIRECTION);
        if (dir == EnumDirection.NORTH_SOUTH) {
            if (approximateDirection == EnumFacing.NORTH) {
                Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 1);
                Vec3 end = start.addVector(0, 0, -1);
                return new TrackPathStraight(start, end);
            } else {
                /* Assume its north. If its not north then the tracks haven't been connected properly and the train
                 * manager will derail the train because of the fast change in direction. */
                Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ());
                Vec3 end = start.addVector(0, 0, 1);
                return new TrackPathStraight(start, end);
            }
        } else {
            if (approximateDirection == EnumFacing.WEST) {
                Vec3 start = new Vec3(pos.getX() + 1, pos.getY(), pos.getZ() + 0.5);
                Vec3 end = start.addVector(-1, 0, 0);
                return new TrackPathStraight(start, end);
            } else {
                /* Assume its east. If its not east then the tracks haven't been connected properly and the train
                 * manager will derail the train because of the fast change in direction. */
                Vec3 start = new Vec3(pos.getX(), pos.getY(), pos.getZ() + 0.5);
                Vec3 end = start.addVector(1, 0, 0);
                return new TrackPathStraight(start, end);
            }
        }
    }
}
