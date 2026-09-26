package dk.tij.winterweather.server.config;


/**
 * Configuration values for frost.
 *
 * @param criticalFreezingTicks the critical freezing ticks value
 * @param playerRadius          the player radius value
 * @param interpolationFunction the interpolation function value
 * @param playerBurningBoost    the player burning boost value
 * @param playerPowderSnowBoost the player powder snow boost value
 * @param isolation             the isolation value
 * @param heatSources           the heat sources value
 */
public record FrostConfig(
        int criticalFreezingTicks,
        double playerRadius,
        String interpolationFunction,
        double playerBurningBoost,
        double playerPowderSnowBoost,
        IsolationConfig isolation,
        HeatSourcesConfig heatSources
) {
}
