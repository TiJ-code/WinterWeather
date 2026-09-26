package dk.tij.winterweather.server.config;

import java.util.Map;

public record IsolationConfig(
        double maxPossibleIsolation,
        Map<String, Double> armorPieces
) {
}
