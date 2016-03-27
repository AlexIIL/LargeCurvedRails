package alexiil.mc.mod.traincraft.item;

import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.component.TypeCartSmall;
import alexiil.mc.mod.traincraft.entity.EntityGenericRollingStock;

public class ItemTrainCartSmall extends ItemPlacableTrain {
    @Override
    public EntityGenericRollingStock createRollingStock(World world) {
        return new EntityGenericRollingStock(world, TypeCartSmall.INSTANCE);
    }
}
