package dk.tij.winterweather.client;

import dk.tij.winterweather.announcement.WinterWeatherBranding;
import dk.tij.winterweather.config.WinterStartAnnouncementConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Provides winter start animation functionality for Winter Weather.
 */
public final class WinterStartAnimation {
    /**
     * Performs the parse operation.
     */
    private static final Identifier HUD_ID = Identifier.parse("winterweather:winter_start_banner");
    private static final int PANEL_WIDTH = 360;
    private static final long ENTRANCE_NANOS = 500_000_000L;
    private static final long EXIT_NANOS = 1_100_000_000L;
    /**
     * Performs the winter start animation operation.
     */
    private static final WinterStartAnimation INSTANCE = new WinterStartAnimation();
    private long startedAtNanos = -1;
    /**
     * Stores the screen value.
     */
    private WinterStartAnnouncementConfig.Screen screen;

    /**
     * Performs the winter start animation operation.
     */
    private WinterStartAnimation() {
    }

    /**
     * Performs the register operation.
     */
    public static void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, HUD_ID, INSTANCE::render);
    }

    /**
     * Performs the trigger operation.
     *
     * @param announcement the announcement value
     */
    public static void trigger(WinterStartAnnouncementConfig announcement) {
        INSTANCE.screen = announcement.screen();
        INSTANCE.startedAtNanos = System.nanoTime();
    }

    /**
     * Performs the draw snowburst operation.
     *
     * @param graphics      the graphics value
     * @param now           the now value
     * @param centerX       the center x value
     * @param centerY       the center y value
     * @param width         the width value
     * @param height        the height value
     * @param alpha         the alpha value
     * @param particleCount the particle count value
     * @param accent        the accent value
     */
    private static void drawSnowburst(GuiGraphicsExtractor graphics, long now, int centerX,
                                      int centerY, int width, int height, int alpha,
                                      int particleCount, int accent) {
        for (int index = 0; index < particleCount; index++) {
            double phase = index * 1.73 + now / 900_000_000.0;
            int x = centerX + (int) (Math.sin(phase) * (width * 0.68));
            int y = centerY + (int) (Math.cos(phase * 0.73) * (height * 0.66 + 20));
            int particleAlpha = Math.round(alpha * (0.25f + 0.45f
                    * (0.5f + 0.5f * (float) Math.sin(phase * 1.8))));
            int size = index % 4 == 0 ? 3 : 2;
            graphics.fill(x - size, y, x + size + 1, y + 1, color(particleAlpha, accent));
            graphics.fill(x, y - size, x + 1, y + size + 1, color(particleAlpha, accent));
        }
    }

    /**
     * Performs the parse color operation.
     *
     * @param value the value value
     */
    private static int parseColor(String value) {
        try {
            return Integer.parseInt(value, 16);
        } catch (NumberFormatException exception) {
            return 0x65DFFF;
        }
    }

    /**
     * Performs the color operation.
     *
     * @param alpha the alpha value
     * @param rgb   the rgb value
     */
    private static int color(int alpha, int rgb) {
        return (Math.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Performs the ease out operation.
     *
     * @param value the value value
     */
    private static float easeOut(float value) {
        float inverse = 1 - value;
        return 1 - inverse * inverse * inverse;
    }

    /**
     * Performs the render operation.
     *
     * @param graphics     the graphics value
     * @param deltaTracker the delta tracker value
     */
    private void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (startedAtNanos < 0 || screen == null) return;
        long now = System.nanoTime();
        long duration = screen.animation().durationMs() * 1_000_000L;
        long elapsed = now - startedAtNanos;
        if (elapsed >= duration) {
            startedAtNanos = -1;
            return;
        }
        float time = (float) elapsed / duration;
        float fadeIn = easeOut(Math.min(1, (float) elapsed / ENTRANCE_NANOS));
        float fadeOut = Math.min(1, Math.max(0, (float) (duration - elapsed) / EXIT_NANOS));
        int alpha = Math.round(255 * fadeIn * fadeOut);
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        int width = Math.min(PANEL_WIDTH, Math.max(220, graphics.guiWidth() - 32));
        int height = 80 + screen.extraLines().size() * 14;
        int entranceOffset = Math.round((1 - fadeIn) * 42);
        int top = centerY - height / 2 + entranceOffset;
        int left = centerX - width / 2;
        int right = centerX + width / 2;
        int accent = parseColor(screen.animation().accentColor());
        if (screen.animation().type().equalsIgnoreCase("snowburst")) {
            drawSnowburst(graphics, now, centerX, centerY, width, height, alpha,
                    screen.animation().particleCount(), accent);
        }
        graphics.fill(left - 5, top - 5, right + 5, top + height + 5,
                color(Math.round(alpha * 0.34f), accent));
        graphics.fill(left - 2, top - 2, right + 2, top + height + 2,
                color(alpha, 0xB5F2FF));
        graphics.fillGradient(left, top, right, top + height,
                color(Math.round(alpha * 0.96f), 0x102B49), color(Math.round(alpha * 0.94f), 0x050F1D));
        graphics.fill(left + 2, top + 2, right - 2, top + 4, color(alpha, accent));
        graphics.fill(left + 2, top + height - 4, right - 2, top + height - 2,
                color(Math.round(alpha * 0.9f), accent));
        int shimmer = (int) ((time * (width + 120)) % (width + 120));
        int shimmerX = left + shimmer - 60;
        graphics.fill(shimmerX, top + 5, shimmerX + 42, top + 7,
                color(Math.round(alpha * 0.75f), 0xFFFFFF));
        float pulse = 0.8f + (float) Math.sin(now / 210_000_000.0) * 0.2f;
        int snowColor = color(Math.round(alpha * pulse), 0xDDF8FF);
        graphics.centeredText(Minecraft.getInstance().font, "❄", left + 27, top + 19, snowColor);
        graphics.centeredText(Minecraft.getInstance().font, "❄", right - 27, top + 19, snowColor);
        graphics.centeredText(Minecraft.getInstance().font, screen.title(), centerX, top + 14,
                color(alpha, 0xF1FCFF));
        graphics.centeredText(Minecraft.getInstance().font, screen.subtitle(), centerX, top + 34,
                color(Math.round(alpha * 0.82f), 0xA9D8EF));
        List<String> extraLines = screen.extraLines();
        for (int index = 0; index < extraLines.size(); index++) {
            int lineAlpha = Math.round(alpha * (1 - index * 0.08f));
            graphics.centeredText(Minecraft.getInstance().font, extraLines.get(index), centerX,
                    top + 51 + index * 14, color(lineAlpha, 0xD2EFFF));
        }
        graphics.centeredText(Minecraft.getInstance().font, WinterWeatherBranding.screenAttribution(), centerX,
                top + height - 17, color(Math.round(alpha * 0.68f), 0x83BEDA));
    }
}
