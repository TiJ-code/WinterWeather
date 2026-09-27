package dk.tij.winterweather.client.admin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * Section heading displayed in the admin configuration list.
 */
final class AdminConfigHeaderEntry extends AdminConfigEntry {

    private static final int DECORATOR_WIDTH = 2;
    private static final int DECORATOR_GAP = 6;

    private final Component heading;

    AdminConfigHeaderEntry(Font font, Component heading) {
        super(font);
        this.heading = heading;
    }

    @Override
    public void extractContent(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
    ) {
        int x = getContentX();
        int y = getContentY();

        int decoratorY = y + 3;
        int decoratorHeight = 14;

        graphics.fill(
                x,
                decoratorY,
                x + DECORATOR_WIDTH,
                decoratorY + decoratorHeight,
                0xFF55C9E8
        );

        int textX = x + DECORATOR_WIDTH + DECORATOR_GAP;
        int textY = y + 5;

        graphics.text(
                font,
                heading,
                textX,
                textY,
                0xFF9BCFE0,
                false
        );
    }

    @Override
    public int getHeight() {
        return 21;
    }
}