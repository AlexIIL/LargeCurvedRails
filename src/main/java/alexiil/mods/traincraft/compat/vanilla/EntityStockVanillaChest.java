package alexiil.mods.traincraft.compat.vanilla;

import java.util.Collections;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.component.IComponentInner;

public class EntityStockVanillaChest extends EntityStockVanilla {
    private static final IComponentOuter cartComponent;

    static {
        IComponentInner chest = new InnerVanillaChest();
        cartComponent = new ComponentChestCart(null, defaultWheel1, defaultWheel2, Collections.emptyList(), ImmutableList.of(chest), 0.5);
    }

    public EntityStockVanillaChest(World world) {
        super(world, cartComponent);
    }

    @Override
    public int weight() {
        return 100;
    }
}
