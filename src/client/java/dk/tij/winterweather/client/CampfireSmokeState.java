package dk.tij.winterweather.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CampfireSmokeState {
    private static final Map<Identifier, Set<Long>> SUPPRESSED = new HashMap<>();
    private CampfireSmokeState() {}

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

    public static boolean isSuppressed(Identifier dimension, BlockPos pos) {
        return SUPPRESSED.getOrDefault(dimension, Set.of()).contains(pos.asLong());
    }

    public static void clear() { SUPPRESSED.clear(); }
}
