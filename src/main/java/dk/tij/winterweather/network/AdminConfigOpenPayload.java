package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AdminConfigOpenPayload(boolean open) implements CustomPacketPayload {
    public static final Type<AdminConfigOpenPayload> TYPE = new Type<>(Identifier.parse("winterweather:admin_config_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdminConfigOpenPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, AdminConfigOpenPayload::open, AdminConfigOpenPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
