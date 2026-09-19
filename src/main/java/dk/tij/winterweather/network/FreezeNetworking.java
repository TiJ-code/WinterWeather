package dk.tij.winterweather.network;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.state.FreezeStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
                context.server().execute(() -> state.accept(
                        context.player().getUUID(),
                        payload.actualFreezeTicks(),
                        config.current().criticalFreezingTicks())));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            state.register(player);
            ServerPlayNetworking.send(player, new HeatStatePayload(state.get(player.getUUID())));
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            state.reset(newPlayer);
            newPlayer.setTicksFrozen(0);
            ServerPlayNetworking.send(newPlayer, new HeatStatePayload(0));
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                state.unregister(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> state.saveAll());
    }

    public interface FreezingConfigProvider {
        FreezingConfig current();
    }
}
