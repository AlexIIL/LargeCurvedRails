package alexiil.mc.mod.traincraft.block;

import java.util.*;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.api.track.behaviour.BehaviourWrapper;
import alexiil.mc.mod.traincraft.api.track.path.ITrackPath;

/** This "points" to a different block that contains all of the actual information regarding the track path. */
public class BlockTrackPointer extends BlockAbstractTrackSingle {
    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, TRACK_HEIGHT, 1);

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

        public static final EnumOffset[] VALUES = values();

        public final BlockPos offset;
        private final String dispName;

        private EnumOffset(int x, int y, int z) {
            this.offset = new BlockPos(x, y, z);
            dispName = name().replace("_", "_with_").replace("N", "_negative_").replace("P", "_positive_").toLowerCase(Locale.ROOT);
            OFFSET_MAP.put(offset, this);
        }

        public static EnumOffset fromMeta(int meta) {
            return VALUES[meta & 15];
        }

        @Override
        public String getName() {
            return dispName;
        }
    }

    private static final Map<BlockPos, EnumOffset> OFFSET_MAP = new HashMap<>();

    public static final PropertyEnum<EnumOffset> PROP_OFFSET = PropertyEnum.create("offset", EnumOffset.class);
    /** Many more tries than are technically needed, but this makes sure that even if we add very long tracks in the
     * future they are all accounted for. */
    private static final int MAX_TRIES = 90;

    public BlockTrackPointer() {}

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PROP_OFFSET);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PROP_OFFSET).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(PROP_OFFSET, EnumOffset.fromMeta(meta));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    /** Attempts to find the master for this block. If it cannot be found the null is returned. */
    public BlockPos master(World world, BlockPos pos, IBlockState state) {
        try {
            return findMaster(world, pos, state);
        } catch (@SuppressWarnings("unused") IllegalPathException e) {
            // Ignore it, we will need to to update at somepoint.
        }
        return null;
    }

    @Override
    public BehaviourWrapper singleBehaviour(World world, BlockPos pos, IBlockState state) {
        try {
            BlockPos master = findMaster(world, pos, state);
            IBlockState masterState = world.getBlockState(master);
            if (masterState.getBlock() instanceof BlockTrackSeperated) {
                BlockTrackSeperated seperated = (BlockTrackSeperated) masterState.getBlock();
                BehaviourWrapper behaviour = seperated.singleBehaviour(world, master, masterState);
                return behaviour;
            }
            // FIXME: is this right?
//            world.notifyBlockUpdate(pos, state, state, 0);
            return null;
        } catch (IllegalPathException e) {
            // Only update it if it was an actual world
            if (world != null) {
                List<BlockPos> illegalPositions = e.path;
                // FIXME: is this right?
//                illegalPositions.forEach(p -> world.notifyBlockUpdate(p, state, state, 0));
            }
            return null;
        }
    }

    // @Override
    public ITrackPath[] paths(World world, BlockPos pos, IBlockState state) {
        try {
            BlockPos master = findMaster(world, pos, state);
            IBlockState masterState = world.getBlockState(master);
            if (masterState.getBlock() instanceof BlockTrackSeperated) {
                // BlockTrackSeperated seperated = (BlockTrackSeperated) masterState.getBlock();
                // return seperated.paths(world, master, masterState);
            }
            // FIXME: is this right?
//            world.notifyBlockUpdate(pos, state, state, 0);
            return new ITrackPath[0];
        } catch (IllegalPathException e) {
            List<BlockPos> illegalPositions = e.path;
            // FIXME: is this right?
//            illegalPositions.forEach(p -> world.notifyBlockUpdate(p, state, state, 0));
            return new ITrackPath[0];
        }
    }

    protected BlockPos findMaster(World world, BlockPos pos, IBlockState state) throws IllegalPathException {
        BlockPos toTry = pos;
        List<BlockPos> tried = new ArrayList<>();
        int i = 0;
        while (i++ < MAX_TRIES) {
            IBlockState tryState = world.getBlockState(toTry);
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
                // Only check if its a slave offset as we already know that we are a valid slave for it.
                if (seperated.isSlaveOffset(world, toTry, tryState, pos)) return toTry;
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
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing side) {
        return true;
    }

    // @Override
    // public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
    // if (world.isRemote) return;
    // try {
    // BlockPos master = findMaster(world, pos, state);
    // /* We might have just recieved a block break event- however we cannot use "onBlockBreak" as the block has
    // * already happened at that point */
    // world.notifyBlockOfStateChange(master, this);
    // return;
    // } catch (IllegalPathException e) {
    // world.setBlockToAir(pos);
    // }
    // }
}
