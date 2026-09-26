package dk.tij.winterweather.server.config;


/**
 * Configuration values for winter weather.
 *
 * @param enabled the enabled value
 * @param frost   the frost value
 */
public record WinterWeatherConfig(
        boolean enabled,
        FrostConfig frost
) {
}
