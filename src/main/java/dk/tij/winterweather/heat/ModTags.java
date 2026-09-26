package dk.tij.winterweather.heat;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> EXTINGUISHABLE_FIRE =
            TagKey.create(Registries.BLOCK, Identifier.parse("winterweather:extinguishable_fire"));

    private ModTags() {}
}
