package alexiil.mods.traincraft.item;

import net.minecraft.world.World;

import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.entity.EntityRollingStockCart;

public class ItemTrainCartSmall extends ItemPlacableTrain {
    @Override
    public EntityRollingStockBase createRollingStock(World world) {
        return new EntityRollingStockCart(world);
    }
}
