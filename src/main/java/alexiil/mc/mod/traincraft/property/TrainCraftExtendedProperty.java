package alexiil.mc.mod.traincraft.property;

import net.minecraft.util.IStringSerializable;

import net.minecraftforge.common.property.IUnlistedProperty;

public class TrainCraftExtendedProperty<T extends Comparable<T> & IStringSerializable> implements IUnlistedProperty<T> {
    private final String name;
    private final Class<T> clazz;

    public TrainCraftExtendedProperty(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(T value) {
        return value != null;
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    @Override
    public String valueToString(T value) {
        return value.getName();
    }
}
