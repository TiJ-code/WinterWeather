package dk.tij.winterweather.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Configuration values for winter start announcement.
 */
public record WinterStartAnnouncementConfig(
        @SerializedName("schema_version") int schemaVersion,
        Chat chat,
        Screen screen
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("winterweather-announcement.json");
    private static final WinterStartAnnouncementConfig DEFAULTS = new WinterStartAnnouncementConfig(
            CURRENT_SCHEMA_VERSION,
            new Chat("❄ Winter has begun! ❄", List.of("The cold is settling in.")),
            new Screen("WINTER HAS ARRIVED", "The cold is settling in",
                    List.of("Prepare for the frost"),
                    new Animation("snowburst", 7000, 14, "65DFFF"))
    );

    /**
     * Performs the load operation.
     */
    public static WinterStartAnnouncementConfig load() {
        try {
            if (Files.notExists(PATH)) {
                Files.createDirectories(PATH.getParent());
                try (InputStream input = WinterStartAnnouncementConfig.class
                        .getResourceAsStream("/config/winterweather-announcement.json")) {
                    if (input == null) {
                        Files.writeString(PATH, GSON.toJson(DEFAULTS));
                    } else {
                        Files.copy(input, PATH);
                    }
                }
            }

            String json = Files.readString(PATH);
            boolean legacy = isLegacy(json);
            WinterStartAnnouncementConfig config = decode(json);
            if (legacy) Files.writeString(PATH, config.toJson());
            return config;
        } catch (Exception exception) {
            LOGGER.error("Could not load winter start announcement config at {}", PATH, exception);
            return DEFAULTS;
        }
    }

    /**
     * Performs the decode operation.
     *
     * @param json the json value
     */
    public static WinterStartAnnouncementConfig decode(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            WinterStartAnnouncementConfig parsed = isLegacy(root)
                    ? migrateLegacy(root)
                    : GSON.fromJson(root, WinterStartAnnouncementConfig.class);
            return normalize(parsed);
        } catch (RuntimeException exception) {
            LOGGER.warn("Invalid winter start announcement document; using defaults", exception);
            return DEFAULTS;
        }
    }

    /**
     * Performs the normalize operation.
     *
     * @param config the config value
     */
    private static WinterStartAnnouncementConfig normalize(WinterStartAnnouncementConfig config) {
        if (config == null) return DEFAULTS;
        Chat chat = config.chat() == null ? DEFAULTS.chat() : new Chat(
                textOrDefault(config.chat().message(), DEFAULTS.chat().message(), 160),
                linesOrDefault(config.chat().extraLines(), DEFAULTS.chat().extraLines(), 120));
        Screen screen = config.screen() == null ? DEFAULTS.screen() : config.screen();
        Animation animation = screen.animation() == null ? DEFAULTS.screen().animation() : screen.animation();
        animation = new Animation(
                textOrDefault(animation.type(), DEFAULTS.screen().animation().type(), 32),
                Math.clamp(animation.durationMs(), 1000, 30000),
                Math.clamp(animation.particleCount(), 0, 64),
                validColor(animation.accentColor())
                        ? animation.accentColor().toUpperCase(java.util.Locale.ROOT)
                        : DEFAULTS.screen().animation().accentColor());
        screen = new Screen(
                textOrDefault(screen.title(), DEFAULTS.screen().title(), 48),
                textOrDefault(screen.subtitle(), DEFAULTS.screen().subtitle(), 64),
                linesOrDefault(screen.extraLines(), DEFAULTS.screen().extraLines(), 48),
                animation);
        return new WinterStartAnnouncementConfig(
                CURRENT_SCHEMA_VERSION,
                chat,
                screen);
    }

    /**
     * Performs the migrate legacy operation.
     *
     * @param old the old value
     */
    private static WinterStartAnnouncementConfig migrateLegacy(JsonObject old) {
        String chatMessage = stringOrNull(old, "chatMessage");
        String title = stringOrNull(old, "bannerTitle");
        String subtitle = stringOrNull(old, "bannerSubtitle");
        List<String> chatLines = stringsOrNull(old, "chatExtraLines");
        List<String> bannerLines = stringsOrNull(old, "bannerExtraLines");
        return new WinterStartAnnouncementConfig(CURRENT_SCHEMA_VERSION,
                new Chat(chatMessage, chatLines),
                new Screen(title, subtitle, bannerLines, DEFAULTS.screen().animation()));
    }

    /**
     * Reports whether legacy is true.
     *
     * @param json the json value
     */
    private static boolean isLegacy(String json) {
        return isLegacy(JsonParser.parseString(json).getAsJsonObject());
    }

    /**
     * Reports whether legacy is true.
     *
     * @param root the root value
     */
    private static boolean isLegacy(JsonObject root) {
        return !root.has("schema_version")
                && (root.has("chatMessage") || root.has("bannerTitle") || root.has("bannerSubtitle"));
    }

    /**
     * Performs the string or null operation.
     *
     * @param object the object value
     * @param key    the key value
     */
    private static String stringOrNull(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive()
                ? object.get(key).getAsString() : null;
    }

    /**
     * Performs the strings or null operation.
     *
     * @param object the object value
     * @param key    the key value
     */
    private static List<String> stringsOrNull(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) return null;
        return GSON.fromJson(object.getAsJsonArray(key),
                new com.google.gson.reflect.TypeToken<List<String>>() {
                }.getType());
    }

    /**
     * Performs the text or default operation.
     *
     * @param value     the value value
     * @param fallback  the fallback value
     * @param maxLength the max length value
     */
    private static String textOrDefault(String value, String fallback, int maxLength) {
        if (value == null || value.isBlank()) return fallback;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * Performs the lines or default operation.
     *
     * @param lines     the lines value
     * @param fallback  the fallback value
     * @param maxLength the max length value
     */
    private static List<String> linesOrDefault(List<String> lines, List<String> fallback, int maxLength) {
        if (lines == null) return fallback;
        return lines.stream()
                .filter(line -> line != null && !line.isBlank())
                .limit(4)
                .map(line -> line.length() <= maxLength ? line : line.substring(0, maxLength))
                .toList();
    }

    /**
     * Performs the valid color operation.
     *
     * @param value the value value
     */
    private static boolean validColor(String value) {
        return value != null && value.matches("(?i)[0-9a-f]{6}");
    }

    /**
     * Performs the to json operation.
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * Returns the logger value.
     *
     * @param CURRENT_SCHEMA_VERSION the current schema version value
     * @param message                the message value
     */
    public record Chat(String message, @SerializedName("extra_lines") List<String> extraLines) {
    }

    /**
     * Performs the screen operation.
     *
     * @param title    the title value
     * @param subtitle the subtitle value
     */
    public record Screen(
            String title,
            String subtitle,
            @SerializedName("extra_lines") List<String> extraLines,
            Animation animation
    ) {
    }

    /**
     * Performs the animation operation.
     *
     * @param type the type value
     */
    public record Animation(
            String type,
            @SerializedName("duration_ms") int durationMs,
            @SerializedName("particle_count") int particleCount,
            @SerializedName("accent_color") String accentColor
    ) {
    }
}
