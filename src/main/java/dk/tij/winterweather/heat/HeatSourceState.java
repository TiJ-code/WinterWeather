package dk.tij.winterweather.heat;

/**
 * Performs the heat source state operation.
 *
 * @param extinguishAt  the extinguish at value
 * @param locked        the locked value
 * @param suppressSmoke the suppress smoke value
 */
public record HeatSourceState(long extinguishAt, boolean locked, boolean suppressSmoke) {
    /**
     * Performs the heat source state operation.
     *
     * @param extinguishAt the extinguish at value
     */
    public HeatSourceState(long extinguishAt) {
        this(extinguishAt, false, false);
    }

    /**
     * Performs the heat source state operation.
     *
     * @param extinguishAt the extinguish at value
     * @param locked       the locked value
     */
    public HeatSourceState(long extinguishAt, boolean locked) {
        this(extinguishAt, locked, false);
    }

    /**
     * Reports whether expired is true.
     *
     * @param gameTime the game time value
     */
    public boolean isExpired(long gameTime) {
        return extinguishAt > 0 && gameTime >= extinguishAt;
    }

    /**
     * Performs the remaining ticks operation.
     *
     * @param gameTime the game time value
     */
    public int remainingTicks(long gameTime) {
        return Math.max(0, (int) Math.min(Integer.MAX_VALUE, extinguishAt - gameTime));
    }

    /**
     * Reports whether unlit is true.
     */
    public boolean isUnlit() {
        return extinguishAt == 0;
    }

    /**
     * Performs the with locked operation.
     *
     * @param locked the locked value
     */
    public HeatSourceState withLocked(boolean locked) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }

    /**
     * Performs the with extinguish at operation.
     *
     * @param extinguishAt the extinguish at value
     */
    public HeatSourceState withExtinguishAt(long extinguishAt) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }

    /**
     * Performs the with smoke suppressed operation.
     *
     * @param suppressSmoke the suppress smoke value
     */
    public HeatSourceState withSmokeSuppressed(boolean suppressSmoke) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }
}
