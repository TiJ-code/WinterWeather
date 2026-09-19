package dk.tij.winterweather.torch;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class TorchBlocks {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public static boolean isTorch(BlockState state) {
        return state.is(ModTags.TORCHES);
    }

    private TorchBlocks() {}
}
