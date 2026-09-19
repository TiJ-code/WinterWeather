package dk.tij.winterweather.mixin;

import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
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
    private void winterweather$defaultTorchState(CallbackInfoReturnable<BlockState> callbackInfo) {
        BlockState state = callbackInfo.getReturnValue();
        if (state.getBlock() instanceof TorchBlock && state.hasProperty(TorchBlocks.LIT)) {
            callbackInfo.setReturnValue(state.setValue(TorchBlocks.LIT, false));
        } else if (state.getBlock() instanceof CampfireBlock
                && state.hasProperty(CampfireBlock.LIT)) {
            callbackInfo.setReturnValue(state.setValue(CampfireBlock.LIT, false));
        }
    }
}
