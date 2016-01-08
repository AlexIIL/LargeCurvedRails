package alexiil.mods.traincraft.component;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;

import alexiil.mods.traincraft.entity.EntityRollingStockBase;

/** A component is something that makes up a full {@link EntityRollingStockBase} by connected to one or more other
 * components. */
public class Component {
    private final ImmutableMap<Component, Double> subComponents;
    private final ResourceLocation modelLoc, textureLoc;

    public Component(Map<Component, Double> subComponents, ResourceLocation modelLocation, ResourceLocation textureLocation) {
        this.subComponents = ImmutableMap.copyOf(subComponents);
        this.modelLoc = modelLocation;
        this.textureLoc = textureLocation;
    }

    public ResourceLocation getModelLocation() {
        return modelLoc;
    }

    public ResourceLocation getTextureLocation() {
        return textureLoc;
    }

    /** @return A map between components to how far along they are connected to this one. */
    public Map<Component, Double> restingComponents() {
        return subComponents;
    }
}
