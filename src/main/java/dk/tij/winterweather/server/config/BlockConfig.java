package dk.tij.winterweather.server.config;

/**
 * Configuration values for block.
 *
 * @param heat           the heat value
 * @param radius         the radius value
 * @param extinguishable the extinguishable value
 * @param burnoutSeconds the burnout seconds value
 * @param interactable   whether players can turn off this heat source
 */
public record BlockConfig(
        double heat,
        double radius,
        boolean extinguishable,
        int burnoutSeconds,
        boolean interactable
) {
    /**
     * Performs the radius squared operation.
     */
    public double radiusSquared() {
        return radius * radius;
    }
}
