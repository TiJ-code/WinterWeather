package dk.tij.winterweather.torch;

import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> TORCHES =
            TagKey.create(Registries.BLOCK, Identifier.parse("winterweather:realistic_torches"));

    private ModTags() {}
}
