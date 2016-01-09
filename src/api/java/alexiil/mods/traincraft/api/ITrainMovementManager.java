package alexiil.mods.traincraft.api;

import net.minecraft.world.IBlockAccess;

import alexiil.mods.traincraft.api.IRollingStock.Face;

/** Provides a way for trains to find the next path dependant on a previous path. */
public interface ITrainMovementManager {
    /** Finds the next path that follows on from a given path, in a given direction (The returned path will either be
     * null or {@link ITrackPath#start()} will equal then given path's {@link ITrackPath#end()} */
    ITrackPath next(IBlockAccess access, ITrackPath from);

    ITrackPath closest(IRollingStock caller, Face direction);
}
