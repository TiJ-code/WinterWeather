package dk.tij.winterweather;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HeatStatePayload(boolean nearHeatSource) implements CustomPacketPayload {
    public static final Type<HeatStatePayload> TYPE =
            new Type<>(Identifier.parse("winterweather:heat_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeatStatePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    HeatStatePayload::nearHeatSource,
                    HeatStatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
