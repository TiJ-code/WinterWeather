package dk.tij.winterweather.client.admin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * Editable value row displayed in the admin configuration list.
 * <p>
 * Supports both:
 * - numeric +/- controls
 * - ON/OFF toggle controls
 * </p>
 */
final class AdminConfigValueEntry extends AdminConfigEntry {

    private static final int ROW_HEIGHT = 25;

    private static final int CONTENT_PADDING = 7;

    private static final int CONTROL_SIZE = 19;
    private static final int CONTROL_GAP = 4;

    private static final int TOGGLE_WIDTH = 42;

    private final String label;
    private final Supplier<String> valueText;
    private final Runnable decrease;
    private final Runnable increase;
    private final boolean toggle;

    AdminConfigValueEntry(
            Font font,
            String label,
            Supplier<String> valueText,
            Runnable decrease,
            Runnable increase,
            boolean toggle
    ) {
        super(font);

        this.label = label;
        this.valueText = valueText;
        this.decrease = decrease;
        this.increase = increase;
        this.toggle = toggle;
    }

    @Override
    public void extractContent(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick
    ) {
        int left = getContentX();
        int top = getContentY();
        int right = getContentRight();
        int bottom = getContentBottom();

        graphics.fill(
                left,
                top,
                right,
                bottom,
                hovered
                        ? 0xFF263B49
                        : 0xA0182630
        );

        int rowHeight = bottom - top;

        /*
         * Vertically center the controls inside the row.
         *
         * +1 is an optical correction because the button has
         * a 1px highlight line at its top.
         */
        int controlTop =
                top + (rowHeight - CONTROL_SIZE) / 2 + 1;

        int controlRight =
                right - CONTENT_PADDING;

        String value = valueText.get();

        if (toggle) {
            drawToggle(
                    graphics,
                    controlRight - TOGGLE_WIDTH,
                    controlTop,
                    value,
                    mouseX,
                    mouseY
            );

            drawLabel(
                    graphics,
                    left,
                    top
            );

            return;
        }

        int plusX =
                controlRight - CONTROL_SIZE;

        int minusX =
                plusX - CONTROL_GAP - CONTROL_SIZE;

        drawControl(
                graphics,
                minusX,
                controlTop,
                "−",
                mouseX,
                mouseY
        );

        drawControl(
                graphics,
                plusX,
                controlTop,
                "+",
                mouseX,
                mouseY
        );

        int valueRight =
                minusX - CONTROL_GAP;

        int valueWidth =
                font.width(value);

        graphics.text(
                font,
                Component.literal(value),
                valueRight - valueWidth,
                top + 8,
                0xFF9EDCEF,
                false
        );

        int labelRight =
                valueRight - valueWidth - 8;

        String visibleLabel =
                truncate(
                        label,
                        Math.max(
                                0,
                                labelRight - left - CONTENT_PADDING
                        )
                );

        graphics.text(
                font,
                Component.literal(visibleLabel),
                left + CONTENT_PADDING,
                top + 8,
                0xFFE4EDF2,
                false
        );
    }

    private void drawLabel(
            GuiGraphicsExtractor graphics,
            int left,
            int top
    ) {
        graphics.text(
                font,
                Component.literal(label),
                left + CONTENT_PADDING,
                top + 8,
                0xFFE4EDF2,
                false
        );
    }

    private void drawToggle(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            String value,
            int mouseX,
            int mouseY
    ) {
        int height = CONTROL_SIZE;

        boolean hovered =
                mouseX >= x &&
                        mouseX <= x + TOGGLE_WIDTH &&
                        mouseY >= y &&
                        mouseY <= y + height;

        int background;

        if (value.equals("ON")) {
            background =
                    hovered
                            ? 0xFF329078
                            : 0xFF287C66;
        } else {
            background =
                    hovered
                            ? 0xFF596772
                            : 0xFF4A5660;
        }

        graphics.fill(
                x,
                y,
                x + TOGGLE_WIDTH,
                y + height,
                background
        );

        graphics.fill(
                x,
                y,
                x + TOGGLE_WIDTH,
                y + 1,
                0xFF72DDF2
        );

        int textWidth =
                font.width(value);

        graphics.text(
                font,
                Component.literal(value),
                x + (TOGGLE_WIDTH - textWidth) / 2,
                y + 5,
                0xFFF1F7FA,
                false
        );
    }

    private void drawControl(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            String text,
            int mouseX,
            int mouseY
    ) {
        boolean hovered =
                mouseX >= x &&
                        mouseX <= x + CONTROL_SIZE &&
                        mouseY >= y &&
                        mouseY <= y + CONTROL_SIZE;

        int background =
                hovered
                        ? 0xFF287C91
                        : 0xFF185467;

        graphics.fill(
                x,
                y,
                x + CONTROL_SIZE,
                y + CONTROL_SIZE,
                background
        );

        graphics.fill(
                x,
                y,
                x + CONTROL_SIZE,
                y + 1,
                0xFF72DDF2
        );

        int textWidth =
                font.width(text);

        graphics.text(
                font,
                Component.literal(text),
                x + (CONTROL_SIZE - textWidth) / 2,
                y + 5,
                0xFFF1F7FA,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return false;
        }

        int right =
                getContentRight();

        int controlRight =
                right - CONTENT_PADDING;

        int top =
                getContentY();

        int bottom =
                getContentBottom();

        int rowHeight =
                bottom - top;

        /*
         * Must be identical to the rendering calculation.
         */
        int controlTop =
                top + (rowHeight - CONTROL_SIZE) / 2 + 1;

        if (toggle) {
            int toggleLeft =
                    controlRight - TOGGLE_WIDTH;

            if (event.x() >= toggleLeft &&
                    event.x() <= controlRight &&
                    event.y() >= controlTop &&
                    event.y() <= controlTop + CONTROL_SIZE) {

                increase.run();
                return true;
            }

            return false;
        }

        int plusX =
                controlRight - CONTROL_SIZE;

        int minusX =
                plusX - CONTROL_GAP - CONTROL_SIZE;

        if (event.y() < controlTop ||
                event.y() > controlTop + CONTROL_SIZE) {
            return false;
        }

        if (event.x() >= minusX &&
                event.x() < minusX + CONTROL_SIZE) {

            decrease.run();
            return true;
        }

        if (event.x() >= plusX &&
                event.x() <= plusX + CONTROL_SIZE) {

            increase.run();
            return true;
        }

        return false;
    }

    @Override
    public int getHeight() {
        return ROW_HEIGHT;
    }

    private String truncate(
            String text,
            int maxWidth
    ) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String shortened = text;

        while (!shortened.isEmpty() &&
                font.width(shortened + "…") > maxWidth) {

            shortened =
                    shortened.substring(
                            0,
                            shortened.length() - 1
                    );
        }

        return shortened.isEmpty()
                ? ""
                : shortened + "…";
    }
}