package alexiil.mods.traincraft.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public abstract class BlockTraincraft extends Block {
    protected final IProperty<?>[] properties;
    protected final IProperty<?>[] nonMetaProperties;
    protected final IUnlistedProperty<?>[] extendedProperties;

    protected final boolean hasExtendedProperties;

    protected final List<IProperty<?>> propertyList;
    protected final Map<Integer, IBlockState> intToState = Maps.newHashMap();
    protected final Map<IBlockState, Integer> stateToInt = Maps.newHashMap();
    protected final BlockState myBlockState;

    // Copied (with minor adjustments) from MY OWN buildcraft code
    public BlockTraincraft(Material material, IProperty<?>... properties) {
        super(material);
        List<IProperty<?>> metas = Lists.newArrayList();
        List<IProperty<?>> nonMetas = Lists.newArrayList();
        List<IUnlistedProperty<?>> infinites = Lists.newArrayList();

        int total = 1;
        boolean hasExtendedProps = false;

        for (IProperty<?> prop : properties) {
            if (prop == null) {
                continue;
            }
            if (prop instanceof IUnlistedProperty<?>) {
                infinites.add((IUnlistedProperty<?>) prop);
                hasExtendedProps = true;
                continue;
            }

            total *= prop.getAllowedValues().size();

            if (total > 16) {
                nonMetas.add(prop);
            } else {
                metas.add(prop);
            }
        }

        this.hasExtendedProperties = hasExtendedProps;

        this.properties = metas.toArray(new IProperty<?>[0]);
        this.nonMetaProperties = nonMetas.toArray(new IProperty<?>[0]);
        this.extendedProperties = infinites.toArray(new IUnlistedProperty<?>[0]);

        this.myBlockState = createBlockState();

        IBlockState defaultState = getBlockState().getBaseState();

        Map<IBlockState, Integer> tempValidStates = Maps.newHashMap();
        tempValidStates.put(defaultState, 0);

        for (IProperty<?> prop : properties) {
            if (prop == null) {
                continue;
            }

            if (prop instanceof IUnlistedProperty<?>) {
                continue;
            }

            List<? extends Comparable> allowedValues = new ArrayList<>(prop.getAllowedValues());
            defaultState = withProperty(defaultState, prop, allowedValues.iterator().next());

            Map<IBlockState, Integer> newValidStates = Maps.newHashMap();
            int mul = metas.contains(prop) ? allowedValues.size() : 1;
            for (Entry<IBlockState, Integer> entry : tempValidStates.entrySet()) {
                int index = 0;
                Collections.sort(allowedValues);
                for (Comparable<?> comp : allowedValues) {
                    int pos = entry.getValue() * mul + index;
                    newValidStates.put(withProperty(entry.getKey(), prop, comp), pos);
                    if (mul > 1) {
                        index++;
                    }
                }
            }
            tempValidStates = newValidStates;
        }

        for (Entry<IBlockState, Integer> entry : tempValidStates.entrySet()) {
            int i = entry.getValue();
            stateToInt.put(entry.getKey(), i);
            if (!intToState.containsKey(i)) {
                intToState.put(i, entry.getKey());
            }
        }
        setDefaultState(defaultState);

        List<IProperty<?>> allProperties = new ArrayList<>();
        allProperties.addAll(metas);
        allProperties.addAll(nonMetas);
        propertyList = Collections.unmodifiableList(allProperties);
    }

    // Generic helper methods, these stop generics from being strange
    @SuppressWarnings("unchecked")
    private IBlockState withProperty(IBlockState state, IProperty prop, Comparable value) {
        return withProperty0(state, prop, value);
    }

    private <V extends Comparable<V>, T extends V> IBlockState withProperty0(IBlockState state, IProperty<V> prop, T value) {
        return state.withProperty(prop, value);
    }

    @Override
    public BlockState getBlockState() {
        return this.myBlockState;
    }

    @Override
    protected BlockState createBlockState() {
        if (properties == null) {
            // Will be overridden later
            return new BlockState(this, new IProperty[] {});
        }

        IProperty[] props = new IProperty[properties.length + nonMetaProperties.length];
        for (int i = 0; i < properties.length; i++) {
            props[i] = properties[i];
        }
        for (int i = 0; i < nonMetaProperties.length; i++) {
            props[properties.length + i] = nonMetaProperties[i];
        }
        if (hasExtendedProperties) {
            return new ExtendedBlockState(this, props, extendedProperties);
        }
        return new BlockState(this, props);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return stateToInt.get(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return intToState.get(meta);
    }
}
