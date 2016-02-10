package alexiil.mods.traincraft.api.train;

public interface ITrainWorldCache {
    /** Creates a new train to be saved and loaded witb the world.
     * 
     * @param train The new train that has been created */
    void createTrain(StockPathFinder newTrain);

    /** Removes a train and notifies all clients that thay can forget about this train, but only if all of the
     * {@link IRollingStock} in {@link StockPathFinder#parts} return a different train object than the given one.
     * 
     * ONLY CALL THIS ON THE SERVER!!!!
     * 
     * @param stockPathFinder */
    void deleteTrainIfUnused(StockPathFinder stockPathFinder);

    // /** Sends an update packet about the train to the client. */
    // void updateTrain(Train train);
}
