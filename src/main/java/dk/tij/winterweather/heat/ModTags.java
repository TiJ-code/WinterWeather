package dk.tij.winterweather.heat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


/**
 * Provides mod tags functionality for Winter Weather.
 */
public final class ModTags {
    public static final TagKey<Block> EXTINGUISHABLE_FIRE =
            TagKey.create(Registries.BLOCK, Identifier.parse("winterweather:extinguishable_fire"));

    /**
     * Performs the mod tags operation.
     */
    private ModTags() {
    }
}
