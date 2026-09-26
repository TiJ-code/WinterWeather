package dk.tij.winterweather.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Loads and saves per-player freeze progress and debug preferences.
 */
public final class PlayerDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("winterweather-playerdata.json");
    private final Map<UUID, PlayerData> data = new HashMap<>();

    /**
     * Loads player data from the config directory, clearing it if the file is invalid.
     */
    public void load() {
        if (Files.notExists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            JsonObject players = root.has("players") && root.get("players").isJsonObject()
                    ? root.getAsJsonObject("players")
                    : root;
            for (var entry : players.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                JsonObject value = entry.getValue().getAsJsonObject();
                data.put(uuid, new PlayerData(
                        value.has("actual_freeze_ticks") ? value.get("actual_freeze_ticks").getAsDouble() : 0,
                        value.has("debug") && value.get("debug").getAsBoolean()
                ));
            }
        } catch (IOException | JsonParseException | IllegalStateException
                 | IllegalArgumentException | UnsupportedOperationException exception) {
            LOGGER.error("Could not load player data from {}", path, exception);
            data.clear();
        }
    }

    /**
     * Writes all player data to the config directory.
     */
    public void save() {
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            JsonObject players = new JsonObject();
            for (var entry : data.entrySet()) {
                JsonObject value = new JsonObject();
                value.addProperty("actual_freeze_ticks", entry.getValue().actualFreezeTicks());
                value.addProperty("debug", entry.getValue().debug());
                players.add(entry.getKey().toString(), value);
            }
            root.add("players", players);
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save player data", exception);
        }
    }

    /**
     * Gets a player's persisted freeze progress.
     *
     * @param uuid player identifier
     * @return stored freeze ticks, or zero when absent
     */
    public double freezeTicks(UUID uuid) {
        return data.getOrDefault(uuid, new PlayerData(0, false)).actualFreezeTicks();
    }

    /**
     * Updates a player's freeze progress while retaining their debug setting.
     *
     * @param uuid  player identifier
     * @param value freeze ticks to store
     */
    public void setFreezeTicks(UUID uuid, double value) {
        PlayerData previous = data.getOrDefault(uuid, new PlayerData(0, false));
        data.put(uuid, new PlayerData(value, previous.debug()));
    }

    /**
     * Checks whether debug output is enabled for a player.
     *
     * @param uuid player identifier
     * @return {@code true} when debug output is enabled
     */
    public boolean debug(UUID uuid) {
        return data.getOrDefault(uuid, new PlayerData(0, false)).debug();
    }

    /**
     * Updates a player's debug setting while retaining their freeze progress.
     *
     * @param uuid  player identifier
     * @param value whether debug output should be enabled
     * @return the new debug setting
     */
    public boolean setDebug(UUID uuid, boolean value) {
        PlayerData previous = data.getOrDefault(uuid, new PlayerData(0, false));
        data.put(uuid, new PlayerData(previous.actualFreezeTicks(), value));
        return value;
    }

    /**
     * Persisted values associated with one player.
     */
    private record PlayerData(double actualFreezeTicks, boolean debug) {
    }
}
