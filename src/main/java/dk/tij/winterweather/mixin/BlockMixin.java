package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import dk.tij.winterweather.torch.TorchBlocks;
import dk.tij.winterweather.torch.TorchManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void winterweather$addTorchProperty(
            StateDefinition.Builder<Block, BlockState> builder,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo callbackInfo
    ) {
        if ((Object) this instanceof TorchBlock) {
            builder.add(TorchBlocks.LIT);
        }
    }

    @Inject(method = "defaultBlockState", at = @At("RETURN"), cancellable = true)
    private void winterweather$defaultExtinguishableState(
            CallbackInfoReturnable<BlockState> callbackInfo
    ) {
        BlockState state = callbackInfo.getReturnValue();
        var manager = WinterWeather.torchManagerOrNull();
        if (manager != null && manager.isExtinguishable(state)) {
            callbackInfo.setReturnValue(TorchManager.withLit(state, false));
        }
    }
}
