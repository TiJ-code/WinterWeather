package dk.tij.winterweather.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dk.tij.winterweather.server.config.BlockConfig;
import dk.tij.winterweather.server.config.ConfigParseException;
import dk.tij.winterweather.server.config.FrostConfig;
import dk.tij.winterweather.server.config.HeatSourceConfig;
import dk.tij.winterweather.server.config.HeatSourceVariant;
import dk.tij.winterweather.server.config.HeatSourcesConfig;
import dk.tij.winterweather.server.config.IsolationConfig;
import dk.tij.winterweather.server.config.WinterWeatherConfig;
import dk.tij.winterweather.server.config.WinterWeatherConfigLoader;
import dk.tij.winterweather.heat.HeatSourceBlocks;
import dk.tij.winterweather.utils.InterpolationFunctions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record FreezingConfig(
        boolean enabled,
        int criticalFreezingTicks,
        double playerRadius,
        double maxPossibleIsolation,
        double playerBurningBoost,
        double playerPowderSnowBoost,
        InterpolationFunctions interpolation,
        Map<Block, BlockConfig> blocks,
        Map<Identifier, Double> armorIsolation,
        boolean heatSourcesEnabled,
        boolean useUnlitState,
        boolean heatSourceRelightingEnabled,
        int heatSourceRelightDurabilityCost
) {
    public static final int MAX_FROZEN_TICKS = 140;

    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("winterweather.json");

    private static FreezingConfig defaults() {
        return from(defaultWinterWeatherConfig());
    }

    private static WinterWeatherConfig defaultWinterWeatherConfig() {
        return new WinterWeatherConfig(
                false,
                new FrostConfig(
                        1800,
                        5,
                        "smootherstep",
                        10,
                        0,
                        new IsolationConfig(
                                80,
                                Map.of(
                                        "minecraft:leather_boots", 15.0,
                                        "minecraft:leather_leggings", 30.0,
                                        "minecraft:leather_chestplate", 40.0,
                                        "minecraft:leather_helmet", 15.0
                                )
                        ),
                        new HeatSourcesConfig(
                                true,
                                true,
                                new HeatSourcesConfig.RelightConfig(true, 1),
                                List.of(
                                        heatSource(1.0, 3.0, 300,
                                                "minecraft:torch", "minecraft:wall_torch"),
                                        heatSource(2.0, 3.0, 3000,
                                                "minecraft:soul_torch", "minecraft:soul_wall_torch"),
                                        heatSource(1.5, 3.5, 600,
                                                "minecraft:copper_torch", "minecraft:copper_wall_torch"),
                                        heatSource(2.0, 5.0, 300, "minecraft:campfire"),
                                        heatSource(2.0, 6.0, 3000, "minecraft:soul_campfire"),
                                        heatSource(3.0, 5.0, 0, "minecraft:fire"),
                                        heatSource(3.0, 7.0, 0, "minecraft:soul_fire"),
                                        heatSource(6.0, 3.0, 0, "minecraft:lava"),
                                        heatSource(1.0, 5.0, 0,
                                                "minecraft:lantern",
                                                "minecraft:copper_lantern",
                                                "minecraft:exposed_copper_lantern",
                                                "minecraft:weathered_copper_lantern",
                                                "minecraft:oxidized_copper_lantern",
                                                "minecraft:waxed_copper_lantern",
                                                "minecraft:waxed_exposed_copper_lantern",
                                                "minecraft:waxed_weathered_copper_lantern",
                                                "minecraft:waxed_oxidized_copper_lantern")
                                )
                        )
                )
        );
    }

    private static HeatSourceConfig heatSource(
            double value,
            double radius,
            int burnoutSeconds,
            String... blockIds
    ) {
        return new HeatSourceConfig(
                value,
                radius,
                burnoutSeconds,
                java.util.Arrays.stream(blockIds)
                        .map(HeatSourceVariant::new)
                        .toList()
        );
    }

    public static FreezingConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (InputStream input = FreezingConfig.class.getResourceAsStream("/config/winterweather.json")) {
                    if (input == null) {
                        WinterWeatherConfigLoader.save(CONFIG_PATH, defaultWinterWeatherConfig());
                    } else {
                        Files.copy(input, CONFIG_PATH);
                    }
                }
            }
            return from(WinterWeatherConfigLoader.load(CONFIG_PATH));
        } catch (Exception exception) {
            LOGGER.error("Could not load {}, using defaults", CONFIG_PATH, exception);
            return defaults();
        }
    }

    public static FreezingConfig from(WinterWeatherConfig config) {
        FrostConfig frost = config.frost();
        HeatSourcesConfig heatSources = frost.heatSources();

        Map<Block, BlockConfig> blocks = new HashMap<>();
        for (HeatSourceConfig source : heatSources.blocks()) {
            boolean extinguishable = heatSources.extinguishable();
            for (HeatSourceVariant variant : source.variants()) {
                Identifier id = Identifier.tryParse(variant.blockId());
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                    LOGGER.warn("Unknown heat source block id: {}", variant.blockId());
                    continue;
                }
                BuiltInRegistries.BLOCK.get(id).ifPresent(holder -> blocks.put(
                        holder.value(),
                        new BlockConfig(
                                source.value(),
                                source.radius(),
                                extinguishable,
                                source.burnoutSeconds()
                        )
                ));
            }
        }

        Map<Identifier, Double> armor = new HashMap<>();
        for (var entry : frost.isolation().armorPieces().entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id != null) {
                armor.put(id, Math.max(0, Math.min(1, entry.getValue() / 100)));
            }
        }

        return new FreezingConfig(
                config.enabled(),
                Math.max(1, frost.criticalFreezingTicks()),
                Math.max(0, frost.playerRadius()),
                Math.max(0, frost.isolation().maxPossibleIsolation()) / 100,
                1 + Math.max(0, frost.playerBurningBoost()) / 100,
                1 + Math.max(0, frost.playerPowderSnowBoost()) / 100,
                InterpolationFunctions.by(frost.interpolationFunction()),
                Map.copyOf(blocks),
                Map.copyOf(armor),
                heatSources.extinguishable(),
                heatSources.useUnlitState(),
                heatSources.relight().flintAndSteel(),
                Math.max(0, heatSources.relight().durabilityCost())
        );
    }

    public static JsonElement getValue(String path) {
        try {
            JsonElement current = JsonParser.parseString(Files.readString(CONFIG_PATH));
            for (String part : path.split("\\.")) {
                if (!current.isJsonObject() || !current.getAsJsonObject().has(part)) return null;
                current = current.getAsJsonObject().get(part);
            }
            return current;
        } catch (IOException | IllegalStateException | ConfigParseException exception) {
            return null;
        }
    }

    public double temperatureDelta(Level level, BlockPos playerBlockPos, Vec3 playerPos, Vec3 eyePos,
                                   List<Identifier> armorItems, boolean burning, int powderSnowBlocks) {
        double heat = 0;
        int radius = (int) Math.ceil(playerRadius);
        for (BlockPos candidate : BlockPos.betweenClosed(
                playerBlockPos.offset(-radius, -radius, -radius),
                playerBlockPos.offset(radius, radius, radius))) {
            var state = level.getBlockState(candidate);
            if (!dk.tij.winterweather.heat.HeatSourceManager.isLit(state)) {
                continue;
            }
            BlockConfig source = blocks.get(state.getBlock());
            if (source == null || candidate.getCenter().distanceToSqr(playerPos) > source.radiusSquared()) {
                continue;
            }
            heat += source.heat();
        }

        double change = heat > 0 ? -heat : 1;
        if (change > 0) {
            double isolation = armorItems.stream().mapToDouble(item -> armorIsolation.getOrDefault(item, 0d))
                    .sum() * maxPossibleIsolation;
            change *= 1 - Math.min(1, isolation);
        }
        if (burning) {
            change = change < 0 ? change - (3 * playerBurningBoost) : change - playerBurningBoost;
        }
        if (powderSnowBlocks > 0) {
            double powderSnow = Math.pow(1.225, powderSnowBlocks) * playerPowderSnowBoost;
            change = change < 0 ? powderSnow : change + powderSnow;
        }
        return change;
    }

    public int toFrozenTicks(double actualFreezeTicks) {
        return Math.clamp((int) (toFrozenProgress(actualFreezeTicks) * MAX_FROZEN_TICKS + 0.5),
                0, MAX_FROZEN_TICKS);
    }

    public float toFrozenProgress(double actualFreezeTicks) {
        double progress = Math.clamp(actualFreezeTicks / criticalFreezingTicks, 0, 1);
        return (float) Math.clamp(interpolation.apply(progress), 0, 1);
    }

    public boolean isExtinguishable(BlockState state) {
        BlockConfig config = blocks.get(state.getBlock());
        return config != null && config.extinguishable()
                && (state.hasProperty(HeatSourceBlocks.LIT)
                || state.hasProperty(net.minecraft.world.level.block.CampfireBlock.LIT));
    }

    public int extinguishableBurnoutSeconds(BlockState state) {
        BlockConfig config = blocks.get(state.getBlock());
        return config == null ? 0 : config.burnoutSeconds();
    }

    public static boolean setValue(String path, String rawValue) {
        try {
            JsonElement root = JsonParser.parseString(Files.readString(CONFIG_PATH));
            String[] parts = path.split("\\.");
            JsonObject current = root.getAsJsonObject();
            for (int index = 0; index < parts.length - 1; index++) {
                JsonElement child = current.get(parts[index]);
                if (child == null || !child.isJsonObject()) return false;
                current = child.getAsJsonObject();
            }
            if (!current.has(parts[parts.length - 1])) return false;
            current.add(parts[parts.length - 1], parseValue(rawValue));
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static JsonElement parseValue(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return new JsonPrimitive(Boolean.parseBoolean(value));
        }
        try {
            return new JsonPrimitive(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            try {
                return new JsonPrimitive(Double.parseDouble(value));
            } catch (NumberFormatException ignoredAgain) {
                return new JsonPrimitive(value);
            }
        }
    }

    public boolean isInsulatedArmor(Identifier item) {
        return armorIsolation.containsKey(item);
    }
}
