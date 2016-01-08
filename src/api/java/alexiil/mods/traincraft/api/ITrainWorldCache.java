package alexiil.mods.traincraft.api;

public interface ITrainWorldCache {
    /** Creates and sends this train to the client.
     * 
     * @param train The new train that has been created */
    void createTrain(Train newTrain);

    /** Removes a train and notifies all clients that thay can forget about this train, but only if all of the
     * {@link IRollingStock} in {@link Train#parts} return a different train object than the given one.
     * 
     * ONLY CALL THIS ON THE SERVER!!!!
     * 
     * @param train */
    void deleteTrainIfUnused(Train train);

    /** Sends an update packet about the train to the client. */
    void updateTrain(Train train);
}
