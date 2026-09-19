package dk.tij.winterweather.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dk.tij.winterweather.utils.Maths;
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
import java.util.Set;
import java.util.function.Function;

public record FreezingConfig(
        boolean enabled,
        int criticalFreezingTicks,
        double playerRadius,
        double maxPossibleIsolation,
        double playerBurningBoost,
        double playerPowderSnowBoost,
        Function<Double, Double> interpolation,
        Map<Block, HeatSource> heatSources,
        Map<Identifier, Double> armorIsolation,
        Map<Block, Integer> extinguishableBurnoutSeconds,
        boolean torchesEnabled,
        boolean torchRelightingEnabled,
        int torchRelightDurabilityCost
) {
    public static final int MAX_FROZEN_TICKS = 140;

    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("winterweather.json");

    private static FreezingConfig defaults() {
        Map<Block, HeatSource> sources = new HashMap<>();
        addSource(sources, "minecraft:torch", 1, 3);
        addSource(sources, "minecraft:wall_torch", 1, 3);
        addSource(sources, "minecraft:campfire", 2, 3);
        addSource(sources, "minecraft:soul_campfire", 2, 3);
        addSource(sources, "minecraft:fire", 3, 5);
        addSource(sources, "minecraft:soul_fire", 3, 5);
        addSource(sources, "minecraft:lava", 3, 7);
        addSource(sources, "minecraft:lantern", 1, 3);

        Map<Identifier, Double> armor = new HashMap<>();
        armor.put(Identifier.parse("minecraft:leather_boots"), .15);
        armor.put(Identifier.parse("minecraft:leather_leggings"), .30);
        armor.put(Identifier.parse("minecraft:leather_chestplate"), .40);
        armor.put(Identifier.parse("minecraft:leather_helmet"), .15);
        Map<Block, Integer> extinguishable = new HashMap<>();
        addExtinguishable(extinguishable, "minecraft:torch", 86400);
        addExtinguishable(extinguishable, "minecraft:wall_torch", 86400);
        addExtinguishable(extinguishable, "minecraft:soul_torch", 86400);
        addExtinguishable(extinguishable, "minecraft:soul_wall_torch", 86400);
        addExtinguishable(extinguishable, "minecraft:copper_torch", 86400);
        addExtinguishable(extinguishable, "minecraft:copper_wall_torch", 86400);
        addExtinguishable(extinguishable, "minecraft:campfire", 86400);
        addExtinguishable(extinguishable, "minecraft:soul_campfire", 86400);
        return new FreezingConfig(false, 1800, 5, .8, 1.1, 1, Maths::smootherstep,
                sources, armor, extinguishable, true, true, 1);
    }

    public static FreezingConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (InputStream input = FreezingConfig.class.getResourceAsStream("/config/winterweather.json")) {
                    if (input == null) {
                        Files.writeString(CONFIG_PATH, GSON.toJson(defaults().toJson()));
                    } else {
                        Files.copy(input, CONFIG_PATH);
                    }
                }
            }
            return fromJson(JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject());
        } catch (Exception exception) {
            LOGGER.error("Could not load {}, using defaults", CONFIG_PATH, exception);
            return defaults();
        }
    }

    public static JsonElement getValue(String path) {
        try {
            JsonElement current = JsonParser.parseString(Files.readString(CONFIG_PATH));
            for (String part : path.split("\\.")) {
                if (!current.isJsonObject() || !current.getAsJsonObject().has(part)) return null;
                current = current.getAsJsonObject().get(part);
            }
            return current;
        } catch (IOException | IllegalStateException exception) {
            return null;
        }
    }

    private static void addSource(Map<Block, HeatSource> sources, String id, double heat, double radius) {
        BuiltInRegistries.BLOCK.get(Identifier.parse(id))
                .ifPresent(holder -> sources.put(holder.value(), new HeatSource(heat, radius * radius)));
    }

    private static void addExtinguishable(Map<Block, Integer> blocks, String id, int seconds) {
        BuiltInRegistries.BLOCK.get(Identifier.parse(id))
                .ifPresent(holder -> blocks.put(holder.value(), seconds));
    }

    public double temperatureDelta(Level level, BlockPos playerBlockPos, Vec3 playerPos, Vec3 eyePos,
                                   List<Identifier> armorItems, boolean burning, int powderSnowBlocks) {
        double heat = 0;
        int radius = (int) Math.ceil(playerRadius);
        for (BlockPos candidate : BlockPos.betweenClosed(
                playerBlockPos.offset(-radius, -radius, -radius),
                playerBlockPos.offset(radius, radius, radius))) {
            var state = level.getBlockState(candidate);
            if (state.hasProperty(dk.tij.winterweather.torch.TorchBlocks.LIT)
                    && !state.getValue(dk.tij.winterweather.torch.TorchBlocks.LIT)) {
                continue;
            }
            HeatSource source = heatSources.get(state.getBlock());
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
        double progress = Math.clamp(actualFreezeTicks / criticalFreezingTicks, 0, 1);
        return Math.clamp(
                (int) (interpolation.apply(progress) * MAX_FROZEN_TICKS + 0.5),
                0, MAX_FROZEN_TICKS
        );
    }

    public boolean isExtinguishable(BlockState state) {
        return extinguishableBurnoutSeconds.containsKey(state.getBlock());
    }

    public int extinguishableBurnoutSeconds(BlockState state) {
        return extinguishableBurnoutSeconds.getOrDefault(state.getBlock(), 0);
    }

    private static FreezingConfig fromJson(JsonObject root) {
        JsonObject frost = object(root, "frost");
        Map<Block, HeatSource> sources = new HashMap<>();
        JsonObject configuredSources = object(frost, "heat_sources");
        for (var entry : configuredSources.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            JsonObject source = entry.getValue().isJsonObject() ? entry.getValue().getAsJsonObject() : null;
            if (id == null || source == null || !BuiltInRegistries.BLOCK.containsKey(id)) continue;
            double heat = positive(source, "value", 1);
            double radius = positive(source, "radius", 3);
            BuiltInRegistries.BLOCK.get(id).ifPresent(holder ->
                    sources.put(holder.value(), new HeatSource(heat, radius * radius)));
        }
        if (sources.isEmpty()) return defaults();

        Map<Identifier, Double> armor = new HashMap<>();
        JsonObject armorObject = object(object(frost, "isolation"), "armor_pieces");
        for (var entry : armorObject.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id != null) armor.put(id, Math.max(0, Math.min(1, entry.getValue().getAsDouble() / 100)));
        }

        String interpolationName = string(frost, "interpolation_function", "smootherstep");
        Function<Double, Double> interpolation = switch (interpolationName) {
            case "linear" -> value -> value;
            case "smoothstep" -> Maths::smoothstep;
            default -> Maths::smootherstep;
        };
        JsonObject fire = root.has("extinguishable_fire")
                ? object(root, "extinguishable_fire")
                : object(root, "torches");
        Map<Block, Integer> extinguishable = new HashMap<>();
        JsonObject configuredBlocks = object(fire, "block_types");
        if (configuredBlocks.size() == 0) {
            configuredBlocks = object(fire, "torch_types");
        }
        for (var entry : configuredBlocks.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null) {
                continue;
            }
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                addExtinguishable(extinguishable, id.toString(),
                        positiveInt(value.getAsJsonObject(), "burnout_seconds", 86400));
            } else if (value.isJsonPrimitive() && value.getAsBoolean()) {
                addExtinguishable(extinguishable, id.toString(),
                        positiveInt(fire, "burnout_seconds", 86400));
            }
        }
        if (!root.has("extinguishable_fire") && !root.has("torches")) {
            extinguishable = defaults().extinguishableBurnoutSeconds();
        }
        return new FreezingConfig(
                root.has("enabled") && root.get("enabled").getAsBoolean(),
                positiveInt(frost, "critical_freezing_ticks", 1800),
                positive(frost, "player_radius", 5),
                positive(object(frost, "isolation"), "max_possible_isolation", 80) / 100,
                1 + positive(frost, "player_burning_boost", 10) / 100,
                1 + positive(frost, "player_powder_snow_boost", 0) / 100,
                interpolation, sources, armor, extinguishable,
                booleanValue(fire, "enabled", true),
                booleanValue(object(fire, "relight"), "flint_and_steel", true),
                positiveInt(object(fire, "relight"), "durability_cost", 1)
        );
    }

    private static JsonObject object(JsonObject parent, String key) {
        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }
    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }
    private static double positive(JsonObject object, String key, double fallback) {
        return object.has(key) ? Math.max(0, object.get(key).getAsDouble()) : fallback;
    }
    private static int positiveInt(JsonObject object, String key, int fallback) {
        return (int) positive(object, key, fallback);
    }

    private static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        return object.has(key) ? object.get(key).getAsBoolean() : fallback;
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

    private JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        JsonObject frost = new JsonObject();
        frost.addProperty("critical_freezing_ticks", criticalFreezingTicks);
        frost.addProperty("player_radius", playerRadius);
        frost.addProperty("interpolation_function", "smootherstep");
        frost.addProperty("player_burning_boost", (playerBurningBoost - 1) * 100);
        frost.addProperty("player_powder_snow_boost", (playerPowderSnowBoost - 1) * 100);
        JsonObject isolation = new JsonObject();
        isolation.addProperty("max_possible_isolation", maxPossibleIsolation * 100);
        JsonObject armor = new JsonObject();
        for (var entry : armorIsolation.entrySet()) armor.addProperty(entry.getKey().toString(), entry.getValue() * 100);
        isolation.add("armor_pieces", armor);
        frost.add("isolation", isolation);
        JsonObject sources = new JsonObject();
        for (var entry : heatSources.entrySet()) {
            JsonObject source = new JsonObject();
            source.addProperty("value", entry.getValue().heat());
            source.addProperty("radius", Math.sqrt(entry.getValue().radiusSquared()));
            sources.add(BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString(), source);
        }
        frost.add("heat_sources", sources);
        root.add("frost", frost);
        return root;
    }
}
