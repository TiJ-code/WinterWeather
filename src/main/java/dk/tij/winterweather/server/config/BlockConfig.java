package dk.tij.winterweather.server.config;

public record BlockConfig(
        double heat,
        double radius,
        boolean extinguishable,
        int burnoutSeconds
) {
    public double radiusSquared() {
        return radius * radius;
    }
}
