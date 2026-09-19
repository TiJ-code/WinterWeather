package dk.tij.winterweather.torch;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.TorchData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public final class TorchManager {
    private final Supplier<FreezingConfig> config;

    public TorchManager(Supplier<FreezingConfig> config) {
        this.config = config;
    }

    public void registerPlacedTorch(ServerLevel level, BlockPos pos, BlockState state) {
        if (config.get().torchesEnabled() && TorchBlocks.isTorch(state)) {
            int burnoutTicks = burnoutTicks();
            if (burnoutTicks > 0) {
                TorchData.get(level).set(pos, new TorchState(level.getGameTime() + burnoutTicks));
            }
        }
    }

    public void removeTorch(ServerLevel level, BlockPos pos) {
        TorchData.get(level).remove(pos);
    }

    public boolean extinguish(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !TorchBlocks.isTorch(state) || !isLit(state)) {
            return false;
        }

        level.setBlock(pos, state.setValue(TorchBlocks.LIT, false), 3);
        TorchData.get(level).set(pos, new TorchState(0));
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean relight(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !TorchBlocks.isTorch(state) || isLit(state)) {
            return false;
        }

        level.setBlock(pos, state.setValue(TorchBlocks.LIT, true), 11);
        int burnoutTicks = burnoutTicks();
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
            if (!TorchBlocks.isTorch(state)) {
                data.remove(pos);
                continue;
            }

            if (isLit(state) && entry.getValue().isExpired(level.getGameTime())) {
                extinguish(level, pos, state);
            }
        }
    }

    public static boolean isLit(BlockState state) {
        return !state.hasProperty(TorchBlocks.LIT)
                || state.getValue(TorchBlocks.LIT);
    }

    private int burnoutTicks() {
        return Math.max(0, config.get().torchBurnoutSeconds()) * 20;
    }

    public int relightDurabilityCost() {
        return config.get().torchRelightDurabilityCost();
    }

    public boolean relightingEnabled() {
        return config.get().torchRelightingEnabled();
    }
}
