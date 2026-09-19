package dk.tij.winterweather.torch;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public final class TorchInteraction {
    private TorchInteraction() {
    }

    public static void register(TorchManager manager) {
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                interact(manager, player, level, hand, hit));
    }

    private static InteractionResult interact(TorchManager manager, Player player, Level level,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = serverLevel.getBlockState(pos);
        if (!TorchBlocks.isTorch(state)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!TorchManager.isLit(state) && held.is(Items.FLINT_AND_STEEL)
                && manager.relightingEnabled()) {
            if (manager.relight(serverLevel, pos, state)) {
                held.hurtAndBreak(manager.relightDurabilityCost(), player, hand);
                return InteractionResult.SUCCESS;
            }
        }

        if (TorchManager.isLit(state) && held.isEmpty()) {
            return manager.extinguish(serverLevel, pos, state)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }
}
