package alexiil.mods.traincraft.api.train;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.component.IComponentOuter;
import alexiil.mods.traincraft.api.track.ITrackPath;

/** Denotes a train part (that MUST BE an instanceof {@link Entity}) that can usually conenct to other rolling stocks to
 * form a train. */
public interface IRollingStock {
    public enum Face {
        FRONT(1),
        BACK(-1);

        public final int direction;

        private Face(int direction) {
            this.direction = direction;
        }

        public Face opposite() {
            if (this == FRONT) return BACK;
            return FRONT;
        }
    }

    /** @return The weight of this stock, in kilograms */
    int weight();

    /** @param face The speed this face is advancing at.
     * @return The current speed of this stock, in meters per <strong>second</strong> rather than per tick. */
    double speed();

    /** @return The current momentum (in newtons) towards the direction of the train. (May be negative) */
    default double momentum() {
        return speed() * weight();
    }

    /** Sets the speed DIRECTLY to this rolling stock.
     * 
     * This is a callback function for {@link Connector#applyMomentum(double)}, you should NEVER call this yourself. */
    void setSpeed(double newSpeed);

    StockPathFinder pathFinder();

    IComponentOuter mainComponent();

    /** Gets the current position this rolling stock considers itself to be in. This is used by
     * {@link StockPathFinder#requestNextTrackPath(IRollingStock, ITrackPath)} if the given path is null or is not
     * contained by the train in order to find a path to follow. */
    Vec3 getPathPosition();

    Vec3 getPathDirection(Face direction);

    Connector getConnector(Face direction);
}
