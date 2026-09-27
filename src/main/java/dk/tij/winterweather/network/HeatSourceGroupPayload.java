package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/** Named server-config group and the variants sharing its heat settings. */
public record HeatSourceGroupPayload(String name, double heat, double radius, int burnoutSeconds,
                                     List<String> variants) {
    private static final StreamCodec<RegistryFriendlyByteBuf, String> STRING_CODEC =
            StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<String>> STRINGS_CODEC =
            ByteBufCodecs.collection(java.util.ArrayList::new, STRING_CODEC, 256);
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatSourceGroupPayload> CODEC = StreamCodec.composite(
            STRING_CODEC, HeatSourceGroupPayload::name,
            ByteBufCodecs.DOUBLE, HeatSourceGroupPayload::heat,
            ByteBufCodecs.DOUBLE, HeatSourceGroupPayload::radius,
            ByteBufCodecs.VAR_INT, HeatSourceGroupPayload::burnoutSeconds,
            STRINGS_CODEC, HeatSourceGroupPayload::variants,
            HeatSourceGroupPayload::new);
}
