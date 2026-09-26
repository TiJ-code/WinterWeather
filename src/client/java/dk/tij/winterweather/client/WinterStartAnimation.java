package dk.tij.winterweather.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class WinterStartAnimation {
    private static final long DURATION_NANOS = 7_000_000_000L;
    private static long startedAtNanos = -1;
    private static String title = "WINTER HAS ARRIVED";
    private static String subtitle = "The cold is settling in";
    private static List<String> extraLines = List.of("Prepare for the frost");

    private WinterStartAnimation() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.parse("winterweather:winter_start_banner"),
                WinterStartAnimation::render
        );
    }

    public static void trigger(String newTitle, String newSubtitle, List<String> lines) {
        title = newTitle;
        subtitle = newSubtitle;
        extraLines = List.copyOf(lines);
        startedAtNanos = System.nanoTime();
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (startedAtNanos < 0) return;

        long now = System.nanoTime();
        long elapsed = now - startedAtNanos;
        if (elapsed >= DURATION_NANOS) {
            startedAtNanos = -1;
            return;
        }

        float time = (float) elapsed / DURATION_NANOS;
        float fadeIn = easeOut(Math.min(1, elapsed / 500_000_000f));
        float fadeOut = Math.min(1, Math.max(0, (DURATION_NANOS - elapsed) / 1_100_000_000f));
        int alpha = Math.round(255 * fadeIn * fadeOut);
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        int width = 360;
        int height = 80 + extraLines.size() * 14;
        int entranceOffset = Math.round((1 - fadeIn) * 42);
        int top = centerY - height / 2 + entranceOffset;
        int left = centerX - width / 2;
        int right = centerX + width / 2;

        drawSnowburst(graphics, now, centerX, centerY, width, height, alpha);
        graphics.fill(left - 5, top - 5, right + 5, top + height + 5, color(Math.round(alpha * 0.34f), 0x65DFFF));
        graphics.fill(left - 2, top - 2, right + 2, top + height + 2, color(alpha, 0xB5F2FF));
        graphics.fillGradient(left, top, right, top + height,
                color(Math.round(alpha * 0.96f), 0x102B49), color(Math.round(alpha * 0.94f), 0x050F1D));
        graphics.fill(left + 2, top + 2, right - 2, top + 4, color(alpha, 0xE0FBFF));
        graphics.fill(left + 2, top + height - 4, right - 2, top + height - 2,
                color(Math.round(alpha * 0.9f), 0x66C9F2));

        int shimmer = (int) ((time * (width + 120)) % (width + 120));
        int shimmerX = left + shimmer - 60;
        graphics.fill(shimmerX, top + 5, shimmerX + 42, top + 7,
                color(Math.round(alpha * 0.75f), 0xFFFFFF));

        float pulse = 0.8f + (float) Math.sin(now / 210_000_000.0) * 0.2f;
        int snowColor = color(Math.round(alpha * pulse), 0xDDF8FF);
        int textColor = color(alpha, 0xF1FCFF);
        int subtitleColor = color(Math.round(alpha * 0.82f), 0xA9D8EF);
        graphics.centeredText(Minecraft.getInstance().font, "❄", left + 27, top + 19, snowColor);
        graphics.centeredText(Minecraft.getInstance().font, "❄", right - 27, top + 19, snowColor);
        graphics.centeredText(Minecraft.getInstance().font, title, centerX, top + 14, textColor);
        graphics.centeredText(Minecraft.getInstance().font, subtitle, centerX, top + 34, subtitleColor);

        for (int index = 0; index < extraLines.size(); index++) {
            int lineAlpha = Math.round(alpha * (1 - index * 0.08f));
            graphics.centeredText(Minecraft.getInstance().font, extraLines.get(index), centerX,
                    top + 51 + index * 14, color(lineAlpha, 0xD2EFFF));
        }

        String author = "CREATED BY TiJ-code  •  github.com/TiJ-code/WinterWeather";
        graphics.centeredText(Minecraft.getInstance().font, author, centerX,
                top + height - 17, color(Math.round(alpha * 0.68f), 0x83BEDA));
    }

    private static void drawSnowburst(GuiGraphicsExtractor graphics, long now, int centerX,
                                      int centerY, int width, int height, int alpha) {
        for (int index = 0; index < 14; index++) {
            double phase = index * 1.73 + now / 900_000_000.0;
            int x = centerX + (int) (Math.sin(phase) * (width * 0.68));
            int y = centerY + (int) (Math.cos(phase * 0.73) * (height * 0.66 + 20));
            int particleAlpha = Math.round(alpha * (0.25f + 0.45f
                    * (0.5f + 0.5f * (float) Math.sin(phase * 1.8))));
            int size = index % 4 == 0 ? 3 : 2;
            graphics.fill(x - size, y, x + size + 1, y + 1, color(particleAlpha, 0xBCEEFF));
            graphics.fill(x, y - size, x + 1, y + size + 1, color(particleAlpha, 0xBCEEFF));
        }
    }

    private static int color(int alpha, int rgb) {
        return (Math.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    private static float easeOut(float value) {
        float inverse = 1 - value;
        return 1 - inverse * inverse * inverse;
    }
}
