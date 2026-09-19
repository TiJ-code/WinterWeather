package dk.tij.winterweather.state;

import dk.tij.winterweather.data.PlayerDataHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FreezeStateManager {
    private final PlayerDataHandler playerData;
    private final Map<UUID, Double> actualFreezeTicks = new HashMap<>();

    public FreezeStateManager(PlayerDataHandler playerData) {
        this.playerData = playerData;
        playerData.load();
    }

    public void register(ServerPlayer player) {
        actualFreezeTicks.put(player.getUUID(), playerData.freezeTicks(player.getUUID()));
    }

    public void reset(ServerPlayer player) {
        actualFreezeTicks.put(player.getUUID(), 0d);
    }

    public void unregister(ServerPlayer player) {
        UUID uuid = player.getUUID();
        playerData.setFreezeTicks(uuid, get(uuid));
        playerData.save();
        actualFreezeTicks.remove(uuid);
    }

    public void accept(UUID uuid, double value, int maximum) {
        if (Double.isFinite(value)) {
            actualFreezeTicks.put(uuid, clamp(value, maximum));
        }
    }

    public double get(UUID uuid) {
        return actualFreezeTicks.getOrDefault(uuid, playerData.freezeTicks(uuid));
    }

    public boolean debug(UUID uuid) {
        return playerData.debug(uuid);
    }

    public void setDebug(UUID uuid, boolean value) {
        playerData.setDebug(uuid, value);
    }

    public void saveAll() {
        for (var entry : actualFreezeTicks.entrySet()) {
            playerData.setFreezeTicks(entry.getKey(), entry.getValue());
        }
        playerData.save();
    }

    public static double clamp(double value, int maximum) {
        return Math.max(0, Math.min(maximum, value));
    }
}
