package dk.tij.winterweather.heat;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;


/**
 * Provides heat source blocks functionality for Winter Weather.
 */
public final class HeatSourceBlocks {
    /**
     * Performs the create operation.
     */
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    /**
     * Performs the heat source blocks operation.
     */
    private HeatSourceBlocks() {
    }

    /**
     * Reports whether extinguishable is true.
     *
     * @param state the state value
     */
    public static boolean isExtinguishable(BlockState state) {
        return state.is(ModTags.EXTINGUISHABLE_FIRE);
    }
}
