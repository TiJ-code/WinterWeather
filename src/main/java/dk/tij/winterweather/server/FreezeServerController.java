package dk.tij.winterweather.server;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.state.FreezeStateManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.function.BooleanSupplier;

public final class FreezeServerController {
    private final FreezeStateManager state;
    private final BooleanSupplier enabled;
    private int damageTick;

    public FreezeServerController(FreezeStateManager state, BooleanSupplier enabled) {
        this.state = state;
        this.enabled = enabled;
    }

    public void tick(MinecraftServer server, FreezingConfig config) {
        if (!enabled.getAsBoolean()) return;
        damageTick++;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyFreezeState(player, config);
            if (damageTick % 40 == 0
                    && state.get(player.getUUID()) > config.criticalFreezingTicks()
                    && !wearsInsulatedArmor(player, config)) {
                player.hurt(player.damageSources().freeze(), 1);
            }
        }

        if (damageTick % 1200 == 0) {
            state.saveAll();
        }
    }

    private void applyFreezeState(ServerPlayer player, FreezingConfig config) {
        int frozenTicks = config.toFrozenTicks(state.get(player.getUUID()));
        if (player.getTicksFrozen() > frozenTicks) {
            frozenTicks = player.getTicksFrozen();
        }
        player.setTicksFrozen(Math.min(FreezingConfig.MAX_FROZEN_TICKS, frozenTicks));
    }

    private static boolean wearsInsulatedArmor(ServerPlayer player, FreezingConfig config) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()
                    && config.isInsulatedArmor(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                return true;
            }
        }
        return false;
    }
}
