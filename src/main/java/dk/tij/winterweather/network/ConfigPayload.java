package dk.tij.winterweather.network;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.server.config.BlockConfig;
import dk.tij.winterweather.server.config.HeatSourceConfig;
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
import java.util.List;

/**
 * Network payload for transferring config data.
 *
 * @param enabled                         the enabled value
 * @param criticalFreezingTicks           the critical freezing ticks value
 * @param playerRadius                    the player radius value
 * @param maxPossibleIsolation            the max possible isolation value
 * @param playerBurningBoost              the player burning boost value
 * @param playerPowderSnowBoost           the player powder snow boost value
 * @param interpolation                   the interpolation value
 * @param Block                           the block value
 * @param blocks                          the blocks value
 * @param Identifier                      the identifier value
 * @param armorIsolation                  the armor isolation value
 * @param heatSourcesEnabled              the heat sources enabled value
 * @param useUnlitState                   the use unlit state value
 * @param heatSourceRelightingEnabled     the heat source relighting enabled value
 * @param heatSourceRelightDurabilityCost the heat source relight durability cost value
 */
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
        int heatSourceRelightDurabilityCost,
        double glowstoneDurationMultiplier,
        List<HeatSourceGroupPayload> heatSourceGroups
) implements CustomPacketPayload {
    public boolean valid() {
        if (criticalFreezingTicks < 1 || criticalFreezingTicks > 1_000_000
                || !Double.isFinite(playerRadius) || playerRadius < 0 || playerRadius > 128
                || !Double.isFinite(maxPossibleIsolation) || maxPossibleIsolation < 0 || maxPossibleIsolation > 1
                || !Double.isFinite(playerBurningBoost) || playerBurningBoost < 1 || playerBurningBoost > 1001
                || !Double.isFinite(playerPowderSnowBoost) || playerPowderSnowBoost < 1 || playerPowderSnowBoost > 1001
                || heatSourceRelightDurabilityCost < 0 || heatSourceRelightDurabilityCost > 1000
                || !Double.isFinite(glowstoneDurationMultiplier) || glowstoneDurationMultiplier < 1 || glowstoneDurationMultiplier > 1000
                || blocks.size() > 256 || armorIsolation.size() > 256
                || heatSourceGroups.size() > 256
                || java.util.Arrays.stream(InterpolationFunctions.values()).noneMatch(value -> value.getName().equals(interpolation))) return false;
        return blocks.values().stream().allMatch(block -> Double.isFinite(block.heat()) && block.heat() >= 0
                && Double.isFinite(block.radius()) && block.radius() >= 0 && block.radius() <= 128
                && block.burnoutSeconds() >= 0 && block.burnoutSeconds() <= 1_000_000)
                && armorIsolation.values().stream().allMatch(value -> Double.isFinite(value) && value >= 0 && value <= 1)
                && heatSourceGroups.stream().allMatch(group -> group.name() != null && !group.name().isBlank()
                && group.name().length() <= 80 && Double.isFinite(group.heat()) && group.heat() >= 0
                && Double.isFinite(group.radius()) && group.radius() >= 0 && group.radius() <= 128
                && group.burnoutSeconds() >= 0 && group.burnoutSeconds() <= 1_000_000
                && group.variants().size() <= 256 && group.variants().stream().allMatch(id -> id != null && id.length() <= 256));
    }
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
                    BOOL_CODEC,
                    BlockConfig::interactable,
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
                        DOUBLE_CODEC.encode(buffer, payload.glowstoneDurationMultiplier());
                        ByteBufCodecs.collection(java.util.ArrayList::new, HeatSourceGroupPayload.CODEC, 256)
                                .encode(buffer, new java.util.ArrayList<>(payload.heatSourceGroups()));
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
                            INT_CODEC.decode(buffer),
                            DOUBLE_CODEC.decode(buffer),
                            ByteBufCodecs.collection(java.util.ArrayList::new, HeatSourceGroupPayload.CODEC, 256).decode(buffer)
                    )
            );

    /**
     * Performs the from operation.
     *
     * @param config the config value
     */
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
                config.heatSourceRelightDurabilityCost(),
                config.glowstoneDurationMultiplier(),
                config.heatSourceGroups().stream().map(group -> new HeatSourceGroupPayload(group.name(), group.value(),
                        group.radius(), group.burnoutSeconds(), group.interactable(),
                        group.variants().stream().map(v -> v.blockId()).toList())).toList()
        );
    }

    /**
     * Performs the to config operation.
     */
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
                heatSourceRelightDurabilityCost,
                glowstoneDurationMultiplier,
                heatSourceGroups.stream().map(group -> new HeatSourceConfig(group.name(), group.heat(), group.radius(),
                        group.burnoutSeconds(), group.interactable(), group.variants().stream().map(dk.tij.winterweather.server.config.HeatSourceVariant::new).toList())).toList()
        );
    }

    /**
     * Performs the type operation.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
