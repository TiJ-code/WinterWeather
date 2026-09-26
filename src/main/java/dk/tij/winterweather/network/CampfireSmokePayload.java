package dk.tij.winterweather.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record CampfireSmokePayload(Identifier dimension, BlockPos pos, boolean suppressed) implements CustomPacketPayload {
    public static final Type<CampfireSmokePayload> TYPE = new Type<>(Identifier.parse("winterweather:campfire_smoke"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CampfireSmokePayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CampfireSmokePayload::dimension,
            BlockPos.STREAM_CODEC, CampfireSmokePayload::pos,
            ByteBufCodecs.BOOL, CampfireSmokePayload::suppressed,
            CampfireSmokePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
