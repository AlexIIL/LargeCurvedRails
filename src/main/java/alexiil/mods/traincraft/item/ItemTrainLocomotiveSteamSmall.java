package alexiil.mods.traincraft.item;

import net.minecraft.world.World;

import alexiil.mods.traincraft.entity.EntityRollingStockBase;
import alexiil.mods.traincraft.entity.EntitySmallSteamLocomotive;

public class ItemTrainLocomotiveSteamSmall extends ItemPlacableTrain {

    @Override
    protected EntityRollingStockBase createRollingStock(World world) {
        return new EntitySmallSteamLocomotive(world);
    }
}
