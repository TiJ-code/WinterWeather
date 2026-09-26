package dk.tij.winterweather.mixin.client;

import dk.tij.winterweather.client.CampfireSmokeState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin hooks that connect campfire block client behavior to Winter Weather.
 */
@Mixin(CampfireBlock.class)
public abstract class CampfireBlockClientMixin {
    /**
     * Performs the winterweather$hide suppressed smoke operation.
     *
     * @param level           the level value
     * @param pos             the pos value
     * @param signalFire      the signal fire value
     * @param spawnExtraSmoke the spawn extra smoke value
     * @param callbackInfo    the callback info value
     */
    @Inject(method = "makeParticles", at = @At("HEAD"), cancellable = true)
    private static void winterweather$hideSuppressedSmoke(Level level, BlockPos pos, boolean signalFire,
                                                          boolean spawnExtraSmoke, CallbackInfo callbackInfo) {
        if (CampfireSmokeState.isSuppressed(level.dimension().identifier(), pos)) callbackInfo.cancel();
    }
}
