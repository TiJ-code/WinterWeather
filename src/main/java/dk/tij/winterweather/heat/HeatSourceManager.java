package dk.tij.winterweather.heat;

import dk.tij.winterweather.config.FreezingConfig;
import dk.tij.winterweather.data.HeatSourceData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;


/**
 * Manages heat source state and behavior.
 */
public final class HeatSourceManager {
    /**
     * Stores the config value.
     */
    private final Supplier<FreezingConfig> config;

    /**
     * Performs the heat source manager operation.
     *
     * @param config the config value
     */
    public HeatSourceManager(Supplier<FreezingConfig> config) {
        this.config = config;
    }

    /**
     * Reports whether lit is true.
     *
     * @param state the state value
     */
    public static boolean isLit(BlockState state) {
        if (state.hasProperty(HeatSourceBlocks.LIT)) {
            return state.getValue(HeatSourceBlocks.LIT);
        }
        return !state.hasProperty(CampfireBlock.LIT)
                || state.getValue(CampfireBlock.LIT);
    }

    /**
     * Performs the with lit operation.
     *
     * @param state the state value
     * @param lit   the lit value
     */
    public static BlockState withLit(BlockState state, boolean lit) {
        if (state.hasProperty(HeatSourceBlocks.LIT)) {
            return state.setValue(HeatSourceBlocks.LIT, lit);
        }
        if (state.hasProperty(CampfireBlock.LIT)) {
            return state.setValue(CampfireBlock.LIT, lit);
        }
        return state;
    }

    /**
     * Performs the unlit state operation.
     *
     * @param state the state value
     */
    public static BlockState unlitState(BlockState state) {
        return withLit(state, false);
    }

    /**
     * Performs the register placed heat source operation.
     *
     * @param level the level value
     * @param pos   the pos value
     * @param state the state value
     */
    public void registerPlacedHeatSource(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().heatSourcesEnabled() || !isExtinguishable(state)) {
            return;
        }

        HeatSourceData data = HeatSourceData.get(level);
        HeatSourceState existing = data.get(pos);
        if (existing != null) {
            return;
        }

        if (config.get().useUnlitState()) {
            BlockState unlitState = unlitState(state);
            if (unlitState != state) {
                level.setBlock(pos, unlitState, 3);
            }
            data.set(pos, new HeatSourceState(0));
            return;
        }

        int burnoutTicks = burnoutTicks(state);
        if (burnoutTicks > 0 && isLit(state)) {
            data.set(pos, new HeatSourceState(level.getGameTime() + burnoutTicks));
        } else {
            data.set(pos, new HeatSourceState(0));
        }
    }

    /**
     * Performs the remove heat source operation.
     *
     * @param level the level value
     * @param pos   the pos value
     */
    public void removeHeatSource(ServerLevel level, BlockPos pos) {
        HeatSourceData.get(level).remove(pos);
    }

    /**
     * Performs the extinguish operation.
     *
     * @param level the level value
     * @param pos   the pos value
     * @param state the state value
     */
    public boolean extinguish(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().heatSourcesEnabled() || !isExtinguishable(state) || !isLit(state)) {
            return false;
        }
        if (isLocked(level, pos)) {
            return false;
        }
        level.setBlock(pos, withLit(state, false), 3);
        HeatSourceData data = HeatSourceData.get(level);
        HeatSourceState previous = data.get(pos);
        data.set(pos, new HeatSourceState(0, previous != null && previous.locked(), previous != null && previous.suppressSmoke()));
        level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    /**
     * Performs the relight operation.
     *
     * @param level the level value
     * @param pos   the pos value
     * @param state the state value
     */
    public boolean relight(ServerLevel level, BlockPos pos, BlockState state) {
        if (!config.get().heatSourcesEnabled() || !isRelightable(state) || isLit(state)) {
            return false;
        }
        level.setBlock(pos, withLit(state, true), 11);
        HeatSourceData data = HeatSourceData.get(level);
        HeatSourceState previous = data.get(pos);
        boolean locked = previous != null && previous.locked();
        int burnoutTicks = burnoutTicks(state);
        long extinguishAt = !locked && burnoutTicks > 0
                ? level.getGameTime() + burnoutTicks
                : 0;
        data.set(pos, new HeatSourceState(extinguishAt, locked, previous != null && previous.suppressSmoke()));
        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    /**
     * Performs the set locked operation.
     *
     * @param level  the level value
     * @param pos    the pos value
     * @param locked the locked value
     */
    public boolean setLocked(ServerLevel level, BlockPos pos, boolean locked) {
        BlockState state = level.getBlockState(pos);
        if (!isExtinguishable(state)) {
            return false;
        }
        HeatSourceData data = HeatSourceData.get(level);
        HeatSourceState previous = data.get(pos);
        if (previous == null) {
            previous = new HeatSourceState(0);
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
        data.set(pos, new HeatSourceState(extinguishAt, locked, previous.suppressSmoke()));
        return true;
    }

    /**
     * Performs the set smoke suppressed operation.
     *
     * @param level      the level value
     * @param pos        the pos value
     * @param suppressed the suppressed value
     */
    public boolean setSmokeSuppressed(ServerLevel level, BlockPos pos, boolean suppressed) {
        if (!(level.getBlockState(pos).getBlock() instanceof CampfireBlock)) {
            return false;
        }
        HeatSourceData data = HeatSourceData.get(level);
        HeatSourceState previous = data.get(pos);
        if (previous == null) previous = new HeatSourceState(0);
        data.set(pos, previous.withSmokeSuppressed(suppressed));
        return true;
    }

    /**
     * Reports whether smoke suppressed is true.
     *
     * @param level the level value
     * @param pos   the pos value
     */
    public boolean isSmokeSuppressed(ServerLevel level, BlockPos pos) {
        HeatSourceState state = HeatSourceData.get(level).get(pos);
        return state != null && state.suppressSmoke();
    }

    /**
     * Performs the tick operation.
     *
     * @param level the level value
     */
    public void tick(ServerLevel level) {
        if (!config.get().heatSourcesEnabled()) {
            return;
        }

        HeatSourceData data = HeatSourceData.get(level);
        for (var entry : new java.util.ArrayList<>(data.entries())) {
            BlockPos pos = BlockPos.of(entry.getKey());
            BlockState state = level.getBlockState(pos);
            if (!isExtinguishable(state)) {
                data.remove(pos);
                continue;
            }

            HeatSourceState heatSourceState = entry.getValue();
            if (heatSourceState.locked()) {
                continue;
            }

            if (isLit(state) && heatSourceState.isExpired(level.getGameTime())) {
                extinguish(level, pos, state);
            }
        }
    }

    /**
     * Performs the burnout ticks operation.
     *
     * @param state the state value
     */
    private int burnoutTicks(BlockState state) {
        return Math.max(0, config.get().extinguishableBurnoutSeconds(state)) * 20;
    }

    /**
     * Performs the relight durability cost operation.
     */
    public int relightDurabilityCost() {
        return config.get().heatSourceRelightDurabilityCost();
    }

    /**
     * Performs the relighting enabled operation.
     */
    public boolean relightingEnabled() {
        return config.get().heatSourceRelightingEnabled();
    }

    /**
     * Reports whether extinguishable is true.
     *
     * @param state the state value
     */
    public boolean isExtinguishable(BlockState state) {
        if (config.get().isExtinguishable(state)) {
            return true;
        }
        return config.get().heatSourcesEnabled()
                && state.getBlock() instanceof LanternBlock
                && state.hasProperty(HeatSourceBlocks.LIT);
    }

    /**
     * Reports whether relightable is true.
     *
     * @param state the state value
     */
    public boolean isRelightable(BlockState state) {
        return isExtinguishable(state);
    }

    /**
     * Reports whether locked is true.
     *
     * @param level the level value
     * @param pos   the pos value
     */
    public boolean isLocked(ServerLevel level, BlockPos pos) {
        HeatSourceState state = HeatSourceData.get(level).get(pos);
        return state != null && state.locked();
    }
}
