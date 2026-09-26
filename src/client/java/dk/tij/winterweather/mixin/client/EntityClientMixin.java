package dk.tij.winterweather.mixin.client;

import dk.tij.winterweather.client.HeatStatePayloadState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityClientMixin {
    @Inject(method = "getPercentFrozen", at = @At("HEAD"), cancellable = true)
    private void winterweather$smoothLocalFrostOverlay(CallbackInfoReturnable<Float> callback) {
        if ((Object) this instanceof LocalPlayer
                && HeatStatePayloadState.initialized()
                && HeatStatePayloadState.serverEnabled()) {
            callback.setReturnValue(HeatStatePayloadState.visualProgress());
        }
    }
}
