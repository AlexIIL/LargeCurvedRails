package alexiil.mods.traincraft.property;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BlockStatePropWrapper implements IStringSerializable, Comparable<BlockStatePropWrapper> {
    public final IBlockState state;
    private final String string;

    public BlockStatePropWrapper(IBlockState state) {
        this.state = state;
        this.string = state.toString();
    }

    @Override
    public int compareTo(BlockStatePropWrapper o) {
        int thisMeta = state.getBlock().getMetaFromState(state);
        int thatMeta = o.state.getBlock().getMetaFromState(o.state);
        return thisMeta - thatMeta;
    }

    @Override
    public String getName() {
        return string;
    }
}
