package dk.tij.winterweather.mixin.client;

import dk.tij.winterweather.client.HeatStatePayloadState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityClientMixin {
    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void winterweather$allowCustomFreeze(CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof LocalPlayer
                && HeatStatePayloadState.initialized()
                && HeatStatePayloadState.serverEnabled()) {
            callback.setReturnValue(true);
        }
    }
}
