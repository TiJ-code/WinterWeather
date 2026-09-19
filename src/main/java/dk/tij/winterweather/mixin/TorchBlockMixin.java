package dk.tij.winterweather.mixin;

import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void winterweather$hideUnlitParticles(
            BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo callbackInfo
    ) {
        if (state.hasProperty(TorchBlocks.LIT)
                && !state.getValue(TorchBlocks.LIT)) {
            callbackInfo.cancel();
        }
    }
}
