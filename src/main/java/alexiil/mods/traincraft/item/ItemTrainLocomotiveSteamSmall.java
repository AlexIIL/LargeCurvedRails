package alexiil.mods.traincraft.item;

import net.minecraft.world.World;

import alexiil.mods.traincraft.component.TypeSteamSmall;
import alexiil.mods.traincraft.entity.EntityGenericRollingStock;

public class ItemTrainLocomotiveSteamSmall extends ItemPlacableTrain {
    @Override
    public EntityGenericRollingStock createRollingStock(World world) {
        return new EntityGenericRollingStock(world, TypeSteamSmall.INSTANCE);
    }
}
