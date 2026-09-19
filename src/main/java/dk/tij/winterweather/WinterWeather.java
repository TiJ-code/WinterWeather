package dk.tij.winterweather;

import dk.tij.winterweather.commands.WinterCommand;
import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.PlayerDataHandler;
import dk.tij.winterweather.network.FreezeNetworking;
import dk.tij.winterweather.server.FreezeServerController;
import dk.tij.winterweather.state.FreezeStateManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class WinterWeather implements ModInitializer {
    private FreezeStateManager freezeState;
    private FreezeServerController freezeController;
    private FreezingConfig config;
    private boolean enabled;

    @Override
    public void onInitialize() {
        config = FreezingConfig.load();
        enabled = config.enabled();

        freezeState = new FreezeStateManager(new PlayerDataHandler());
        freezeController = new FreezeServerController(freezeState, () -> enabled);
        FreezeNetworking.register(freezeState, () -> config);
        ServerTickEvents.END_SERVER_TICK.register(server -> freezeController.tick(server, config));
        WinterCommand.register(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (FreezingConfig.setValue("enabled", Boolean.toString(enabled))) {
            config = FreezingConfig.load();
        }
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void reload() {
        config = FreezingConfig.load();
        enabled = config.enabled();
    }
    public String configValue(String path) {
        var value = FreezingConfig.getValue(path);
        return value == null ? null : value.toString();
    }
    public boolean setConfigValue(String path, String value) {
        return FreezingConfig.setValue(path, value);
    }
    public FreezeStateManager freezeState() {
        return freezeState;
    }
}
