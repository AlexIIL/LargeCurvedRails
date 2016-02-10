package alexiil.mods.traincraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mods.traincraft.api.train.ITrainWorldCache;
import alexiil.mods.traincraft.api.train.StockPathFinder;

public enum TrainWorldCache implements ITrainWorldCache {
    INSTANCE;

    private final Map<Integer, TrainSavedData> trainMap = new HashMap<>();

    private TrainWorldCache() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void loadWorld(WorldEvent.Load load) {
        if (load.world.isRemote) return;
        WorldSavedData savedData = load.world.loadItemData(TrainSavedData.class, "traincraft-cache");
        if (savedData instanceof TrainSavedData) {
            trainMap.put(load.world.provider.getDimensionId(), (TrainSavedData) savedData);
        } else if (savedData == null) {
            TrainSavedData trains = new TrainSavedData("traincraft-cache");
            trainMap.put(load.world.provider.getDimensionId(), trains);
            load.world.setItemData("traincraft-cache", trains);
        } else throw new IllegalStateException(savedData.getClass() + " Was not the correct type! Should have been " + TrainSavedData.class);
    }

    @SubscribeEvent
    public void unloadWorld(WorldEvent.Unload unload) {
        if (unload.world.isRemote) return;
        trainMap.remove(unload.world.provider.getDimensionId());
    }

    // @SubscribeEvent
    // public void playerSwitchDim(PlayerEvent.PlayerChangedDimensionEvent event) {
    // EntityPlayerMP player = (EntityPlayerMP) event.player;
    // int dimId = event.toDim;
    // trainMap.get(Side.SERVER).get(dimId).sendAllTrains(player);
    // }
    //
    // @SubscribeEvent
    // public void playerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
    // // event.player
    // }

    @Override
    public void createTrain(StockPathFinder stockPathFinder) {
        // This should NEVER fail- trains cannot be empty, and all rolling stocks are implicitly entities.
        Entity ent = (Entity) stockPathFinder.parts().get(0);
        World world = ent.getEntityWorld();
        if (world == null) throw new NullPointerException("train.randomStock.world was null!");
        // if (world.isRemote) return;
        // if (true) return;
        int dimId = world.provider.getDimensionId();
        TrainCraft.trainCraftLog.info("TrainWorldCache::createTrain | Created uuid " + stockPathFinder.uuid);
        TrainSavedData tsd = trainMap.get(dimId);
        if (tsd == null) throw new IllegalStateException("Tried to load a train from dimension id " + dimId
            + " but there was not a loaded world for it!");
        // tsd.trains.put(train.uuid, train);
    }

    @Override
    public void deleteTrainIfUnused(StockPathFinder stockPathFinder) {
        Entity ent = (Entity) stockPathFinder.parts().get(0);
        World world = ent.getEntityWorld();
        if (world == null) throw new NullPointerException("train.randomStock.world was null!");
        // if (world.isRemote) return;
        // if (true) return;
        int dimId = world.provider.getDimensionId();

        TrainSavedData tsd = trainMap.get(dimId);
        if (tsd == null) throw new IllegalStateException("Tried to delete a train from dimension id " + dimId
            + " but there was not a loaded world for it!");
        // tsd.trains.remove(train.uuid);
    }

    public static class TrainSavedData extends WorldSavedData {
        private final Map<UUID, StockPathFinder> stockPathFinders = new HashMap<>();

        public TrainSavedData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {

        }

        @Override
        public void writeToNBT(NBTTagCompound nbt) {

        }

        @Override
        public boolean isDirty() {
            return true;
        }
    }
}
