package dk.tij.winterweather.torch;

public record TorchState(long extinguishAt, boolean locked) {
    public TorchState(long extinguishAt) {
        this(extinguishAt, false);
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
        return new TorchState(extinguishAt, locked);
    }

    public TorchState withExtinguishAt(long extinguishAt) {
        return new TorchState(extinguishAt, locked);
    }
}
