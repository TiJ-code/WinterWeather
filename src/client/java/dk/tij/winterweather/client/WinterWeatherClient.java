package dk.tij.winterweather.client;

import dk.tij.winterweather.FreezingConfig;
import dk.tij.winterweather.HeatStatePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.ClientModInitializer;

public class WinterWeatherClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FreezingConfig config = FreezingConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                HeatStatePayloadState.reset();
                return;
            }

            boolean nearHeatSource = config.isNearHeatSource(
                    client.level,
                    client.player.blockPosition(),
                    client.player.position()
            );
            if (!HeatStatePayloadState.matches(nearHeatSource)) {
                HeatStatePayloadState.update(nearHeatSource);
                if (ClientPlayNetworking.canSend(HeatStatePayload.TYPE)) {
                    ClientPlayNetworking.send(new HeatStatePayload(nearHeatSource));
                }
            }
        });
    }
}
