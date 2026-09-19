package dk.tij.winterweather.mixin;

import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void winterweather$unlitTorchHasNoLight(CallbackInfoReturnable<Integer> callbackInfo) {
        BlockState state = (BlockState) (Object) this;
        if (state.hasProperty(TorchBlocks.LIT)
                && !state.getValue(TorchBlocks.LIT)) {
            callbackInfo.setReturnValue(0);
        }
    }
}
