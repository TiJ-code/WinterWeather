package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Network payload for transferring winter start data.
 *
 * @param announcementJson the announcement json value
 */
public record WinterStartPayload(String announcementJson) implements CustomPacketPayload {
    public static final Type<WinterStartPayload> TYPE =
            new Type<>(Identifier.parse("winterweather:winter_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WinterStartPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                    WinterStartPayload::announcementJson,
                    WinterStartPayload::new);

    /**
     * Performs the type operation.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
