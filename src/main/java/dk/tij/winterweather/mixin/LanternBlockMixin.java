package dk.tij.winterweather.mixin;

import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void winterweather$addLitProperty(StateDefinition.Builder<Block, BlockState> builder,
                                               CallbackInfo callbackInfo) {
        builder.add(TorchBlocks.LIT);
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/LanternBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private BlockState winterweather$registerUnlitDefault(BlockState defaultState) {
        return defaultState.setValue(TorchBlocks.LIT, false);
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void winterweather$placeLanternUnlit(BlockPlaceContext context,
                                                  CallbackInfoReturnable<BlockState> callbackInfo) {
        BlockState state = callbackInfo.getReturnValue();
        if (state != null) callbackInfo.setReturnValue(state.setValue(TorchBlocks.LIT, false));
    }
}
