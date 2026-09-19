package dk.tij.winterweather.torch;

public record TorchState(long extinguishAt) {
    public boolean isExpired(long gameTime) {
        return extinguishAt > 0 && gameTime >= extinguishAt;
    }

    public int remainingTicks(long gameTime) {
        return Math.max(0, (int) Math.min(Integer.MAX_VALUE, extinguishAt - gameTime));
    }

    public boolean isUnlit() {
        return extinguishAt == 0;
    }

}
