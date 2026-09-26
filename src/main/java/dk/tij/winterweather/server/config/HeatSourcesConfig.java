package dk.tij.winterweather.server.config;

import java.util.List;


/**
 * Configuration values for heat sources.
 *
 * @param extinguishable the extinguishable value
 * @param useUnlitState  the use unlit state value
 * @param relight        the relight value
 * @param blocks         the blocks value
 */
public record HeatSourcesConfig(
        boolean extinguishable,
        boolean useUnlitState,
        RelightConfig relight,
        List<HeatSourceConfig> blocks
) {
    /**
     * Configuration values for relight.
     *
     * @param flintAndSteel  the flint and steel value
     * @param durabilityCost the durability cost value
     */
    public record RelightConfig(
            boolean flintAndSteel,
            int durabilityCost
    ) {
    }
}
