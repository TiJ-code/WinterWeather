package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Network payload for transferring debug state data.
 *
 * @param enabled the enabled value
 */
public record DebugStatePayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<DebugStatePayload> TYPE =
            new Type<>(Identifier.parse("winterweather:debug_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DebugStatePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, DebugStatePayload::enabled, DebugStatePayload::new);

    /**
     * Performs the type operation.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
