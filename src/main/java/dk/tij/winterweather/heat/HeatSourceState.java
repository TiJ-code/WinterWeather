package dk.tij.winterweather.heat;

public record HeatSourceState(long extinguishAt, boolean locked, boolean suppressSmoke) {
    public HeatSourceState(long extinguishAt) {
        this(extinguishAt, false, false);
    }

    public HeatSourceState(long extinguishAt, boolean locked) {
        this(extinguishAt, locked, false);
    }

    public boolean isExpired(long gameTime) {
        return extinguishAt > 0 && gameTime >= extinguishAt;
    }

    public int remainingTicks(long gameTime) {
        return Math.max(0, (int) Math.min(Integer.MAX_VALUE, extinguishAt - gameTime));
    }

    public boolean isUnlit() {
        return extinguishAt == 0;
    }

    public HeatSourceState withLocked(boolean locked) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }

    public HeatSourceState withExtinguishAt(long extinguishAt) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }

    public HeatSourceState withSmokeSuppressed(boolean suppressSmoke) {
        return new HeatSourceState(extinguishAt, locked, suppressSmoke);
    }
}
