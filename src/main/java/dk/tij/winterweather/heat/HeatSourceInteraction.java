package dk.tij.winterweather.heat;

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

public final class HeatSourceInteraction {
    private HeatSourceInteraction() {
    }

    public static void register(HeatSourceManager manager) {
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                interact(manager, player, level, hand, hit));
    }

    private static InteractionResult interact(HeatSourceManager manager, Player player, Level level,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getItemInHand(hand);
        if (!manager.isExtinguishable(state)
                && !(held.is(Items.FLINT_AND_STEEL) && manager.isRelightable(state))) {
            return InteractionResult.PASS;
        }

        if (held.is(Items.FLINT_AND_STEEL)) {
            if (!HeatSourceManager.isLit(state) && manager.relightingEnabled()) {
                if (!(level instanceof ServerLevel serverLevel)) {
                    return InteractionResult.SUCCESS;
                }
                if (manager.relight(serverLevel, pos, state)) {
                    held.hurtAndBreak(manager.relightDurabilityCost(), player, hand);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        if (HeatSourceManager.isLit(state) && held.isEmpty()) {
            return manager.extinguish(serverLevel, pos, state)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }
}
