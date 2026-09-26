package dk.tij.winterweather.server.config;

public record WinterWeatherConfig(
        boolean enabled,
        FrostConfig frost
) {
}
