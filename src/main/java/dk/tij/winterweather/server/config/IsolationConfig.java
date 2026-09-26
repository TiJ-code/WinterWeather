package dk.tij.winterweather.server.config;

import java.util.Map;


/**
 * Configuration values for isolation.
 *
 * @param maxPossibleIsolation the max possible isolation value
 * @param String               the string value
 * @param armorPieces          the armor pieces value
 */
public record IsolationConfig(
        double maxPossibleIsolation,
        Map<String, Double> armorPieces
) {
}
