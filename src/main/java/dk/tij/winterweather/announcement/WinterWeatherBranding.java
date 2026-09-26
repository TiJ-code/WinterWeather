package dk.tij.winterweather.announcement;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.net.URI;

/**
 * Provides winter weather branding functionality for Winter Weather.
 */
public final class WinterWeatherBranding {
    public static final String AUTHOR = "TiJ-code";
    public static final URI PROJECT_URI = URI.create("https://github.com/TiJ-code/WinterWeather");
    public static final String PROJECT_URL = "github.com/TiJ-code/WinterWeather";

    /**
     * Performs the winter weather branding operation.
     */
    private WinterWeatherBranding() {
    }

    /**
     * Performs the chat attribution operation.
     */
    public static Component chatAttribution() {
        return Component.literal("Created by ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(AUTHOR).withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(PROJECT_URI))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Open the WinterWeather repository")))));
    }

    /**
     * Performs the screen attribution operation.
     */
    public static String screenAttribution() {
        return "CREATED BY " + AUTHOR + "  •  " + PROJECT_URL;
    }
}
