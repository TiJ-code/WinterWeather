package dk.tij.winterweather.client.admin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.Font;

/**
 * Scrollable list used by the admin configuration screen.
 */
final class AdminConfigList
        extends ContainerObjectSelectionList<AdminConfigEntry> {

    static final int PANEL_WIDTH = 360;
    static final int ROW_WIDTH = 326;

    AdminConfigList(
            int width,
            int height,
            Font font
    ) {
        super(
                Minecraft.getInstance(),
                PANEL_WIDTH,
                Math.max(80, height - 95),
                39,
                25
        );

        setX(
                width / 2 - PANEL_WIDTH / 2
        );
    }

    @Override
    public int getRowWidth() {
        return ROW_WIDTH;
    }

    void add(AdminConfigEntry entry) {
        addEntry(entry);
    }
}