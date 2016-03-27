package alexiil.mc.mod.traincraft.lib;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

import alexiil.mc.mod.traincraft.TrainCraft;

public class NBTUtils {

    private static final String NULL_ENUM_STRING = "_NULL";

    public static <E extends Enum<E>> NBTBase serializeEnum(E value) {
        if (value == null) return new NBTTagString(NULL_ENUM_STRING);
        return new NBTTagString(value.name());
    }

    public static <E extends Enum<E>> E deserializeEnum(NBTBase nbt, Class<E> clazz) {
        return deserializeEnum(nbt, clazz, null);
    }

    public static <E extends Enum<E>> E deserializeEnum(NBTBase nbt, Class<E> clazz, E defaultValue) {
        if (nbt instanceof NBTTagString) {
            String value = ((NBTTagString) nbt).getString();
            if (NULL_ENUM_STRING.equals(value)) return defaultValue;
            try {
                return Enum.valueOf(clazz, value);
            } catch (Throwable t) {
                // In case we didn't find the constant
                TrainCraft.trainCraftLog.warn("Tried and failed to read the value(" + value + ") from " + clazz.getSimpleName(), t);
                return null;
            }
        } else if (nbt == null) {
            return defaultValue;
        } else {
            // Don't actually throw as anybody can affect the output NBT, we don't want it to throw just anytime
            TrainCraft.trainCraftLog.warn(new IllegalArgumentException(
                    "Tried to read an enum value when it was not a string! This is probably not good!"));
            return defaultValue;
        }
    }
}
