package dk.tij.winterweather.network;

import dk.tij.winterweather.announcement.WinterWeatherBranding;
import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.config.WinterStartAnnouncementConfig;
import dk.tij.winterweather.state.FreezeStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides freeze networking functionality for Winter Weather.
 */
public final class FreezeNetworking {
    /**
     * Performs the freeze networking operation.
     */
    private FreezeNetworking() {
    }

    /**
     * Performs the register operation.
     *
     * @param state  the state value
     * @param config the config value
     */
    public static void register(FreezeStateManager state, FreezingConfigProvider config) {
        Map<UUID, ResourceKey<Level>> syncedDimensions = new HashMap<>();
        PayloadTypeRegistry.serverboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CampfireSmokePayload.TYPE, CampfireSmokePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DebugStatePayload.TYPE, DebugStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WinterStartPayload.TYPE, WinterStartPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HeatStatePayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    if (config.current().enabled()) {
                        state.accept(context.player().getUUID(), payload.actualFreezeTicks());
                    }
                }));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            state.register(player);
            sendState(player, state, config.current());
            sendCampfireSmoke(player);
            syncedDimensions.put(player.getUUID(), player.level().dimension());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ResourceKey<Level> dimension = player.level().dimension();
                if (!dimension.equals(syncedDimensions.put(player.getUUID(), dimension))) {
                    sendCampfireSmoke(player);
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            state.reset(newPlayer);
            newPlayer.setTicksFrozen(0);
            sendState(newPlayer, state, config.current());
            sendCampfireSmoke(newPlayer);
            syncedDimensions.put(newPlayer.getUUID(), newPlayer.level().dimension());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
        {
            state.unregister(handler.getPlayer());
            syncedDimensions.remove(handler.getPlayer().getUUID());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> state.saveAll());
    }

    /**
     * Performs the send campfire smoke operation.
     *
     * @param player     the player value
     * @param pos        the pos value
     * @param suppressed the suppressed value
     */
    public static void sendCampfireSmoke(ServerPlayer player, net.minecraft.core.BlockPos pos, boolean suppressed) {
        ServerPlayNetworking.send(player, new CampfireSmokePayload(
                player.level().dimension().identifier(), pos, suppressed));
    }

    /**
     * Performs the send campfire smoke operation.
     *
     * @param player the player value
     */
    private static void sendCampfireSmoke(ServerPlayer player) {
        var data = dk.tij.winterweather.data.HeatSourceData.get(player.level());
        for (var entry : data.entries()) {
            if (entry.getValue().suppressSmoke()) {
                ServerPlayNetworking.send(player,
                        new CampfireSmokePayload(player.level().dimension().identifier(),
                                net.minecraft.core.BlockPos.of(entry.getKey()), true));
            }
        }
    }

    /**
     * Performs the send state operation.
     *
     * @param player the player value
     * @param state  the state value
     * @param config the config value
     */
    private static void sendState(ServerPlayer player, FreezeStateManager state, FreezingConfig config) {
        // Send config first so the client has current parameters before ticking freeze progress.
        ServerPlayNetworking.send(player, ConfigPayload.from(config));
        ServerPlayNetworking.send(player,
                new HeatStatePayload(config.enabled(), state.get(player.getUUID())));
        sendDebugState(player, state.debug(player.getUUID()));
    }

    /** Sends the current config and freeze state to every connected player. */
    public static void syncAll(net.minecraft.server.MinecraftServer server,
                               FreezeStateManager state,
                               FreezingConfig config) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendState(player, state, config);
        }
    }

    /**
     * Performs the send debug state operation.
     *
     * @param player  the player value
     * @param enabled the enabled value
     */
    public static void sendDebugState(ServerPlayer player, boolean enabled) {
        ServerPlayNetworking.send(player, new DebugStatePayload(enabled));
    }

    /**
     * Performs the broadcast winter start operation.
     *
     * @param server the server value
     */
    public static void broadcastWinterStart(net.minecraft.server.MinecraftServer server) {
        WinterStartAnnouncementConfig announcement = WinterStartAnnouncementConfig.load();
        var message = net.minecraft.network.chat.Component.literal(announcement.chat().message())
                .withStyle(style -> style.withColor(net.minecraft.ChatFormatting.AQUA).withBold(true));
        server.getPlayerList().broadcastSystemMessage(message, false);
        for (String line : announcement.chat().extraLines()) {
            server.getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal(line)
                            .withStyle(net.minecraft.ChatFormatting.GRAY), false);
        }
        server.getPlayerList().broadcastSystemMessage(WinterWeatherBranding.chatAttribution(), false);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, new WinterStartPayload(announcement.toJson()));
        }
    }

    /**
     * Provides freezing config provider functionality for Winter Weather.
     */
    public interface FreezingConfigProvider {
        FreezingConfig current();
    }
}
