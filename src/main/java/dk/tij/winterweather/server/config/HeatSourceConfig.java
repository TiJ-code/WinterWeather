package dk.tij.winterweather.server.config;

import java.util.List;


/**
 * Configuration values for heat source.
 *
 * @param name           display name for this group of block variants
 * @param value          heat contribution
 * @param radius         the radius value
 * @param burnoutSeconds the burnout seconds value
 * @param variants       the variants value
 */
public record HeatSourceConfig(
        String name,
        double value,
        double radius,
        int burnoutSeconds,
        List<HeatSourceVariant> variants
) {
}
