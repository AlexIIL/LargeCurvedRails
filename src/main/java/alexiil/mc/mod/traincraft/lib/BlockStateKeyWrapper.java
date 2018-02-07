package alexiil.mc.mod.traincraft.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateKeyWrapper {
    public final IBlockState state;
    private final IExtendedBlockState extended;
    private final int hashCode;

    public BlockStateKeyWrapper(IBlockState state) {
        this.state = state;
        this.extended = state instanceof IExtendedBlockState ? (IExtendedBlockState) state : null;
        this.hashCode = computeHashCode();
    }

    private int computeHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        for (Comparable<?> value : state.getProperties().values()) {
            builder.append(value);
        }

        if (extended != null) {
            for (Optional<?> val : extended.getUnlistedProperties().values()) {
                builder.append(val);
            }
        }
        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        BlockStateKeyWrapper o = (BlockStateKeyWrapper) obj;
        if (state == o.state) return true;
        if (o.extended == null && extended != null || extended == null && o.extended != null) return false;

        List<IProperty> props = new ArrayList<>();
        props.addAll(state.getProperties().keySet());
        for (IProperty prop : props) {
            if (!o.state.getProperties().containsKey(prop)) return false;
        }
        for (IProperty prop : o.state.getProperties().keySet()) {
            if (!props.contains(prop)) return false;

            Comparable val = state.getValue(prop);
            Comparable other = o.state.getValue(prop);
            if (!val.equals(other)) return false;
        }

        if (extended != null) {
            List<IUnlistedProperty> unlistedProps = new ArrayList<>();
            unlistedProps.addAll(extended.getUnlistedNames());
            for (IUnlistedProperty prop : unlistedProps) {
                if (!o.state.getProperties().containsKey(prop)) return false;
            }
            for (IUnlistedProperty prop : o.extended.getUnlistedNames()) {
                if (!unlistedProps.contains(prop)) return false;

                Object val = extended.getValue(prop);
                Object other = o.extended.getValue(prop);
                if (!val.equals(other)) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
