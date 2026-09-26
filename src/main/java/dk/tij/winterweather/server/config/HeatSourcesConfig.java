package dk.tij.winterweather.server.config;

import java.util.List;

public record HeatSourcesConfig(
        boolean extinguishable,
        boolean useUnlitState,
        RelightConfig relight,
        List<HeatSourceConfig> blocks
) {
    public record RelightConfig(
            boolean flintAndSteel,
            int durabilityCost
    ) {}
}
