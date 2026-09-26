package dk.tij.winterweather;

import dk.tij.winterweather.commands.WinterCommand;
import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.PlayerDataHandler;
import dk.tij.winterweather.heat.HeatSourceInteraction;
import dk.tij.winterweather.heat.HeatSourceManager;
import dk.tij.winterweather.network.FreezeNetworking;
import dk.tij.winterweather.server.FreezeServerController;
import dk.tij.winterweather.state.FreezeStateManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Provides winter weather functionality for Winter Weather.
 */
public class WinterWeather implements ModInitializer {
    /**
     * Stores the mod enabled value.
     */
    private static boolean modEnabled;
    /**
     * Stores the instance value.
     */
    private static WinterWeather INSTANCE;
    /**
     * Stores the freeze state value.
     */
    private FreezeStateManager freezeState;
    /**
     * Stores the freeze controller value.
     */
    private FreezeServerController freezeController;
    /**
     * Stores the config value.
     */
    private FreezingConfig config;
    /**
     * Stores the enabled value.
     */
    private boolean enabled;
    /**
     * Stores the heat source manager value.
     */
    private HeatSourceManager heatSourceManager;

    /**
     * Performs the mod enabled operation.
     */
    public static boolean modEnabled() {
        return modEnabled;
    }

    /**
     * Performs the heat source manager operation.
     */
    public static HeatSourceManager heatSourceManager() {
        return INSTANCE.heatSourceManager;
    }

    /**
     * Performs the heat source manager or null operation.
     */
    public static HeatSourceManager heatSourceManagerOrNull() {
        return INSTANCE == null ? null : INSTANCE.heatSourceManager;
    }

    /**
     * Performs the use unlit state operation.
     */
    public static boolean useUnlitState() {
        return INSTANCE != null && INSTANCE.config != null && INSTANCE.config.useUnlitState();
    }

    /**
     * Performs the on initialize operation.
     */
    @Override
    public void onInitialize() {
        INSTANCE = this;
        config = FreezingConfig.load();
        enabled = config.enabled();
        modEnabled = enabled;
        freezeState = new FreezeStateManager(new PlayerDataHandler());
        heatSourceManager = new HeatSourceManager(() -> config);
        HeatSourceInteraction.register(heatSourceManager);
        freezeController = new FreezeServerController(freezeState, () -> enabled);
        FreezeNetworking.register(freezeState, () -> config);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            freezeController.tick(server, config);
            for (var level : server.getAllLevels()) {
                heatSourceManager.tick(level);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (var level : server.getAllLevels()) {
                dk.tij.winterweather.data.HeatSourceData.get(level);
                level.getDataStorage().saveAndJoin();
            }
        });
        WinterCommand.register(this);
    }

    /**
     * Reports whether enabled is true.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Performs the set enabled operation.
     *
     * @param enabled the enabled value
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        modEnabled = enabled;
        if (FreezingConfig.setValue("enabled", Boolean.toString(enabled))) {
            config = FreezingConfig.load();
        }
    }

    /**
     * Performs the reload operation.
     */
    public void reload() {
        config = FreezingConfig.load();
        enabled = config.enabled();
        modEnabled = enabled;
    }

    /**
     * Performs the config value operation.
     *
     * @param path the path value
     */
    public String configValue(String path) {
        var value = FreezingConfig.getValue(path);
        return value == null ? null : value.toString();
    }

    /**
     * Performs the set config value operation.
     *
     * @param path  the path value
     * @param value the value value
     */
    public boolean setConfigValue(String path, String value) {
        return FreezingConfig.setValue(path, value);
    }

    /**
     * Performs the freeze state operation.
     */
    public FreezeStateManager freezeState() {
        return freezeState;
    }

    /** Sends the latest config and freeze state to currently connected clients. */
    public void syncClients(MinecraftServer server) {
        FreezeNetworking.syncAll(server, freezeState, config);
    }
}
