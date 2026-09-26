package dk.tij.winterweather.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record WinterStartAnnouncementConfig(
        String chatMessage,
        List<String> chatExtraLines,
        String bannerTitle,
        String bannerSubtitle,
        List<String> bannerExtraLines
) {
    private static final Logger LOGGER = LoggerFactory.getLogger("WinterWeather");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("winterweather-announcement.json");
    private static final WinterStartAnnouncementConfig DEFAULTS = new WinterStartAnnouncementConfig(
            "❄ Winter has begun! ❄",
            List.of("The cold is settling in."),
            "WINTER HAS ARRIVED",
            "The cold is settling in",
            List.of("Prepare for the frost")
    );

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

            WinterStartAnnouncementConfig loaded = GSON.fromJson(
                    Files.readString(PATH), WinterStartAnnouncementConfig.class);
            if (loaded == null) return DEFAULTS;
            return new WinterStartAnnouncementConfig(
                    textOrDefault(loaded.chatMessage(), DEFAULTS.chatMessage(), 160),
                    linesOrDefault(loaded.chatExtraLines(), DEFAULTS.chatExtraLines(), 120),
                    textOrDefault(loaded.bannerTitle(), DEFAULTS.bannerTitle(), 48),
                    textOrDefault(loaded.bannerSubtitle(), DEFAULTS.bannerSubtitle(), 64),
                    linesOrDefault(loaded.bannerExtraLines(), DEFAULTS.bannerExtraLines(), 48)
            );
        } catch (Exception exception) {
            LOGGER.error("Could not load winter start announcement config at {}", PATH, exception);
            return DEFAULTS;
        }
    }

    private static String textOrDefault(String value, String fallback, int maxLength) {
        if (value == null || value.isBlank()) return fallback;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static List<String> linesOrDefault(List<String> lines, List<String> fallback, int maxLength) {
        if (lines == null) return fallback;
        return lines.stream()
                .filter(line -> line != null && !line.isBlank())
                .limit(4)
                .map(line -> line.length() <= maxLength ? line : line.substring(0, maxLength))
                .toList();
    }
}
