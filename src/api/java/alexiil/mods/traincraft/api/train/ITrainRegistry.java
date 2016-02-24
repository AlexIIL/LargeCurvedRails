package alexiil.mods.traincraft.api.train;

import net.minecraft.util.ResourceLocation;

public interface ITrainRegistry {

    void registerTrain(IRollingStockType factory);

    void unregisterTrain(IRollingStockType type);

    IRollingStockType getFactory(ResourceLocation location);

}
