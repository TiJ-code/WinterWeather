package dk.tij.winterweather.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record WinterStartPayload(
        String chatMessage,
        List<String> chatExtraLines,
        String bannerTitle,
        String bannerSubtitle,
        List<String> bannerExtraLines
) implements CustomPacketPayload {
    public static final Type<WinterStartPayload> TYPE =
            new Type<>(Identifier.parse("winterweather:winter_start"));
    private static final StreamCodec<ByteBuf, List<String>> STRING_LIST_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(32));
    public static final StreamCodec<RegistryFriendlyByteBuf, WinterStartPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WinterStartPayload::chatMessage,
            STRING_LIST_CODEC, WinterStartPayload::chatExtraLines,
            ByteBufCodecs.STRING_UTF8, WinterStartPayload::bannerTitle,
            ByteBufCodecs.STRING_UTF8, WinterStartPayload::bannerSubtitle,
            STRING_LIST_CODEC, WinterStartPayload::bannerExtraLines,
            WinterStartPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
