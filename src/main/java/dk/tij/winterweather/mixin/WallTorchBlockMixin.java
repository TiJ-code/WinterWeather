package dk.tij.winterweather.mixin;

import dk.tij.winterweather.heat.HeatSourceBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin hooks that connect wall torch block behavior to Winter Weather.
 */
@Mixin(WallTorchBlock.class)
public abstract class WallTorchBlockMixin {
    /**
     * Performs the winterweather$add lit property operation.
     *
     * @param Block        the block value
     * @param builder      the builder value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void winterweather$addLitProperty(
            StateDefinition.Builder<Block, BlockState> builder,
            CallbackInfo callbackInfo
    ) {
        builder.add(HeatSourceBlocks.LIT);
    }

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
            BlockState state, net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random,
            CallbackInfo callbackInfo
    ) {
        if (state.hasProperty(HeatSourceBlocks.LIT)
                && !state.getValue(HeatSourceBlocks.LIT)) {
            callbackInfo.cancel();
        }
    }
}
