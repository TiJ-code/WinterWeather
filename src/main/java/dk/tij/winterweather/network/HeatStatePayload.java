package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HeatStatePayload(double actualFreezeTicks) implements CustomPacketPayload {
    public static final Type<HeatStatePayload> TYPE = new Type<>(Identifier.parse("winterweather:freeze_progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, HeatStatePayload::actualFreezeTicks, HeatStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
