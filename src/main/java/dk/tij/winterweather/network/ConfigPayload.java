package dk.tij.winterweather.network;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.server.config.BlockConfig;
import dk.tij.winterweather.utils.InterpolationFunctions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public record ConfigPayload(
        boolean enabled,
        int criticalFreezingTicks,
        double playerRadius,
        double maxPossibleIsolation,
        double playerBurningBoost,
        double playerPowderSnowBoost,
        String interpolation,
        Map<Block, BlockConfig> blocks,
        Map<Identifier, Double> armorIsolation,
        boolean heatSourcesEnabled,
        boolean useUnlitState,
        boolean heatSourceRelightingEnabled,
        int heatSourceRelightDurabilityCost
) implements CustomPacketPayload {

    public static final Type<ConfigPayload> TYPE =
            new Type<>(Identifier.parse("winterweather:config"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Boolean> BOOL_CODEC =
            StreamCodec.of(
                    RegistryFriendlyByteBuf::writeBoolean,
                    RegistryFriendlyByteBuf::readBoolean
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT_CODEC =
            StreamCodec.of(
                    RegistryFriendlyByteBuf::writeInt,
                    RegistryFriendlyByteBuf::readInt
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Double> DOUBLE_CODEC =
            StreamCodec.of(
                    RegistryFriendlyByteBuf::writeDouble,
                    RegistryFriendlyByteBuf::readDouble
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, String> STRING_CODEC =
            StreamCodec.of(
                    RegistryFriendlyByteBuf::writeUtf,
                    RegistryFriendlyByteBuf::readUtf
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK_CODEC =
            ByteBufCodecs.registry(Registries.BLOCK);

    private static final StreamCodec<ByteBuf, Identifier> IDENTIFIER_CODEC =
            Identifier.STREAM_CODEC;

    private static final StreamCodec<RegistryFriendlyByteBuf, BlockConfig> BLOCK_CONFIG_CODEC =
            StreamCodec.composite(
                    DOUBLE_CODEC,
                    BlockConfig::heat,
                    DOUBLE_CODEC,
                    BlockConfig::radius,
                    BOOL_CODEC,
                    BlockConfig::extinguishable,
                    INT_CODEC,
                    BlockConfig::burnoutSeconds,
                    BlockConfig::new
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Block, BlockConfig>> BLOCKS_CODEC =
            ByteBufCodecs.map(
                    HashMap::new,
                    BLOCK_CODEC,
                    BLOCK_CONFIG_CODEC,
                    256
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Identifier, Double>> ARMOR_ISOLATION_CODEC =
            ByteBufCodecs.map(
                    HashMap::new,
                    IDENTIFIER_CODEC,
                    DOUBLE_CODEC,
                    256
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        BOOL_CODEC.encode(buffer, payload.enabled());
                        INT_CODEC.encode(buffer, payload.criticalFreezingTicks());
                        DOUBLE_CODEC.encode(buffer, payload.playerRadius());
                        DOUBLE_CODEC.encode(buffer, payload.maxPossibleIsolation());
                        DOUBLE_CODEC.encode(buffer, payload.playerBurningBoost());
                        DOUBLE_CODEC.encode(buffer, payload.playerPowderSnowBoost());
                        STRING_CODEC.encode(buffer, payload.interpolation());

                        BLOCKS_CODEC.encode(buffer, payload.blocks());
                        ARMOR_ISOLATION_CODEC.encode(buffer, payload.armorIsolation());

                        BOOL_CODEC.encode(buffer, payload.heatSourcesEnabled());
                        BOOL_CODEC.encode(buffer, payload.useUnlitState());
                        BOOL_CODEC.encode(buffer, payload.heatSourceRelightingEnabled());
                        INT_CODEC.encode(buffer, payload.heatSourceRelightDurabilityCost());
                    },
                    buffer -> new ConfigPayload(
                            BOOL_CODEC.decode(buffer),
                            INT_CODEC.decode(buffer),
                            DOUBLE_CODEC.decode(buffer),
                            DOUBLE_CODEC.decode(buffer),
                            DOUBLE_CODEC.decode(buffer),
                            DOUBLE_CODEC.decode(buffer),
                            STRING_CODEC.decode(buffer),

                            BLOCKS_CODEC.decode(buffer),
                            ARMOR_ISOLATION_CODEC.decode(buffer),

                            BOOL_CODEC.decode(buffer),
                            BOOL_CODEC.decode(buffer),
                            BOOL_CODEC.decode(buffer),
                            INT_CODEC.decode(buffer)
                    )
            );

    public static ConfigPayload from(FreezingConfig config) {
        return new ConfigPayload(
                config.enabled(),
                config.criticalFreezingTicks(),
                config.playerRadius(),
                config.maxPossibleIsolation(),
                config.playerBurningBoost(),
                config.playerPowderSnowBoost(),
                config.interpolation().getName(),
                config.blocks(),
                config.armorIsolation(),
                config.heatSourcesEnabled(),
                config.useUnlitState(),
                config.heatSourceRelightingEnabled(),
                config.heatSourceRelightDurabilityCost()
        );
    }

    public FreezingConfig toConfig() {
        return new FreezingConfig(
                enabled,
                criticalFreezingTicks,
                playerRadius,
                maxPossibleIsolation,
                playerBurningBoost,
                playerPowderSnowBoost,
                InterpolationFunctions.by(interpolation),
                blocks,
                armorIsolation,
                heatSourcesEnabled,
                useUnlitState,
                heatSourceRelightingEnabled,
                heatSourceRelightDurabilityCost
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
