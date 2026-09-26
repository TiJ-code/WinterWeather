package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin hooks that connect living entity behavior to Winter Weather.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /**
     * Performs the winterweather$allow custom freeze operation.
     *
     * @param callback the callback value
     */
    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void winterweather$allowCustomFreeze(CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof ServerPlayer && WinterWeather.modEnabled()) {
            callback.setReturnValue(true);
        }
    }
}
