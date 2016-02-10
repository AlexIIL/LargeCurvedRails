package alexiil.mods.traincraft.block;

import java.util.*;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.track.ITrackPath;

/** This "points" to a different block that contains all of the actual information regarding the track path. */
public class BlockTrackPointer extends BlockAbstractTrack {
    public enum EnumOffset implements IStringSerializable {
        // @formatter:off
        /** 0 */ XN1    (-1, 0, 0),
        /** 1 */ XP1    ( 1, 0, 0),
        /** 2 */ YN1    ( 0,-1, 0),
        /** 3 */ YP1    ( 0, 1, 0),
        /** 4 */ ZN1    ( 0, 0,-1),
        /** 5 */ ZP1    ( 0, 0, 1),
        /** 6 */ XN2    (-2, 0, 0),
        /** 7 */ XP2    ( 2, 0, 0),
        /** 8 */ ZN2    ( 0, 0,-2),
        /** 9 */ ZP2    ( 0, 0, 2),
        /** A */ XN1_ZN1(-1, 0,-1),
        /** B */ XP1_ZN1( 1, 0,-1),
        /** C */ XN1_ZP1(-1, 0, 1),
        /** D */ XP1_ZP1( 1, 0, 1);
        /** E */ // UNUSED
        /** D */ // UNUSED
        // @formatter:on

        public final BlockPos offset;
        private final String dispName;

        private EnumOffset(int x, int y, int z) {
            this.offset = new BlockPos(x, y, z);
            dispName = name().replace("_", "_with_").replace("N", "_negative_").replace("P", "_positive_").toLowerCase(Locale.ROOT);
            OFFSET_MAP.put(offset, this);
        }

        @Override
        public String getName() {
            return dispName;
        }
    }

    private static final Map<BlockPos, EnumOffset> OFFSET_MAP = new HashMap<>();

    public static final PropertyEnum<EnumOffset> PROP_OFFSET = PropertyEnum.create("offset", EnumOffset.class);
    /** Many more tries than are technically needed, but this makes sure that */
    private static final int MAX_TRIES = 90;

    protected BlockTrackPointer(IProperty<?>... properties) {
        super(properties);
    }

    public BlockTrackPointer() {
        super(PROP_OFFSET);
    }

    @Override
    public ITrackPath[] paths(IBlockAccess access, BlockPos pos, IBlockState state) {
        try {
            BlockPos master = findMaster(access, pos, state);
            IBlockState masterState = access.getBlockState(master);
            if (masterState.getBlock() instanceof BlockTrackSeperated) {
                BlockTrackSeperated seperated = (BlockTrackSeperated) masterState.getBlock();
                return seperated.paths(access, master, masterState);
            }
            if (access instanceof World) {
                ((World) access).markBlockForUpdate(pos);
            }
            return new ITrackPath[0];
        } catch (IllegalPathException e) {
            // Only update it if it was an actual world
            if (access instanceof World) {
                World world = (World) access;
                List<BlockPos> illegalPositions = e.path;
                illegalPositions.forEach(p -> world.markBlockForUpdate(p));
            }
            return new ITrackPath[0];
        }
    }

    protected BlockPos findMaster(IBlockAccess access, BlockPos pos, IBlockState state) throws IllegalPathException {
        BlockPos toTry = pos;
        List<BlockPos> tried = new ArrayList<>();
        int i = 0;
        while (i++ < MAX_TRIES) {
            IBlockState tryState = access.getBlockState(toTry);
            if (tryState.getBlock() == this) {
                EnumOffset offset = tryState.getValue(PROP_OFFSET);
                toTry = toTry.add(offset.offset);
                if (tried.contains(toTry)) {
                    /* Somehow we have gone back around and tried to test a block that points back to ourselves...
                     * something is wrong somewhere. */
                    throw new IllegalPathException(tried);
                }
                tried.add(toTry);
            } else if (tryState.getBlock() instanceof BlockTrackSeperated) {
                BlockTrackSeperated seperated = (BlockTrackSeperated) tryState.getBlock();
                if (seperated.isSlave(access, toTry, tryState, pos, state)) return toTry;
            } else {
                /* Somehow this block doesn't point to a proper block. */
                throw new IllegalPathException(tried);
            }
        }
        throw new IllegalPathException(tried);
    }

    @SuppressWarnings("serial")
    protected static class IllegalPathException extends Exception {
        private final List<BlockPos> path;

        public IllegalPathException(List<BlockPos> path) {
            this.path = path;
        }
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return true;
    }
}
