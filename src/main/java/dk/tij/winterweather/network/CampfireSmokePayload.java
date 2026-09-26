package dk.tij.winterweather.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Network payload for transferring campfire smoke data.
 *
 * @param dimension  the dimension value
 * @param pos        the pos value
 * @param suppressed the suppressed value
 */
public record CampfireSmokePayload(Identifier dimension, BlockPos pos,
                                   boolean suppressed) implements CustomPacketPayload {
    /**
     * Performs the parse operation.
     */
    public static final Type<CampfireSmokePayload> TYPE = new Type<>(Identifier.parse("winterweather:campfire_smoke"));
    /**
     * Performs the composite operation.
     *
     * @param STREAM_CODEC the stream codec value
     * @param dimension the dimension value
     * @param STREAM_CODEC the stream codec value
     * @param pos the pos value
     * @param BOOL the bool value
     * @param suppressed the suppressed value
     * @param new the new value
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, CampfireSmokePayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CampfireSmokePayload::dimension,
            BlockPos.STREAM_CODEC, CampfireSmokePayload::pos,
            ByteBufCodecs.BOOL, CampfireSmokePayload::suppressed,
            CampfireSmokePayload::new);

    /**
     * Performs the type operation.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
