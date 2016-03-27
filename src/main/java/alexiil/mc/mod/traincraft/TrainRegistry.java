package alexiil.mc.mod.traincraft;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.traincraft.api.train.IRollingStockType;
import alexiil.mc.mod.traincraft.api.train.ITrainRegistry;

public enum TrainRegistry implements ITrainRegistry {
    INSTANCE;

    private final Map<ResourceLocation, IRollingStockType> trains = new HashMap<>();

    @Override
    public void registerTrain(IRollingStockType factory) {
        ResourceLocation uniqueId = factory.uniqueID();
        if (trains.containsKey(uniqueId)) throw new IllegalStateException("Tried to re register the ID " + uniqueId);
        trains.put(uniqueId, factory);
    }
    
    @Override
    public void unregisterTrain(IRollingStockType type) {
        trains.remove(type.uniqueID(), type);
    }

    @Override
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
