package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import dk.tij.winterweather.heat.HeatSourceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin hooks that connect campfire block behavior to Winter Weather.
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    /**
     * Performs the winterweather$prevent locked campfire dowse operation.
     *
     * @param entity       the entity value
     * @param level        the level value
     * @param pos          the pos value
     * @param state        the state value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "dowse", at = @At("HEAD"), cancellable = true)
    private static void winterweather$preventLockedCampfireDowse(
            Entity entity,
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            CallbackInfo callbackInfo
    ) {
        if (isLocked(level, pos)) {
            callbackInfo.cancel();
        }
    }

    /**
     * Reports whether locked is true.
     *
     * @param level the level value
     * @param pos   the pos value
     */
    private static boolean isLocked(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        HeatSourceManager manager = WinterWeather.heatSourceManagerOrNull();
        return manager != null && manager.isLocked(serverLevel, pos);
    }

    /**
     * Performs the winterweather$prevent locked campfire water extinguish operation.
     *
     * @param level        the level value
     * @param pos          the pos value
     * @param state        the state value
     * @param fluidState   the fluid state value
     * @param callbackInfo the callback info value
     */
    @Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
    private void winterweather$preventLockedCampfireWaterExtinguish(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            FluidState fluidState,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        if (isLocked(level, pos)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
