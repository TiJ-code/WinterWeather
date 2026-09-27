package dk.tij.winterweather.client.admin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Secondary informational entry displayed below a configuration heading.
 */
final class AdminConfigDetailEntry extends AdminConfigEntry {

    private static final int TEXT_OFFSET_X = 8;
    private static final int TEXT_OFFSET_Y = 1;

    private final Component text;

    AdminConfigDetailEntry(Font font, Component text) {
        super(font);
        this.text = text;
    }

    @Override
    public void extractContent(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
    ) {
        int x = getContentX() + TEXT_OFFSET_X;
        int y = getContentY() + TEXT_OFFSET_Y;

        List<FormattedCharSequence> lines =
                font.split(
                        text,
                        getContentWidth() - TEXT_OFFSET_X
                );

        for (int i = 0; i < lines.size(); i++) {
            graphics.text(
                    font,
                    lines.get(i),
                    x,
                    y + i * 9,
                    0xFFB9C9D4,
                    false
            );
        }
    }

    @Override
    public int getHeight() {
        return Math.max(
                16,
                font.split(
                        text,
                        getContentWidth() - TEXT_OFFSET_X
                ).size() * 9 + 2
        );
    }
}