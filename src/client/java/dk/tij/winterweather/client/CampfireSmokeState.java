package dk.tij.winterweather.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Represents the current campfire smoke state.
 */
public final class CampfireSmokeState {
    private static final Map<Identifier, Set<Long>> SUPPRESSED = new HashMap<>();

    /**
     * Performs the campfire smoke state operation.
     */
    private CampfireSmokeState() {
    }

    /**
     * Performs the set operation.
     *
     * @param dimension  the dimension value
     * @param pos        the pos value
     * @param suppressed the suppressed value
     */
    public static void set(Identifier dimension, BlockPos pos, boolean suppressed) {
        if (suppressed) {
            SUPPRESSED.computeIfAbsent(dimension, ignored -> new HashSet<>()).add(pos.asLong());
        } else {
            Set<Long> positions = SUPPRESSED.get(dimension);
            if (positions != null) {
                positions.remove(pos.asLong());
                if (positions.isEmpty()) SUPPRESSED.remove(dimension);
            }
        }
    }

    /**
     * Reports whether suppressed is true.
     *
     * @param dimension the dimension value
     * @param pos       the pos value
     */
    public static boolean isSuppressed(Identifier dimension, BlockPos pos) {
        return SUPPRESSED.getOrDefault(dimension, Set.of()).contains(pos.asLong());
    }

    /**
     * Performs the clear operation.
     */
    public static void clear() {
        SUPPRESSED.clear();
    }
}
