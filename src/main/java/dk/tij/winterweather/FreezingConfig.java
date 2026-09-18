package dk.tij.winterweather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FreezingConfig {
    public static final int MAX_FROZEN_TICKS = 140;

    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("winterweather.json");

    private final List<HeatSource> heatSources;
    private final int freezeRate;
    private final int thawRate;
    private final int maxRange;

    private FreezingConfig(List<HeatSource> heatSources, int freezeRate, int thawRate) {
        this.heatSources = heatSources;
        this.freezeRate = freezeRate;
        this.thawRate = thawRate;
        this.maxRange = heatSources.stream()
                .mapToInt(source -> (int) Math.ceil(source.range()))
                .max()
                .orElse(0);
    }

    public static FreezingConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (InputStream input = FreezingConfig.class.getResourceAsStream("/config/winterweather.json")) {
                    if (input != null) {
                        Files.copy(input, CONFIG_PATH);
                    } else {
                        Files.writeString(CONFIG_PATH, GSON.toJson(defaults().toJson()));
                    }
                }
            }

            return fromJson(JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject());
        } catch (IOException | JsonParseException | IllegalStateException exception) {
            LOGGER.error("Could not load {}, using default freezing configuration", CONFIG_PATH, exception);
            return defaults();
        }
    }

    public boolean isNearHeatSource(Level world, BlockPos playerBlockPos,
                                    net.minecraft.world.phys.Vec3 playerPos) {
        BlockPos min = playerBlockPos.offset(-maxRange, -maxRange, -maxRange);
        BlockPos max = playerBlockPos.offset(maxRange, maxRange, maxRange);
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            Block block = world.getBlockState(candidate).getBlock();
            for (HeatSource heatSource : heatSources) {
                if (heatSource.block() == block
                        && candidate.getCenter().distanceToSqr(playerPos) <= heatSource.range() * heatSource.range()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int freezeRate() {
        return freezeRate;
    }

    public int thawRate() {
        return thawRate;
    }

    private static FreezingConfig defaults() {
        List<HeatSource> heatSources = new ArrayList<>();
        addDefault(heatSources, "minecraft:torch", 8);
        addDefault(heatSources, "minecraft:wall_torch", 8);
        addDefault(heatSources, "minecraft:campfire", 8);
        addDefault(heatSources, "minecraft:soul_campfire", 8);
        addDefault(heatSources, "minecraft:fire", 5);
        addDefault(heatSources, "minecraft:soul_fire", 5);
        addDefault(heatSources, "minecraft:lava", 6);
        addDefault(heatSources, "minecraft:magma_block", 3);
        return new FreezingConfig(heatSources, 1, 2);
    }

    private static void addDefault(List<HeatSource> heatSources, String id, double range) {
        BuiltInRegistries.BLOCK.get(Identifier.parse(id))
                .ifPresent(holder -> heatSources.add(new HeatSource(holder.value(), range)));
    }

    private static FreezingConfig fromJson(JsonObject json) {
        int freezeRate = positiveInt(json, "freeze_rate", 1);
        int thawRate = positiveInt(json, "thaw_rate", 2);
        List<HeatSource> heatSources = new ArrayList<>();
        JsonElement sourceElement = json.get("heat_sources");
        if (sourceElement == null || !sourceElement.isJsonObject()) {
            return defaults();
        }
        JsonObject sources = sourceElement.getAsJsonObject();

        for (var entry : sources.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                LOGGER.warn("Ignoring unknown heat-source block '{}'", entry.getKey());
                continue;
            }

            double range = entry.getValue().getAsDouble();
            if (range <= 0) {
                LOGGER.warn("Ignoring heat-source block '{}' with non-positive range", entry.getKey());
                continue;
            }
            BuiltInRegistries.BLOCK.get(id)
                    .ifPresent(holder -> heatSources.add(new HeatSource(holder.value(), range)));
        }

        return new FreezingConfig(heatSources, freezeRate, thawRate);
    }

    private static int positiveInt(JsonObject json, String name, int fallback) {
        JsonElement value = json.get(name);
        if (value == null || !value.isJsonPrimitive() || value.getAsInt() <= 0) {
            return fallback;
        }
        return value.getAsInt();
    }

    private JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("freeze_rate", freezeRate);
        json.addProperty("thaw_rate", thawRate);

        JsonObject sources = new JsonObject();
        for (HeatSource heatSource : heatSources) {
            sources.addProperty(BuiltInRegistries.BLOCK.getKey(heatSource.block()).toString(), heatSource.range());
        }
        json.add("heat_sources", sources);
        return json;
    }

    private record HeatSource(Block block, double range) {
    }
}
