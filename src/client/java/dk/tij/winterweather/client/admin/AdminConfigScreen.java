package dk.tij.winterweather.client.admin;

import dk.tij.winterweather.network.ConfigPayload;
import dk.tij.winterweather.network.HeatSourceGroupPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Scrollable, server-backed editor for winter settings
 * and every configured heat block.
 */
public final class AdminConfigScreen extends Screen {

    private static final int PANEL_WIDTH =
            AdminConfigList.PANEL_WIDTH;

    private ConfigPayload draft;
    private AdminConfigList settings;

    public AdminConfigScreen(ConfigPayload config) {
        super(
                Component.literal(
                        "Winter Weather • Admin"
                )
        );

        this.draft = config;
    }

    @Override
    protected void init() {
        Font font =
                Minecraft.getInstance().font;

        settings =
                addRenderableWidget(
                        new AdminConfigList(
                                width,
                                height,
                                font
                        )
                );

        buildGeneralSettings(font);
        buildHeatSourceSettings(font);
    }

    private void buildGeneralSettings(Font font) {
        settings.add(
                header(
                        font,
                        "WINTER CONTROL"
                )
        );

        settings.add(
                toggleRow(
                        "Winter system",
                        () -> draft.enabled(),
                        () -> update(
                                old -> copy(
                                        old,
                                        !old.enabled(),
                                        old.heatSourcesEnabled()
                                )
                        )
                )
        );

        settings.add(
                toggleRow(
                        "Heat sources extinguishable",
                        () -> draft.heatSourcesEnabled(),
                        () -> update(
                                old -> copy(
                                        old,
                                        old.enabled(),
                                        !old.heatSourcesEnabled()
                                )
                        )
                )
        );

        settings.add(
                header(
                        font,
                        "FREEZING & INSULATION"
                )
        );

        settings.add(
                valueRow(
                        "Critical freezing time",
                        () -> draft.criticalFreezingTicks(),
                        100,
                        value ->
                                Math.round(value) + " ticks",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        clamp(
                                                                (int) value,
                                                                100,
                                                                12000
                                                        ),
                                                        old.playerRadius(),
                                                        old.maxPossibleIsolation(),
                                                        old.playerBurningBoost(),
                                                        old.playerPowderSnowBoost(),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        old.glowstoneDurationMultiplier(),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );

        settings.add(
                valueRow(
                        "Player heat radius",
                        () -> draft.playerRadius(),
                        0.5,
                        value ->
                                fmt(value) + " blocks",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        old.criticalFreezingTicks(),
                                                        clamp(
                                                                value,
                                                                0,
                                                                128
                                                        ),
                                                        old.maxPossibleIsolation(),
                                                        old.playerBurningBoost(),
                                                        old.playerPowderSnowBoost(),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        old.glowstoneDurationMultiplier(),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );

        settings.add(
                valueRow(
                        "Maximum insulation",
                        () -> draft.maxPossibleIsolation(),
                        0.05,
                        value ->
                                Math.round(value * 100) + "%",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        old.criticalFreezingTicks(),
                                                        old.playerRadius(),
                                                        clamp(
                                                                value,
                                                                0,
                                                                1
                                                        ),
                                                        old.playerBurningBoost(),
                                                        old.playerPowderSnowBoost(),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        old.glowstoneDurationMultiplier(),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );

        settings.add(
                valueRow(
                        "Burning boost",
                        () -> draft.playerBurningBoost(),
                        0.1,
                        value ->
                                fmt((value - 1) * 100) + "%",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        old.criticalFreezingTicks(),
                                                        old.playerRadius(),
                                                        old.maxPossibleIsolation(),
                                                        clamp(
                                                                value,
                                                                1,
                                                                6
                                                        ),
                                                        old.playerPowderSnowBoost(),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        old.glowstoneDurationMultiplier(),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );

        settings.add(
                valueRow(
                        "Powder snow boost",
                        () -> draft.playerPowderSnowBoost(),
                        0.1,
                        value ->
                                fmt((value - 1) * 100) + "%",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        old.criticalFreezingTicks(),
                                                        old.playerRadius(),
                                                        old.maxPossibleIsolation(),
                                                        old.playerBurningBoost(),
                                                        clamp(
                                                                value,
                                                                1,
                                                                6
                                                        ),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        old.glowstoneDurationMultiplier(),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );

        settings.add(
                valueRow(
                        "Glowstone duration",
                        () -> draft.glowstoneDurationMultiplier(),
                        1,
                        value ->
                                fmt(value) + "×",
                        value ->
                                update(
                                        old ->
                                                new ConfigPayload(
                                                        old.enabled(),
                                                        old.criticalFreezingTicks(),
                                                        old.playerRadius(),
                                                        old.maxPossibleIsolation(),
                                                        old.playerBurningBoost(),
                                                        old.playerPowderSnowBoost(),
                                                        old.interpolation(),
                                                        old.blocks(),
                                                        old.armorIsolation(),
                                                        old.heatSourcesEnabled(),
                                                        old.useUnlitState(),
                                                        old.heatSourceRelightingEnabled(),
                                                        old.heatSourceRelightDurabilityCost(),
                                                        clamp(
                                                                value,
                                                                1,
                                                                1000
                                                        ),
                                                        old.heatSourceGroups()
                                                )
                                )
                )
        );
    }

    private void buildHeatSourceSettings(Font font) {
        settings.add(
                header(
                        font,
                        "HEAT SOURCE CONFIGURATIONS  •  "
                                + draft.heatSourceGroups().size()
                )
        );

        if (draft.heatSourceGroups().isEmpty()) {
            settings.add(
                    detail(
                            font,
                            "No configured heat source groups"
                    )
            );

            return;
        }

        for (
                int index = 0;
                index < draft.heatSourceGroups().size();
                index++
        ) {
            int groupIndex = index;

            HeatSourceGroupPayload group =
                    draft.heatSourceGroups().get(index);

            settings.add(
                    header(
                            font,
                            group.name()
                    )
            );

            settings.add(
                    detail(
                            font,
                            "BLOCK VARIANTS  •  "
                                    + group.variants().size()
                    )
            );

            for (String variant : group.variants()) {
                settings.add(
                        detail(
                                font,
                                "• " + variant
                        )
                );
            }

            settings.add(
                    valueRow(
                            "Range",
                            () ->
                                    draft
                                            .heatSourceGroups()
                                            .get(groupIndex)
                                            .radius(),
                            0.5,
                            value ->
                                    fmt(value) + " blocks",
                            value ->
                                    updateGroupValue(
                                            groupIndex,
                                            value,
                                            null,
                                            null,
                                            null
                                    )
                    )
            );

            settings.add(
                    valueRow(
                            "Heat contribution",
                            () ->
                                    draft
                                            .heatSourceGroups()
                                            .get(groupIndex)
                                            .heat(),
                            0.1,
                            AdminConfigScreen::fmt,
                            value ->
                                    updateGroupValue(
                                            groupIndex,
                                            null,
                                            value,
                                            null,
                                            null
                                    )
                    )
            );

            settings.add(
                    valueRow(
                            "Burnout time",
                            () ->
                                    draft
                                            .heatSourceGroups()
                                            .get(groupIndex)
                                            .burnoutSeconds(),
                            100,
                            value ->
                                    Math.round(value)
                                            + " seconds",
                            value ->
                                    updateGroupValue(
                                            groupIndex,
                                            null,
                                            null,
                                            (int) value,
                                            null
                                    )
                    )
            );
        }
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                0,
                0,
                width,
                height,
                0xA0101822
        );

        int panelLeft =
                width / 2 - PANEL_WIDTH / 2;

        int panelRight =
                panelLeft + PANEL_WIDTH;

        graphics.fill(
                panelLeft,
                0,
                panelRight,
                height,
                0xF0101822
        );

        graphics.fill(
                panelLeft,
                0,
                panelRight,
                3,
                0xFF55C9E8
        );

        graphics.text(
                font,
                title.copy()
                        .withStyle(
                                net.minecraft.ChatFormatting.BOLD
                        ),
                width / 2 - font.width(title) / 2,
                7,
                0xF1F7FF,
                false
        );

        graphics.text(
                font,
                Component.literal(
                        "CONFIGURATION  /  "
                                + draft.heatSourceGroups().size()
                                + " HEAT SOURCE GROUPS"
                ),
                width / 2 - 160,
                21,
                0x79CFE5,
                false
        );

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        int buttonY =
                height - 27;

        drawAction(
                graphics,
                width / 2 - 154,
                buttonY,
                148,
                20,
                "Cancel",
                mouseX,
                mouseY,
                false
        );

        drawAction(
                graphics,
                width / 2 + 6,
                buttonY,
                148,
                20,
                "Save & apply",
                mouseX,
                mouseY,
                true
        );

        graphics.fill(
                panelLeft,
                height - 33,
                panelRight,
                height - 32,
                0x664B6A7A
        );

        Component hint =
                Component.literal(
                        "Scroll to browse  •  Apply changes on save"
                );

        graphics.text(
                font,
                hint,
                width / 2 - font.width(hint) / 2,
                height - 43,
                0xFFB6C5D2,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0 &&
                event.y() >= height - 27 &&
                event.y() < height - 7) {

            if (event.x() >= width / 2 - 154 &&
                    event.x() <= width / 2 - 6) {

                onClose();
                return true;
            }

            if (event.x() >= width / 2 + 6 &&
                    event.x() <= width / 2 + 154) {

                ClientPlayNetworking.send(draft);
                onClose();

                return true;
            }
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private AdminConfigHeaderEntry header(
            Font font,
            String label
    ) {
        return new AdminConfigHeaderEntry(
                font,
                Component.literal(label)
        );
    }

    private AdminConfigDetailEntry detail(
            Font font,
            String label
    ) {
        return new AdminConfigDetailEntry(
                font,
                Component.literal(label)
        );
    }

    private AdminConfigValueEntry valueRow(
            String label,
            DoubleSupplier current,
            double step,
            DoubleFunction<String> format,
            java.util.function.DoubleConsumer change
    ) {
        return new AdminConfigValueEntry(
                font,
                label,
                () -> format.apply(
                        current.getAsDouble()
                ),
                () ->
                        change.accept(
                                current.getAsDouble() - step
                        ),
                () ->
                        change.accept(
                                current.getAsDouble() + step
                        ),
                false
        );
    }

    private AdminConfigValueEntry toggleRow(
            String label,
            Supplier<Boolean> current,
            Runnable toggle
    ) {
        return new AdminConfigValueEntry(
                font,
                label,
                () ->
                        current.get()
                                ? "ON"
                                : "OFF",
                toggle,
                toggle,
                true
        );
    }

    private void update(
            UnaryOperator<ConfigPayload> change
    ) {
        draft =
                change.apply(draft);
    }

    private void updateGroup(
            int index,
            HeatSourceGroupPayload group
    ) {
        List<HeatSourceGroupPayload> groups =
                new ArrayList<>(
                        draft.heatSourceGroups()
                );

        groups.set(
                index,
                group
        );

        draft =
                new ConfigPayload(
                        draft.enabled(),
                        draft.criticalFreezingTicks(),
                        draft.playerRadius(),
                        draft.maxPossibleIsolation(),
                        draft.playerBurningBoost(),
                        draft.playerPowderSnowBoost(),
                        draft.interpolation(),
                        draft.blocks(),
                        draft.armorIsolation(),
                        draft.heatSourcesEnabled(),
                        draft.useUnlitState(),
                        draft.heatSourceRelightingEnabled(),
                        draft.heatSourceRelightDurabilityCost(),
                        draft.glowstoneDurationMultiplier(),
                        groups
                );
    }

    private void updateGroupValue(
            int index,
            Double radius,
            Double heat,
            Integer burnoutSeconds,
            Boolean interactable
    ) {
        HeatSourceGroupPayload group =
                draft.heatSourceGroups().get(index);

        updateGroup(
                index,
                new HeatSourceGroupPayload(
                        group.name(),

                        heat == null
                                ? group.heat()
                                : clamp(
                                heat,
                                0,
                                20
                        ),

                        radius == null
                                ? group.radius()
                                : clamp(
                                radius,
                                0,
                                128
                        ),

                        burnoutSeconds == null
                                ? group.burnoutSeconds()
                                : clamp(
                                burnoutSeconds,
                                0,
                                10000
                        ),

                        interactable != null || group.interactable(),

                        group.variants()
                )
        );
    }

    private static ConfigPayload copy(
            ConfigPayload old,
            boolean enabled,
            boolean extinguishable
    ) {
        return new ConfigPayload(
                enabled,
                old.criticalFreezingTicks(),
                old.playerRadius(),
                old.maxPossibleIsolation(),
                old.playerBurningBoost(),
                old.playerPowderSnowBoost(),
                old.interpolation(),
                old.blocks(),
                old.armorIsolation(),
                extinguishable,
                old.useUnlitState(),
                old.heatSourceRelightingEnabled(),
                old.heatSourceRelightDurabilityCost(),
                old.glowstoneDurationMultiplier(),
                old.heatSourceGroups()
        );
    }

    private static String fmt(
            double value
    ) {
        return String.format(
                java.util.Locale.ROOT,
                "%.1f",
                value
        );
    }

    private static double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.clamp(
                value,
                min,
                max
        );
    }

    private static int clamp(
            int value,
            int min,
            int max
    ) {
        return Math.clamp(
                value,
                min,
                max
        );
    }

    private void drawAction(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int buttonWidth,
            int buttonHeight,
            String label,
            int mouseX,
            int mouseY,
            boolean primary
    ) {
        boolean hovered =
                mouseX >= x &&
                        mouseX <= x + buttonWidth &&
                        mouseY >= y &&
                        mouseY <= y + buttonHeight;

        int color =
                primary
                        ? hovered
                        ? 0xFF287C91
                        : 0xFF185467
                        : hovered
                        ? 0xFF344A58
                        : 0xFF233642;

        graphics.fill(
                x,
                y,
                x + buttonWidth,
                y + buttonHeight,
                color
        );

        graphics.fill(
                x,
                y,
                x + buttonWidth,
                y + 1,
                primary
                        ? 0xFF72DDF2
                        : 0xFF526C79
        );

        Component text =
                Component.literal(label);

        graphics.text(
                font,
                text,
                x + (
                        buttonWidth -
                                font.width(text)
                ) / 2,
                y + 6,
                0xFFF1F7FA,
                false
        );
    }
}