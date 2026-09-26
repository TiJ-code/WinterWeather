package dk.tij.winterweather.network;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.state.FreezeStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FreezeNetworking {
    private FreezeNetworking() {}

    public static void register(FreezeStateManager state, FreezingConfigProvider config) {
        Map<UUID, ResourceKey<Level>> syncedDimensions = new HashMap<>();
        PayloadTypeRegistry.serverboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CampfireSmokePayload.TYPE, CampfireSmokePayload.CODEC);

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
                if (server.getTickCount() % 100 == 0) sendState(player, state, config.current());
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

    public static void sendCampfireSmoke(ServerPlayer player, net.minecraft.core.BlockPos pos, boolean suppressed) {
        ServerPlayNetworking.send(player, new CampfireSmokePayload(
                player.level().dimension().identifier(), pos, suppressed));
    }

    private static void sendCampfireSmoke(ServerPlayer player) {
        var data = dk.tij.winterweather.data.TorchData.get((ServerLevel) player.level());
        for (var entry : data.entries()) {
            if (entry.getValue().suppressSmoke()) {
                ServerPlayNetworking.send(player,
                        new CampfireSmokePayload(player.level().dimension().identifier(),
                                net.minecraft.core.BlockPos.of(entry.getKey()), true));
            }
        }
    }

    private static void sendState(ServerPlayer player, FreezeStateManager state, FreezingConfig config) {
        ServerPlayNetworking.send(player,
                new HeatStatePayload(config.enabled(), state.get(player.getUUID())));
        ServerPlayNetworking.send(player, ConfigPayload.from(config));
    }

    public interface FreezingConfigProvider {
        FreezingConfig current();
    }
}
