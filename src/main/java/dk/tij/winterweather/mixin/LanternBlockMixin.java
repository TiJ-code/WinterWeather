package dk.tij.winterweather.mixin;

import dk.tij.winterweather.heat.HeatSourceBlocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin hooks that connect lantern block behavior to Winter Weather.
 */
@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin {
    /**
     * Performs the winterweather$add lit property operation.
     *
     * @param Block        the block value
     * @param builder      the builder value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void winterweather$addLitProperty(StateDefinition.Builder<Block, BlockState> builder,
                                              CallbackInfo callbackInfo) {
        builder.add(HeatSourceBlocks.LIT);
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/LanternBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
/**
 * Performs the winterweather$register unlit default operation.
 * @param defaultState the default state value
 */
    private BlockState winterweather$registerUnlitDefault(BlockState defaultState) {
        return defaultState.setValue(HeatSourceBlocks.LIT, false);
    }

    /**
     * Performs the winterweather$place lantern unlit operation.
     *
     * @param context      the context value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void winterweather$placeLanternUnlit(BlockPlaceContext context,
                                                 CallbackInfoReturnable<BlockState> callbackInfo) {
        BlockState state = callbackInfo.getReturnValue();
        if (state != null) callbackInfo.setReturnValue(state.setValue(HeatSourceBlocks.LIT, false));
    }
}
