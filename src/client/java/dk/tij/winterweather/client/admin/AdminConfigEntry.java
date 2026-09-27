package dk.tij.winterweather.client.admin;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.Font;

import java.util.List;

/**
 * Base class for entries displayed in the admin configuration list.
 */
abstract class AdminConfigEntry
        extends ContainerObjectSelectionList.Entry<AdminConfigEntry> {

    protected final Font font;

    protected AdminConfigEntry(Font font) {
        this.font = font;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return List.of();
    }
}