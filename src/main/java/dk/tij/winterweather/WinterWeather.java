package dk.tij.winterweather;

import dk.tij.winterweather.commands.WinterCommand;
import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.PlayerDataHandler;
import dk.tij.winterweather.network.FreezeNetworking;
import dk.tij.winterweather.server.FreezeServerController;
import dk.tij.winterweather.state.FreezeStateManager;
import dk.tij.winterweather.torch.TorchInteraction;
import dk.tij.winterweather.torch.TorchManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class WinterWeather implements ModInitializer {
    private static boolean modEnabled;
    private FreezeStateManager freezeState;
    private FreezeServerController freezeController;
    private FreezingConfig config;
    private boolean enabled;
    private TorchManager torchManager;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        config = FreezingConfig.load();
        enabled = config.enabled();
        modEnabled = enabled;

        freezeState = new FreezeStateManager(new PlayerDataHandler());
        torchManager = new TorchManager(() -> config);
        TorchInteraction.register(torchManager);
        freezeController = new FreezeServerController(freezeState, () -> enabled);
        FreezeNetworking.register(freezeState, () -> config);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            freezeController.tick(server, config);
            for (var level : server.getAllLevels()) {
                torchManager.tick(level);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (var level : server.getAllLevels()) {
                dk.tij.winterweather.data.TorchData.get(level);
                level.getDataStorage().saveAndJoin();
            }
        });
        WinterCommand.register(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        modEnabled = enabled;
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
        modEnabled = enabled;
    }
    public static boolean modEnabled() {
        return modEnabled;
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
    public static TorchManager torchManager() {
        return INSTANCE.torchManager;
    }

    public static TorchManager torchManagerOrNull() {
        return INSTANCE == null ? null : INSTANCE.torchManager;
    }

    public static boolean useUnlitState() {
        return INSTANCE != null && INSTANCE.config != null && INSTANCE.config.useUnlitState();
    }

    private static WinterWeather INSTANCE;
}
