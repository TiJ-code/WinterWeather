package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin hooks that connect level behavior to Winter Weather.
 */
@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("RETURN")
    )
/**
 * Performs the winterweather$track generated block operation.
 * @param pos the pos value
 * @param state the state value
 * @param flags the flags value
 * @param callbackInfo the callback info value
 */
    private void winterweather$trackGeneratedBlock(
            BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        if (callbackInfo.getReturnValueZ() && (Object) this instanceof ServerLevel level) {
            var manager = WinterWeather.heatSourceManagerOrNull();
            if (manager != null && manager.isExtinguishable(state)) {
                manager.registerPlacedHeatSource(level, pos, level.getBlockState(pos));
            }
        }
    }
}
