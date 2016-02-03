package alexiil.mods.traincraft.property;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.block.properties.IProperty;
import net.minecraft.util.IStringSerializable;

import net.minecraftforge.common.property.IUnlistedProperty;

public class TrainCraftExtendedProperty<T extends Comparable<T> & IStringSerializable> implements IUnlistedProperty<T>, IProperty<T> {
    private final String name;
    private final Class<T> clazz;

    public TrainCraftExtendedProperty(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    // IProperty
    @Override
    public Collection<T> getAllowedValues() {
        return Collections.emptyList();
    }

    @Override
    public Class<T> getValueClass() {
        return clazz;
    }

    @Override
    public String getName(T value) {
        return value.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    // IUnlistedProperty
    @Override
    public boolean isValid(T value) {
        return value != null;
    }

    @Override
    public Class<T> getType() {
        return getValueClass();
    }

    @Override
    public String valueToString(T value) {
        return getName(value);
    }
}
