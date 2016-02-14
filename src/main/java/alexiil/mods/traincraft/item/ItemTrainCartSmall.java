package alexiil.mods.traincraft.item;

import net.minecraft.world.World;

import alexiil.mods.traincraft.component.RollingStockTypeCart;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;

public class ItemTrainCartSmall extends ItemPlacableTrain {
    @Override
    public EntityGenericRollingStock createRollingStock(World world) {
        return new EntityGenericRollingStock(world, RollingStockTypeCart.INSTANCE);
    }
}
