package alexiil.mods.traincraft;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.train.IRollingStockType;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;

public enum TrainRegistry {
    INSTANCE;

    private final Map<ResourceLocation, IRollingStockType> trains = new HashMap<>();

    public void registerTrain(IRollingStockType factory) {
        ResourceLocation uniqueId = factory.uniqueID();
        if (trains.containsKey(uniqueId)) throw new IllegalStateException("Tried to re register the ID " + uniqueId);
        trains.put(uniqueId, factory);
    }

    public IRollingStockType getFactory(ResourceLocation location) {
        return trains.get(location);
    }

    /** NOTE: This is GOOGLE's function as forge doesn't have java 8! */
    @SideOnly(Side.CLIENT)
    public Function<ResourceLocation, TextureAtlasSprite> getSpriteFunction() {
        return this::getSprite;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSprite(ResourceLocation location) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
    }
}
