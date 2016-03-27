package alexiil.mc.mod.traincraft.item;

import net.minecraft.world.World;

import alexiil.mc.mod.traincraft.component.TypeSteamSmall;
import alexiil.mc.mod.traincraft.entity.EntityGenericRollingStock;

public class ItemTrainLocomotiveSteamSmall extends ItemPlacableTrain {
    @Override
    public EntityGenericRollingStock createRollingStock(World world) {
        return new EntityGenericRollingStock(world, TypeSteamSmall.INSTANCE);
    }
}
