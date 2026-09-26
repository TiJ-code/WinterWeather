package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


/**
 * Network payload for transferring heat state data.
 *
 * @param enabled           the enabled value
 * @param actualFreezeTicks the actual freeze ticks value
 */
public record HeatStatePayload(boolean enabled, double actualFreezeTicks) implements CustomPacketPayload {
    /**
     * Performs the parse operation.
     */
    public static final Type<HeatStatePayload> TYPE = new Type<>(Identifier.parse("winterweather:freeze_progress"));
    /**
     * Performs the composite operation.
     *
     * @param BOOL the bool value
     * @param enabled the enabled value
     * @param DOUBLE the double value
     * @param actualFreezeTicks the actual freeze ticks value
     * @param new the new value
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HeatStatePayload::enabled,
            ByteBufCodecs.DOUBLE, HeatStatePayload::actualFreezeTicks,
            HeatStatePayload::new);

    /**
     * Performs the type operation.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
