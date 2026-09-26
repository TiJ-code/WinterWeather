package dk.tij.winterweather.mixin;

import dk.tij.winterweather.heat.HeatSourceBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin hooks that connect torch block behavior to Winter Weather.
 */
@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin {
    /**
     * Performs the winterweather$hide unlit particles operation.
     *
     * @param state        the state value
     * @param level        the level value
     * @param pos          the pos value
     * @param random       the random value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void winterweather$hideUnlitParticles(
            BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo
    ) {
        if (state.hasProperty(HeatSourceBlocks.LIT)
                && !state.getValue(HeatSourceBlocks.LIT)) {
            callbackInfo.cancel();
        }
    }
}
