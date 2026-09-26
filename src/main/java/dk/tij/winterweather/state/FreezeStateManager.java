package dk.tij.winterweather.state;

import dk.tij.winterweather.data.PlayerDataHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Manages freeze state state and behavior.
 */
public final class FreezeStateManager {
    /**
     * Stores the player data value.
     */
    private final PlayerDataHandler playerData;
    private final Map<UUID, Double> actualFreezeTicks = new HashMap<>();

    /**
     * Performs the freeze state manager operation.
     *
     * @param playerData the player data value
     */
    public FreezeStateManager(PlayerDataHandler playerData) {
        this.playerData = playerData;
        playerData.load();
    }

    /**
     * Performs the register operation.
     *
     * @param player the player value
     */
    public void register(ServerPlayer player) {
        actualFreezeTicks.put(player.getUUID(), playerData.freezeTicks(player.getUUID()));
    }

    /**
     * Performs the reset operation.
     *
     * @param player the player value
     */
    public void reset(ServerPlayer player) {
        actualFreezeTicks.put(player.getUUID(), 0d);
    }

    /**
     * Performs the unregister operation.
     *
     * @param player the player value
     */
    public void unregister(ServerPlayer player) {
        UUID uuid = player.getUUID();
        playerData.setFreezeTicks(uuid, get(uuid));
        playerData.save();
        actualFreezeTicks.remove(uuid);
    }

    /**
     * Performs the accept operation.
     *
     * @param uuid  the uuid value
     * @param value the value value
     */
    public void accept(UUID uuid, double value) {
        if (Double.isFinite(value)) {
            actualFreezeTicks.put(uuid, Math.max(0, value));
        }
    }

    /**
     * Returns the  value.
     *
     * @param uuid the uuid value
     */
    public double get(UUID uuid) {
        return actualFreezeTicks.getOrDefault(uuid, playerData.freezeTicks(uuid));
    }

    /**
     * Performs the debug operation.
     *
     * @param uuid the uuid value
     */
    public boolean debug(UUID uuid) {
        return playerData.debug(uuid);
    }

    /**
     * Performs the set debug operation.
     *
     * @param uuid  the uuid value
     * @param value the value value
     */
    public void setDebug(UUID uuid, boolean value) {
        playerData.setDebug(uuid, value);
    }

    /**
     * Performs the save all operation.
     */
    public void saveAll() {
        for (var entry : actualFreezeTicks.entrySet()) {
            playerData.setFreezeTicks(entry.getKey(), entry.getValue());
        }
        playerData.save();
    }

}
