package dk.tij.winterweather.mixin;

import dk.tij.winterweather.heat.HeatSourceBlocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void winterweather$unlitHeatSourceHasNoLight(CallbackInfoReturnable<Integer> callbackInfo) {
        BlockState state = (BlockState) (Object) this;
        if (state.hasProperty(HeatSourceBlocks.LIT)
                && !state.getValue(HeatSourceBlocks.LIT)) {
            callbackInfo.setReturnValue(0);
        }
    }
}
