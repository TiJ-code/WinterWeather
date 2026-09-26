package dk.tij.winterweather.server.config;

import java.util.List;

public record HeatSourceConfig(
        double value,
        double radius,
        int burnoutSeconds,
        List<HeatSourceVariant> variants
) {
}
