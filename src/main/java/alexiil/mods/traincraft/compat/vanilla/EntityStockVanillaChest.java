package alexiil.mods.traincraft.compat.vanilla;

import java.util.Collections;

import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.world.World;

import alexiil.mods.traincraft.api.component.IComponent;

public class EntityStockVanillaChest extends EntityStockVanillaWrapper<EntityMinecartChest> {
    private static final IComponent cartComponent;

    static {
        cartComponent = new ComponentChestCart(null, wheel1, wheel2, Collections.emptyList(), Collections.emptyList(), 0.5);
    }

    public EntityStockVanillaChest(World world) {
        super(world, cartComponent, new EntityMinecartChest(world));
    }

    @Override
    public int weight() {
        return 0;
    }
}
