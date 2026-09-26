package dk.tij.winterweather.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dk.tij.winterweather.WinterWeather;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WinterWeatherConfigLoader {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private WinterWeatherConfigLoader() {}

    public static WinterWeatherConfig load(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root == null) {
                throw new ConfigParseException(
                        "Configuration file is empty: " + path
                );
            }

            return parse(root);
        } catch (IOException e) {
            throw new ConfigParseException(
                    "Failed to read configuration file: " + path,
                    e
            );
        } catch (JsonParseException e) {
            throw new ConfigParseException(
                    "Invalid JSON in configuration file: " + path,
                    e
            );
        }
    }

    private static WinterWeatherConfig parse(JsonObject root) {
        boolean enabled = getRequiredBoolean(root, "enabled");

        JsonObject frostJson = getRequiredObject(root, "frost");

        FrostConfig frost = parseFrost(frostJson);

        return new WinterWeatherConfig(
                enabled,
                frost
        );
    }

    private static FrostConfig parseFrost(JsonObject json) {
        int criticalFreezingTicks = getRequiredInt(json, "critical_freezing_ticks");

        double playerRadius = getRequiredDouble(json, "player_radius");

        String interpolationFunction = getRequiredString(json, "interpolation_function");

        double playerBurningBoost = getRequiredDouble(json, "player_burning_boost");

        double playerPowderSnowBoost = getRequiredDouble(json, "player_powder_snow_boost");

        IsolationConfig isolation = parseIsolation(getRequiredObject(json, "isolation"));

        HeatSourcesConfig heatSources = parseHeatSources(getRequiredObject(json, "heat_sources"));

        return new FrostConfig(
                criticalFreezingTicks,
                playerRadius,
                interpolationFunction,
                playerBurningBoost,
                playerPowderSnowBoost,
                isolation,
                heatSources
        );
    }

    private static IsolationConfig parseIsolation(JsonObject json) {
        double maxPossibleIsolation =
                getRequiredDouble(json, "max_possible_isolation");

        JsonObject armorJson =
                getRequiredObject(json, "armor_pieces");

        Map<String, Double> armorPieces = new java.util.LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : armorJson.entrySet()) {
            JsonElement value = entry.getValue();

            if (!value.isJsonPrimitive()
                    || !value.getAsJsonPrimitive().isNumber()) {

                throw new ConfigParseException(
                        "Armor isolation value must be a number for: "
                                + entry.getKey()
                );
            }

            armorPieces.put(
                    entry.getKey(),
                    value.getAsDouble()
            );
        }

        return new IsolationConfig(
                maxPossibleIsolation,
                Map.copyOf(armorPieces)
        );
    }

    private static HeatSourcesConfig parseHeatSources(JsonObject json) {
        boolean extinguishable =
                getRequiredBoolean(json, "extinguishable");

        boolean useUnlitState =
                getRequiredBoolean(json, "use_unlit_state");

        JsonObject relightJson =
                getRequiredObject(json, "relight");

        HeatSourcesConfig.RelightConfig relight =
                new HeatSourcesConfig.RelightConfig(
                        getRequiredBoolean(
                                relightJson,
                                "flint_and_steel"
                        ),
                        getRequiredInt(
                                relightJson,
                                "durability_cost"
                        )
                );

        var blocksJson =
                getRequiredArray(json, "blocks");

        List<HeatSourceConfig> blocks = new ArrayList<>();

        for (JsonElement element : blocksJson) {
            if (!element.isJsonObject()) {
                throw new ConfigParseException(
                        "Every heat source entry must be an object."
                );
            }

            blocks.add(parseHeatSource(element.getAsJsonObject()));
        }

        return new HeatSourcesConfig(
                extinguishable,
                useUnlitState,
                relight,
                List.copyOf(blocks)
        );
    }

    private static HeatSourceConfig parseHeatSource(JsonObject json) {
        double value =
                getRequiredDouble(json, "value");

        double radius =
                getRequiredDouble(json, "radius");

        int burnoutSeconds =
                getRequiredInt(json, "burnout_seconds");

        JsonElement variantsElement =
                json.get("variants");

        if (variantsElement == null) {
            throw new ConfigParseException(
                    "Missing required property: variants"
            );
        }

        List<HeatSourceVariant> variants =
                parseVariants(variantsElement);

        return new HeatSourceConfig(
                value,
                radius,
                burnoutSeconds,
                variants
        );
    }

    private static List<HeatSourceVariant> parseVariants(
            JsonElement element
    ) {
        List<HeatSourceVariant> variants = new ArrayList<>();

        if (element.isJsonArray()) {
            for (JsonElement variantElement : element.getAsJsonArray()) {
                if (!variantElement.isJsonPrimitive()
                        || !variantElement.getAsJsonPrimitive().isString()) {

                    throw new ConfigParseException(
                            "Array heat source variants must contain block IDs."
                    );
                }

                variants.add(
                        new HeatSourceVariant(
                                variantElement.getAsString()
                        )
                );
            }

            return List.copyOf(variants);
        }

        if (element.isJsonObject()) {
            JsonObject variantsObject =
                    element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry
                    : variantsObject.entrySet()) {

                JsonElement variantValue = entry.getValue();

                if (!variantValue.isJsonObject()) {
                    throw new ConfigParseException(
                            "Heat source variant configuration must be an object: "
                                    + entry.getKey()
                    );
                }

                variants.add(
                        new HeatSourceVariant(
                                entry.getKey()
                        )
                );
            }

            return List.copyOf(variants);
        }

        throw new ConfigParseException(
                "Heat source variants must be either an object or an array."
        );
    }

    public static void save(Path path, WinterWeatherConfig config) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(serialize(config)));
        } catch (IOException e) {
            throw new ConfigParseException(
                    "Failed to write configuration file: " + path,
                    e
            );
        }
    }

    private static JsonObject serialize(
            WinterWeatherConfig config
    ) {
        JsonObject root = new JsonObject();

        root.addProperty(
                "enabled",
                config.enabled()
        );

        root.add(
                "frost",
                serializeFrost(config.frost())
        );

        return root;
    }

    private static JsonObject serializeFrost(
            FrostConfig config
    ) {
        JsonObject json = new JsonObject();

        json.addProperty(
                "critical_freezing_ticks",
                config.criticalFreezingTicks()
        );

        json.addProperty(
                "player_radius",
                config.playerRadius()
        );

        json.addProperty(
                "interpolation_function",
                config.interpolationFunction()
        );

        json.addProperty(
                "player_burning_boost",
                config.playerBurningBoost()
        );

        json.addProperty(
                "player_powder_snow_boost",
                config.playerPowderSnowBoost()
        );

        json.add(
                "isolation",
                serializeIsolation(config.isolation())
        );

        json.add(
                "heat_sources",
                serializeHeatSources(config.heatSources())
        );

        return json;
    }

    private static JsonObject serializeIsolation(
            IsolationConfig config
    ) {
        JsonObject json = new JsonObject();

        json.addProperty(
                "max_possible_isolation",
                config.maxPossibleIsolation()
        );

        JsonObject armor = new JsonObject();

        for (Map.Entry<String, Double> entry
                : config.armorPieces().entrySet()) {

            armor.addProperty(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        json.add(
                "armor_pieces",
                armor
        );

        return json;
    }

    private static JsonObject serializeHeatSources(
            HeatSourcesConfig config
    ) {
        JsonObject json = new JsonObject();

        json.addProperty(
                "extinguishable",
                config.extinguishable()
        );

        json.addProperty(
                "use_unlit_state",
                config.useUnlitState()
        );

        JsonObject relight = new JsonObject();

        relight.addProperty(
                "flint_and_steel",
                config.relight().flintAndSteel()
        );

        relight.addProperty(
                "durability_cost",
                config.relight().durabilityCost()
        );

        json.add(
                "relight",
                relight
        );

        var blocks = new com.google.gson.JsonArray();

        for (HeatSourceConfig heatSource : config.blocks()) {
            blocks.add(serializeHeatSource(heatSource));
        }

        json.add(
                "blocks",
                blocks
        );

        return json;
    }

    private static JsonObject serializeHeatSource(
            HeatSourceConfig config
    ) {
        JsonObject json = new JsonObject();

        json.addProperty(
                "value",
                config.value()
        );

        json.addProperty(
                "radius",
                config.radius()
        );

        json.addProperty(
                "burnout_seconds",
                config.burnoutSeconds()
        );

        JsonArray variantArray = new JsonArray();
        for (HeatSourceVariant variant : config.variants()) {
            variantArray.add(variant.blockId());
        }
        json.add("variants", variantArray);

        return json;
    }

    private static String getRequiredString(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new ConfigParseException("Property \"" + property + "\" must be a string.");
        }

        return element.getAsString();
    }

    private static boolean getRequiredBoolean(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new ConfigParseException("Property \"" + property + "\" must be a boolean.");
        }

        return element.getAsBoolean();
    }

    private static int getRequiredInt(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new ConfigParseException("Property \"" + property + "\" must be a integer.");
        }

        return element.getAsInt();
    }

    private static double getRequiredDouble(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new ConfigParseException("Property \"" + property + "\" must be a number.");
        }

        return element.getAsDouble();
    }

    private static JsonObject getRequiredObject(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonObject()) {
            throw new ConfigParseException("Property \"" + property + "\" must be an object.");
        }

        return element.getAsJsonObject();
    }

    private static JsonArray getRequiredArray(JsonObject object, String property) {
        assertNotMissing(object, property);

        JsonElement element = object.get(property);

        if (!element.isJsonArray()) {
            throw new ConfigParseException("Property \"" + property + "\" must be an array.");
        }

        return element.getAsJsonArray();
    }

    private static void assertNotMissing(JsonObject object, String property) {
        if (!object.has(property) || object.get(property).isJsonNull()) {
            throw missing(property);
        }
    }

    private static ConfigParseException missing(String property) {
        return new ConfigParseException(
                "Missing required configuration property: " + property
        );
    }
}
