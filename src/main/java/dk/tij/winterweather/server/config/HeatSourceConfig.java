package dk.tij.winterweather.server.config;

import java.util.List;


/**
 * Configuration values for heat source.
 *
 * @param value          the value value
 * @param radius         the radius value
 * @param burnoutSeconds the burnout seconds value
 * @param variants       the variants value
 */
public record HeatSourceConfig(
        double value,
        double radius,
        int burnoutSeconds,
        List<HeatSourceVariant> variants
) {
}
