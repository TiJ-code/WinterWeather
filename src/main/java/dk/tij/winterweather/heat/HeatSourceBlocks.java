package dk.tij.winterweather.heat;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class HeatSourceBlocks {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public static boolean isExtinguishable(BlockState state) {
        return state.is(ModTags.EXTINGUISHABLE_FIRE);
    }

    private HeatSourceBlocks() {}
}
