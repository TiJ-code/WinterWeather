package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import dk.tij.winterweather.torch.TorchManager;
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

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
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

    private static boolean isLocked(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        TorchManager manager = WinterWeather.torchManagerOrNull();
        return manager != null && manager.isLocked(serverLevel, pos);
    }
}
