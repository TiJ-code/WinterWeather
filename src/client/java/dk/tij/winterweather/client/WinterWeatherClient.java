package dk.tij.winterweather.client;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.network.HeatStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class WinterWeatherClient implements ClientModInitializer {
    private static final int REPORT_INTERVAL_TICKS = 100;

    @Override
    public void onInitializeClient() {
        FreezingConfig config = FreezingConfig.load();
        ClientPlayNetworking.registerGlobalReceiver(HeatStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> HeatStatePayloadState.updateFromServer(
                        payload.enabled(), Math.max(0, payload.actualFreezeTicks()))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                HeatStatePayloadState.reset();
                return;
            }

            if (!config.enabled()
                    || !HeatStatePayloadState.initialized()
                    || !HeatStatePayloadState.serverEnabled()) {
                return;
            }

            double temperatureDelta = config.temperatureDelta(
                    client.level,
                    client.player.blockPosition(),
                    client.player.position(),
                    client.player.getEyePosition(),
                    new ArrayList<>(java.util.List.of(
                            BuiltInRegistries.ITEM.getKey(client.player.getItemBySlot(EquipmentSlot.HEAD).getItem()),
                            BuiltInRegistries.ITEM.getKey(client.player.getItemBySlot(EquipmentSlot.CHEST).getItem()),
                            BuiltInRegistries.ITEM.getKey(client.player.getItemBySlot(EquipmentSlot.LEGS).getItem()),
                            BuiltInRegistries.ITEM.getKey(client.player.getItemBySlot(EquipmentSlot.FEET).getItem())
                    )),
                    client.player.isOnFire(),
                    powderSnowBlocks(client)
            );
            double progress = Math.max(0,
                    HeatStatePayloadState.actualFreezeTicks() + temperatureDelta);
            HeatStatePayloadState.update(progress);

            if (client.level.getGameTime() % REPORT_INTERVAL_TICKS == 0
                    && ClientPlayNetworking.canSend(HeatStatePayload.TYPE)) {
                ClientPlayNetworking.send(new HeatStatePayload(true, progress));
            }
        });
    }

    private static int powderSnowBlocks(net.minecraft.client.Minecraft client) {
        int blocks = 0;
        if (client.level.getBlockState(client.player.blockPosition()).is(Blocks.POWDER_SNOW)) blocks++;
        if (client.level.getBlockState(client.player.blockPosition().above()).is(Blocks.POWDER_SNOW)) blocks++;
        return blocks;
    }
}
