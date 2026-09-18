package dk.tij.winterweather;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.api.ModInitializer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WinterWeather implements ModInitializer {
    private static final Map<UUID, Boolean> PLAYER_HEAT_STATE = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        FreezingConfig config = FreezingConfig.load();
        PayloadTypeRegistry.serverboundPlay().register(
                HeatStatePayload.TYPE,
                HeatStatePayload.CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(HeatStatePayload.TYPE, (payload, context) ->
                PLAYER_HEAT_STATE.put(context.player().getUUID(), payload.nearHeatSource()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PLAYER_HEAT_STATE.remove(handler.getPlayer().getUUID()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                if (!player.canFreeze()) {
                    player.clearFreeze();
                    continue;
                }

                if (PLAYER_HEAT_STATE.getOrDefault(player.getUUID(), false)) {
                    player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - config.thawRate()));
                } else {
                    player.setTicksFrozen(Math.min(FreezingConfig.MAX_FROZEN_TICKS,
                            player.getTicksFrozen() + config.freezeRate()));
                }
            }
        });
    }
}
