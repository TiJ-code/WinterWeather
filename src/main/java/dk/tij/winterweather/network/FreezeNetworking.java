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

public final class FreezeNetworking {
    private FreezeNetworking() {}

    public static void register(FreezeStateManager state, FreezingConfigProvider config) {
        PayloadTypeRegistry.serverboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HeatStatePayload.TYPE, HeatStatePayload.CODEC);

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
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 100 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sendState(player, state, config.current());
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            state.reset(newPlayer);
            newPlayer.setTicksFrozen(0);
            sendState(newPlayer, state, config.current());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                state.unregister(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> state.saveAll());
    }

    private static void sendState(ServerPlayer player, FreezeStateManager state, FreezingConfig config) {
        ServerPlayNetworking.send(player,
                new HeatStatePayload(config.enabled(), state.get(player.getUUID())));
    }

    public interface FreezingConfigProvider {
        FreezingConfig current();
    }
}
