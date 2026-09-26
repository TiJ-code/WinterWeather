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
        TorchState existing = data.get(pos);
        if (existing != null) {
            return;
        }

        if (config.get().useUnlitState()) {
            BlockState unlitState = unlitState(state);
            if (unlitState != state) {
                level.setBlock(pos, unlitState, 3);
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
        if (isLocked(level, pos)) {
            return false;
        }

        level.setBlock(pos, withLit(state, false), 3);
        TorchData data = TorchData.get(level);
        TorchState previous = data.get(pos);
        data.set(pos, new TorchState(0, previous != null && previous.locked(), previous != null && previous.suppressSmoke()));
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean relight(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().torchesEnabled() || !isExtinguishable(state) || isLit(state)) {
            return false;
        }

        level.setBlock(pos, withLit(state, true), 11);
        TorchData data = TorchData.get(level);
        TorchState previous = data.get(pos);
        boolean locked = previous != null && previous.locked();
        int burnoutTicks = burnoutTicks(state);
        long extinguishAt = !locked && burnoutTicks > 0
                ? level.getGameTime() + burnoutTicks
                : 0;
        data.set(pos, new TorchState(extinguishAt, locked, previous != null && previous.suppressSmoke()));
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean setLocked(ServerLevel level, BlockPos pos, boolean locked) {
        BlockState state = level.getBlockState(pos);
        if (!isExtinguishable(state)) {
            return false;
        }

        TorchData data = TorchData.get(level);
        TorchState previous = data.get(pos);
        if (previous == null) {
            previous = new TorchState(0);
        }

        if (previous.locked() == locked) {
            data.set(pos, previous);
            return true;
        }

        long extinguishAt = 0;
        if (!locked && isLit(state)) {
            int burnoutTicks = burnoutTicks(state);
            if (burnoutTicks > 0) {
                extinguishAt = level.getGameTime() + burnoutTicks;
            }
        }

        data.set(pos, new TorchState(extinguishAt, locked, previous.suppressSmoke()));
        return true;
    }

    public boolean setSmokeSuppressed(ServerLevel level, BlockPos pos, boolean suppressed) {
        if (!(level.getBlockState(pos).getBlock() instanceof CampfireBlock)) {
            return false;
        }
        TorchData data = TorchData.get(level);
        TorchState previous = data.get(pos);
        if (previous == null) previous = new TorchState(0);
        data.set(pos, previous.withSmokeSuppressed(suppressed));
        return true;
    }

    public boolean isSmokeSuppressed(ServerLevel level, BlockPos pos) {
        TorchState state = TorchData.get(level).get(pos);
        return state != null && state.suppressSmoke();
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

            TorchState torchState = entry.getValue();
            if (torchState.locked()) {
                continue;
            }

            if (isLit(state) && torchState.isExpired(level.getGameTime())) {
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

    public boolean isLocked(ServerLevel level, BlockPos pos) {
        TorchState state = TorchData.get(level).get(pos);
        return state != null && state.locked();
    }
}
