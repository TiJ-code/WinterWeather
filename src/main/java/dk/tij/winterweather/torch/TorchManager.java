package dk.tij.winterweather.torch;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.TorchData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public final class TorchManager {
    private final Supplier<FreezingConfig> config;

    public TorchManager(Supplier<FreezingConfig> config) {
        this.config = config;
    }

    public void registerPlacedTorch(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !isExtinguishable(state)) {
            return;
        }

        TorchData data = TorchData.get(level);
        if (data.get(pos) != null) {
            return;
        }

        if (config.get().useUnlitState()) {
            BlockState unlitState = unlitState(state);
            if (unlitState != state) {
                level.setBlock(pos, unlitState, 3);
                state = unlitState;
            }
            data.set(pos, new TorchState(0));
            return;
        }

        int burnoutTicks = burnoutTicks(state);
        if (burnoutTicks > 0 && isLit(state)) {
            data.set(pos, new TorchState(level.getGameTime() + burnoutTicks));
        } else {
            data.set(pos, new TorchState(0));
        }
    }

    public void removeTorch(ServerLevel level, BlockPos pos) {
        TorchData.get(level).remove(pos);
    }

    public boolean extinguish(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !isExtinguishable(state) || !isLit(state)) {
            return false;
        }

        level.setBlock(pos, withLit(state, false), 3);
        TorchData.get(level).set(pos, new TorchState(0));
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean relight(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !isExtinguishable(state) || isLit(state)) {
            return false;
        }

        level.setBlock(pos, withLit(state, true), 11);
        int burnoutTicks = burnoutTicks(state);
        if (burnoutTicks > 0) {
            TorchData.get(level).set(pos, new TorchState(level.getGameTime() + burnoutTicks));
        }
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public void tick(ServerLevel level) {
        if (!config.get().torchesEnabled()) {
            return;
        }

        TorchData data = TorchData.get(level);
        for (var entry : new java.util.ArrayList<>(data.entries())) {
            BlockPos pos = BlockPos.of(entry.getKey());
            BlockState state = level.getBlockState(pos);
            if (!isExtinguishable(state)) {
                data.remove(pos);
                continue;
            }

            if (isLit(state) && entry.getValue().isExpired(level.getGameTime())) {
                extinguish(level, pos, state);
            }
        }
    }

    public static boolean isLit(BlockState state) {
        if (state.hasProperty(TorchBlocks.LIT)) {
            return state.getValue(TorchBlocks.LIT);
        }
        return !state.hasProperty(CampfireBlock.LIT)
                || state.getValue(CampfireBlock.LIT);
    }

    public static BlockState withLit(BlockState state, boolean lit) {
        if (state.hasProperty(TorchBlocks.LIT)) {
            return state.setValue(TorchBlocks.LIT, lit);
        }
        if (state.hasProperty(CampfireBlock.LIT)) {
            return state.setValue(CampfireBlock.LIT, lit);
        }
        return state;
    }

    public static BlockState unlitState(BlockState state) {
        return withLit(state, false);
    }

    private int burnoutTicks(BlockState state) {
        return Math.max(0, config.get().extinguishableBurnoutSeconds(state)) * 20;
    }

    public int relightDurabilityCost() {
        return config.get().torchRelightDurabilityCost();
    }

    public boolean relightingEnabled() {
        return config.get().torchRelightingEnabled();
    }

    public boolean isExtinguishable(BlockState state) {
        return config.get().isExtinguishable(state);
    }
}
