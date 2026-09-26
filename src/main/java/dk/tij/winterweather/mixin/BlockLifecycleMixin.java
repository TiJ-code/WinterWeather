package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin hooks that connect block lifecycle behavior to Winter Weather.
 */
@Mixin(Block.class)
public abstract class BlockLifecycleMixin {
    /**
     * Performs the winterweather$track placed heat source operation.
     *
     * @param level        the level value
     * @param pos          the pos value
     * @param state        the state value
     * @param placer       the placer value
     * @param stack        the stack value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "setPlacedBy", at = @At("TAIL"))
    private void winterweather$trackPlacedHeatSource(
            Level level, BlockPos pos, BlockState state, LivingEntity placer,
            ItemStack stack, CallbackInfo callbackInfo
    ) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && WinterWeather.heatSourceManager().isExtinguishable(state)) {
            WinterWeather.heatSourceManager().registerPlacedHeatSource(serverLevel, pos, state);
        }
    }

    /**
     * Performs the winterweather$remove broken heat source operation.
     *
     * @param level        the level value
     * @param pos          the pos value
     * @param state        the state value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "destroy", at = @At("TAIL"))
    private void winterweather$removeBrokenHeatSource(
            LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo callbackInfo
    ) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && WinterWeather.heatSourceManager().isExtinguishable(state)) {
            WinterWeather.heatSourceManager().removeHeatSource(serverLevel, pos);
        }
    }
}
