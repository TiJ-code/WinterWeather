package dk.tij.winterweather.torch;

public record TorchState(long extinguishAt, boolean locked, boolean suppressSmoke) {
    public TorchState(long extinguishAt) {
        this(extinguishAt, false, false);
    }

    public TorchState(long extinguishAt, boolean locked) {
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

    public TorchState withLocked(boolean locked) {
        return new TorchState(extinguishAt, locked, suppressSmoke);
    }

    public TorchState withExtinguishAt(long extinguishAt) {
        return new TorchState(extinguishAt, locked, suppressSmoke);
    }

    public TorchState withSmokeSuppressed(boolean suppressSmoke) {
        return new TorchState(extinguishAt, locked, suppressSmoke);
    }
}
