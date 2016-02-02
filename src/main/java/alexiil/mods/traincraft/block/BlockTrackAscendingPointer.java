package alexiil.mods.traincraft.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.TrainCraft;
import alexiil.mods.traincraft.block.BlockTrackAscendingPointer.BlockShownSet.BlockShown;

public class BlockTrackAscendingPointer extends BlockTrackPointer {
    public static class BlockShownSet implements IProperty<BlockShown> {
        private final List<BlockShown> shown = new ArrayList<>();

        // BNF:
        // states ::= <singlestate>|<singlestate><states>
        // singlestate ::= IBlockState|Block|<definingarray>
        // definingarray ::= Block,Class<Enum>|Block<enumvalue>
        // enumvalue ::= Enum|Enum<enumvalue>
        public BlockShownSet(Object... states) {
            if (states.length <= 0) throw new IllegalArgumentException("Too few blocks(" + states.length + "), need at least 1!");
            if (states.length > 16) throw new IllegalArgumentException("Too many blocks(" + states.length + "), only 16 allowed (max)");
            for (Object state : states) {
                if (state instanceof IBlockState) {
                    shown.add(new BlockShown((IBlockState) state));
                } else if (state instanceof Block) {
                    shown.add(new BlockShown(((Block) state).getDefaultState()));
                } else if (state instanceof Object[]) {
                    Object[] arr = (Object[]) state;
                    if (arr.length <= 1) throw new IllegalStateException("You must have at least 2 objects!");
                    if (!(arr[0] instanceof Block)) throw new IllegalStateException("The first array element must be a block!");
                    Block b = (Block) arr[0];
                    if (arr[1] instanceof Class) {
                        Class c = (Class) arr[1];
                        if (!c.isEnum()) throw new IllegalStateException("Must be a class of an enum!");
                        boolean added = false;
                        for (IProperty p : b.getDefaultState().getPropertyNames()) {
                            if (p.getValueClass().isAssignableFrom(c)) {
                                for (Object value : c.getEnumConstants()) {
                                    IBlockState state2 = withPropertyUnsafe(b.getDefaultState(), p, value);
                                    shown.add(new BlockShown(state2));
                                }
                                added = true;
                                break;
                            }
                        }
                        if (!added) throw new IllegalArgumentException("Did not find the corresponding class type!");
                    } else {
                        for (int i = 1; i < arr.length; i++) {
                            Object value = arr[i];
                            for (IProperty p : b.getDefaultState().getPropertyNames()) {
                                if (p.getValueClass().isAssignableFrom(value.getClass())) {
                                    IBlockState state2 = withPropertyUnsafe(b.getDefaultState(), p, value);
                                    shown.add(new BlockShown(state2));
                                    value = null;
                                    break;
                                }
                            }
                            if (value != null) throw new IllegalStateException("Did not find a property for the value " + value);
                        }
                    }
                } else throw new IllegalArgumentException("Unknown class type!");
            }
        }

        private static IBlockState withPropertyUnsafe(IBlockState state, IProperty property, Object value) {
            return withPropertyUnsafeInternal(state, property, value);
        }

        private static <T extends Comparable<T>, V extends T> IBlockState withPropertyUnsafeInternal(IBlockState state, IProperty<T> property,
                Object value) {
            return state.withProperty(property, (V) value);
        }

        @Override
        public String getName() {
            return "material";
        }

        @Override
        public Collection<BlockShown> getAllowedValues() {
            return shown;
        }

        @Override
        public Class<BlockShown> getValueClass() {
            return BlockShown.class;
        }

        @Override
        public String getName(BlockShown value) {
            return value.name;
        }

        public class BlockShown implements IStringSerializable, Comparable<BlockShown> {
            public final IBlockState state;
            private final String name;

            public BlockShown(IBlockState state) {
                this.state = state;
                if (state == null) {
                    name = "__UNUSED__#" + (shown.size() - 1);
                } else {
                    name = state.toString();
                }
                TrainCraft.trainCraftLog.info("Created a new block shown (" + name + ") for set #" + System.identityHashCode(BlockShownSet.this));
            }

            @Override
            public int compareTo(BlockShown o) {
                if (o == null) return 0;
                if (shown.indexOf(o) == -1) return 0;
                return shown.indexOf(this) - shown.indexOf(o);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }

    public BlockTrackAscendingPointer(BlockShownSet blocksShown) {
        super(blocksShown);
    }

    @Override
    protected BlockPos findMaster(IBlockAccess access, BlockPos pos, IBlockState state) throws IllegalPathException {
        for (EnumFacing face : EnumFacing.HORIZONTALS) {
            BlockPos masterPos = pos.offset(face);
            IBlockState masterState = access.getBlockState(masterPos);
            Block masterBlock = masterState.getBlock();
            if (masterBlock instanceof BlockTrackAscending) {
                BlockTrackAscending asc = (BlockTrackAscending) masterBlock;
                if (asc.isSlave(access, masterPos, masterState, pos, state)) return masterPos;
            }
        }
        // We failed. Update this block to remove it.
        return pos;
    }
}
