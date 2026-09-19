package dk.tij.winterweather.mixin;

import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WallTorchBlock.class)
public abstract class WallTorchBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void winterweather$addLitProperty(
            StateDefinition.Builder<Block, BlockState> builder,
            CallbackInfo callbackInfo
    ) {
        builder.add(TorchBlocks.LIT);
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void winterweather$hideUnlitParticles(
            BlockState state, net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random,
            CallbackInfo callbackInfo
    ) {
        if (state.hasProperty(TorchBlocks.LIT)
                && !state.getValue(TorchBlocks.LIT)) {
            callbackInfo.cancel();
        }
    }
}
