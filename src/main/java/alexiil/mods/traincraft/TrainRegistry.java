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

import alexiil.mods.traincraft.entity.EntityRollingStockBase;

public enum TrainRegistry {
    INSTANCE;

    private static final Map<Class<? extends EntityRollingStockBase>, EntityRollingStockBase> trains = new HashMap<>();

    public static void registerTrain(Class<? extends EntityRollingStockBase> stock, String modUniqueName) throws IllegalArgumentException {
        try {
            EntityRollingStockBase base = stock.getConstructor(World.class).newInstance((World) null);
            EntityRegistry.registerModEntity(stock, modUniqueName, 0, Loader.instance().activeModContainer().getMod(), 60, 64, false);
            trains.put(stock, base);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException e) {
            throw new IllegalArgumentException(e);
        }
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
