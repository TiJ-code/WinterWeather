package dk.tij.winterweather.server.config;

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
