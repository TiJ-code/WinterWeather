package dk.tij.winterweather.mixin;

import dk.tij.winterweather.WinterWeather;
import dk.tij.winterweather.torch.TorchBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockLifecycleMixin {
    @Inject(method = "setPlacedBy", at = @At("TAIL"))
    private void winterweather$trackPlacedTorch(
            Level level, BlockPos pos, BlockState state, LivingEntity placer,
            ItemStack stack, CallbackInfo callbackInfo
    ) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && TorchBlocks.isTorch(state)) {
            WinterWeather.torchManager().registerPlacedTorch(serverLevel, pos, state);
        }
    }

    @Inject(method = "destroy", at = @At("TAIL"))
    private void winterweather$removeBrokenTorch(
            LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo callbackInfo
    ) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && TorchBlocks.isTorch(state)) {
            WinterWeather.torchManager().removeTorch(serverLevel, pos);
        }
    }
}
