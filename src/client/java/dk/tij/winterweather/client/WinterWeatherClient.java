package dk.tij.winterweather.client;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.config.WinterStartAnnouncementConfig;
import dk.tij.winterweather.network.CampfireSmokePayload;
import dk.tij.winterweather.network.ConfigPayload;
import dk.tij.winterweather.network.DebugStatePayload;
import dk.tij.winterweather.network.HeatStatePayload;
import dk.tij.winterweather.network.WinterStartPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

/**
 * Provides winter weather client functionality for Winter Weather.
 */
public class WinterWeatherClient implements ClientModInitializer {
    /**
     * Stores the config value.
     */
    private FreezingConfig config;

    /**
     * Performs the powder snow blocks operation.
     *
     * @param client the client value
     */
    private static int powderSnowBlocks(net.minecraft.client.Minecraft client) {
        int blocks = 0;
        if (client.level.getBlockState(client.player.blockPosition()).is(Blocks.POWDER_SNOW)) blocks++;
        if (client.level.getBlockState(client.player.blockPosition().above()).is(Blocks.POWDER_SNOW)) blocks++;
        return blocks;
    }

    /**
     * Performs the on initialize client operation.
     */
    @Override
    public void onInitializeClient() {
        WinterStartAnimation.register();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> CampfireSmokeState.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CampfireSmokeState.clear());
        ClientPlayNetworking.registerGlobalReceiver(HeatStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> HeatStatePayloadState.updateFromServer(
                        payload.enabled(), Math.max(0, payload.actualFreezeTicks()))));

        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
                context.client().execute(() -> config = payload.toConfig()));
        ClientPlayNetworking.registerGlobalReceiver(DebugStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> HeatStatePayloadState.updateDebug(payload.enabled())));
        ClientPlayNetworking.registerGlobalReceiver(WinterStartPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    WinterStartAnimation.trigger(
                            WinterStartAnnouncementConfig.decode(payload.announcementJson()));
                }));
        ClientPlayNetworking.registerGlobalReceiver(CampfireSmokePayload.TYPE, (payload, context) ->
                context.client().execute(() -> CampfireSmokeState.set(
                        payload.dimension(), payload.pos(), payload.suppressed())));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                HeatStatePayloadState.reset();
                return;
            }
            if (config == null || !HeatStatePayloadState.initialized()
                    || !HeatStatePayloadState.serverEnabled()) return;

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
            HeatStatePayloadState.updateVisualProgress(config.toFrozenProgress(progress));

            if (HeatStatePayloadState.debugEnabled()) {
                client.player.sendOverlayMessage(Component.literal(String.format(
                        "Freeze: %d ticks (%.2f internal)",
                        config.toFrozenTicks(progress), progress)));
            }

            if (ClientPlayNetworking.canSend(HeatStatePayload.TYPE)) {
                ClientPlayNetworking.send(new HeatStatePayload(true, progress));
            }
        });
    }
}
