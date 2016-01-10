package alexiil.mods.traincraft;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mods.traincraft.api.IRollingStock;
import alexiil.mods.traincraft.api.ITrainWorldCache;
import alexiil.mods.traincraft.api.Train;
import alexiil.mods.traincraft.network.MessageCreateTrain;
import alexiil.mods.traincraft.network.MessageDeleteTrain;
import alexiil.mods.traincraft.network.MessageHandler;
import alexiil.mods.traincraft.network.MessageUpdateTrain;

import io.netty.buffer.ByteBuf;

public enum TrainWorldCache implements ITrainWorldCache {
    INSTANCE;

    private final Map<Side, Map<Integer, TrainSavedData>> trainMap = new HashMap<>();

    private TrainWorldCache() {
        trainMap.put(Side.CLIENT, new HashMap<>());
        trainMap.put(Side.SERVER, new HashMap<>());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void loadWorld(WorldEvent.Load load) {
        WorldSavedData savedData = load.world.loadItemData(TrainSavedData.class, "traincraft-cache");
        if (savedData instanceof TrainSavedData) {
            Side side = load.world.isRemote ? Side.CLIENT : Side.SERVER;
            trainMap.get(side).put(load.world.provider.getDimensionId(), (TrainSavedData) savedData);
        } else if (savedData == null) {
            Side side = load.world.isRemote ? Side.CLIENT : Side.SERVER;
            TrainSavedData trains = new TrainSavedData("traincraft-cache");
            trainMap.get(side).put(load.world.provider.getDimensionId(), trains);
            load.world.setItemData("traincraft-cache", trains);
        } else throw new IllegalStateException(savedData.getClass() + " Was not the correct type! Should have been " + TrainSavedData.class);
    }

    @SubscribeEvent
    public void unloadWorld(WorldEvent.Unload unload) {
        Side side = unload.world.isRemote ? Side.CLIENT : Side.SERVER;
        trainMap.get(side).remove(unload.world.provider.getDimensionId());
    }

    @SubscribeEvent
    public void playerSwitchDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        int dimId = event.toDim;
        trainMap.get(Side.SERVER).get(dimId).sendAllTrains(player);
    }

    @SubscribeEvent
    public void playerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        // event.player
    }

    @Override
    public void createTrain(Train train) {
        // This should NEVER fail- trains cannot be empty, and all rolling stocks are implicitly entities.
        Entity ent = (Entity) train.parts().get(0);
        World world = ent.getEntityWorld();
        if (world == null) throw new NullPointerException("train.randomStock.world was null!");
        int dimId = world.provider.getDimensionId();
        Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
        TrainCraft.trainCraftLog.info("TrainWorldCache::createTrain | Ccreated id " + train.id + " on side " + side);
        TrainSavedData tsd = trainMap.get(side).get(dimId);
        if (tsd == null) throw new IllegalStateException("Tried to load a train from dimension id " + dimId
            + " but there was not a loaded world for it!");
        tsd.trains.put(train.id, train);
        if (side == Side.CLIENT) return;
        MessageCreateTrain create = new MessageCreateTrain(train);
        MessageHandler.INSTANCE.getWrapper().sendToDimension(create, dimId);
    }

    @Override
    public void deleteTrainIfUnused(Train train) {
        MessageDeleteTrain delete = new MessageDeleteTrain(0, train.id);

        Entity ent = (Entity) train.parts().get(0);
        World world = ent.getEntityWorld();
        if (world == null) throw new NullPointerException("train.randomStock.world was null!");
        int dimId = world.provider.getDimensionId();

        MessageHandler.INSTANCE.getWrapper().sendToDimension(delete, dimId);
    }

    @SideOnly(Side.CLIENT)
    public void deleteTrainIfUnused(int worldDimId, int trainId) {
        TrainSavedData data = trainMap.get(Side.CLIENT).get(worldDimId);
        if (data == null) return;
        Train train = data.trains.remove(trainId);
        boolean has = false;
        for (IRollingStock stock : train.parts()) {
            if (stock.getTrain() == train) {
                has = true;
                // Something went wrong, lets put up a big warning so we can debug it later
                TrainCraft.trainCraftLog.warn("TrainWorldCache::deleteTrainIfUnused | The entity " + ((Entity) stock).getUniqueID()
                    + " was still using the train " + train.uuid);
            }
        }
        if (!has) return;
        data.trains.put(trainId, train);
    }

    @Override
    public void updateTrain(Train train) {
        MessageUpdateTrain update = new MessageUpdateTrain(train);

        Entity ent = (Entity) train.parts().get(0);
        World world = ent.getEntityWorld();
        if (world == null) throw new NullPointerException("train.randomStock.world was null!");
        int dimId = world.provider.getDimensionId();

        MessageHandler.INSTANCE.getWrapper().sendToDimension(update, dimId);
    }

    @SideOnly(Side.CLIENT)
    public void recieveUpdateMessage(int dimId, int trainId, ByteBuf data) {
        TrainSavedData tsd = trainMap.get(Side.CLIENT).get(dimId);
        if (tsd == null) throw new IllegalStateException("Tried to load a train from dimension id " + dimId
            + " but there was not a loaded world for it!");
        Train t = tsd.trains.get(trainId);
        if (t == null) throw new IllegalStateException("Found a null train for trainId " + trainId);
        t.readFromByteBuf(data);
    }

    public static class TrainSavedData extends WorldSavedData {
        private final Map<Integer, Train> trains = new HashMap<>();

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

        public void sendAllTrains(EntityPlayerMP player) {
            TrainCraft.trainCraftLog.info("Sending all trains (" + trains.size() + ") to " + player);
            int i = 0;
            for (Train train : trains.values()) {
                TrainCraft.trainCraftLog.info("  - Sending train " + i++ + ": " + train);
                MessageCreateTrain create = new MessageCreateTrain(train);
                MessageHandler.INSTANCE.getWrapper().sendTo(create, player);
            }
        }
    }
}
