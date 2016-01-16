package alexiil.mods.traincraft.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import alexiil.mods.traincraft.api.component.IComponent;

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

    /** @return The train object that contains this rolling stock. */
    Train getTrain();

    void setTrain(Train train);

    /** @return The weight of this stock, in kilograms */
    int weight();

    /** @param face The speed this face is advancing at.
     * @return The current speed of this stock, in meters per <strong>second</strong> rather than per tick. */
    double speed();

    /** @return The current momentum (in newtons) towards the direction of the train. (May be negative) */
    default double momentum() {
        return speed() * weight();
    }

    /** If {@link #momentum()} returns a value greater than 0
     * 
     * @param newtons The number of newtons to apply immediatly
     * @param face The face to apply the momentum to. */
    default void applyMomentum(double newtons) {
        getTrain().applyMomentum(newtons);
    }

    /** Sets the speed DIRECTLY to this rolling stock.
     * 
     * This is a callback function for {@link Train#applyMomentum(double)}, you should NEVER call this yourself. */
    void setSpeed(double newSpeed);

    /** @return the maximum newtons that this rolling stock can brake with. You should probably experiment to find a
     *         good value. */
    double maxBrakingForce();

    boolean isBraking();

    /** The current resistance that this stock applies per second. Usually this will be
     * <p>
     * <code>
     * C * {@link #weight()} *  1 - {@link Math#abs(double)} ( {@link #inclination()} )  + frontArea * {@link #speed()} )
     * </code>
     * <p>
     * The value for C generally depends on the type and number of wheels you have, for example 0.08 for wooden based
     * wheels and 0.001 for steel based ones. Other materials will vary.
     * 
     * The value for frontArea should generally be the actual area of the stock, which should be smaller if the front is
     * aerodynamic.
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Rolling_resistance">https://en.wikipedia.org/wiki/Rolling_resistance
     *      </a> */
    double resistance();

    /** Should calculate the current amount of newtons of power this rolling stock is putting out. This will be
     * automatically used by {@link Train#tick(IRollingStock)} to model everything properly.
     * 
     * @param face The direction to test against.
     * 
     * @return The current power output of this locamotive (may be 0 in most cases if this is not a locamotive) */
    double engineOutput();

    /** @param face The face to test against (So if this was going forwards down a hill, then if a value of
     *            {@link Face#FRONT} was passed this would return a negative value)
     * 
     * @return The current inclination (between -1 and 1) for how much the train is positioned vertically. A value of -1
     *         indicates that the train is going vertically down, and a value of 1 indicates the train is going
     *         vertically upwards. If the train is on a 45 degree slope upwards then this should return 0.5. (in other
     *         words run the look vector y component through {@link Math#asin(double)}) */
    double inclination();

    /** @param maxNewtons The maximum number of newtons to apply
     * @return The number of newtons left over from applying the brakes (can be used for checking how much wear needs to
     *         be apllied etc) */
    default double applyBrakes(double maxNewtons) {
        // Make max mewtons positive or zero
        maxNewtons = Math.abs(maxNewtons);
        // Get the amount of momentum going forwards
        double forwardsMomentum = momentum();
        if (forwardsMomentum < 0) {
            // Invert the momentum so we can use comparison easily.
            forwardsMomentum = -forwardsMomentum;
        }
        /* If we have more momentum than braking power then apply the maximum braking power (and return 0 because we
         * used it all up) */
        if (forwardsMomentum > maxNewtons) {
            applyMomentum(maxNewtons);
            return 0;
        } else {
            // Apply our own momentum against us.
            applyMomentum(forwardsMomentum);
            // And return whatever we didn't use up.
            return maxNewtons - forwardsMomentum;
        }
    }

    IComponent mainComponent();

    /** Gets the current position this rolling stock considers itself to be in. This is used by
     * {@link Train#requestNextTrackPath(IRollingStock, ITrackPath)} if the given path is null or is not contained by
     * the train in order to find a path to follow. */
    Vec3 getPathPosition();

    Vec3 getPathDirection(Face direction);
}
