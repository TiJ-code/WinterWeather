package dk.tij.winterweather.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.tij.winterweather.heat.HeatSourceState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores persistent state for extinguishable heat sources in a world.
 */
public final class HeatSourceData extends SavedData {
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
            Codec.LONG.fieldOf("extinguish_at").forGetter(Entry::extinguishAt),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(Entry::locked),
            Codec.BOOL.optionalFieldOf("suppress_smoke", false).forGetter(Entry::suppressSmoke)
    ).apply(instance, Entry::new));
    private static final Codec<HeatSourceData> CODEC = ENTRY_CODEC.listOf()
            .xmap(HeatSourceData::new, data -> data.entries().stream()
                    .map(entry -> new Entry(
                            entry.getKey(),
                            entry.getValue().extinguishAt(),
                            entry.getValue().locked(), entry.getValue().suppressSmoke()))
                    .toList());
    private static final SavedDataType<HeatSourceData> TYPE = new SavedDataType<>(
            Identifier.parse("winterweather/torches"),
            HeatSourceData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<Long, HeatSourceState> entries = new HashMap<>();

    /**
     * Creates an empty heat source data store.
     */
    public HeatSourceData() {
    }

    /**
     * Restores heat source entries decoded from saved world data.
     */
    private HeatSourceData(java.util.List<Entry> entries) {
        for (Entry entry : entries) {
            this.entries.put(entry.pos(), new HeatSourceState(entry.extinguishAt(), entry.locked(), entry.suppressSmoke()));
        }
    }

    /**
     * Gets the persistent heat source data for a server level.
     *
     * @param level world whose saved data is requested
     * @return the level's heat source data
     */
    public static HeatSourceData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Gets the saved state at a block position.
     *
     * @param pos position to look up
     * @return saved state, or {@code null} when no entry exists
     */
    public HeatSourceState get(BlockPos pos) {
        return entries.get(pos.asLong());
    }

    /**
     * Returns the stored position and state entries.
     *
     * @return the backing entry set
     */
    public Set<Map.Entry<Long, HeatSourceState>> entries() {
        return entries.entrySet();
    }

    /**
     * Saves a heat source state and marks the data dirty.
     *
     * @param pos   position to update
     * @param state state to store
     */
    public void set(BlockPos pos, HeatSourceState state) {
        entries.put(pos.asLong(), state);
        setDirty();
    }

    /**
     * Removes a stored entry and marks the data dirty when it existed.
     *
     * @param pos position to remove
     */
    public void remove(BlockPos pos) {
        if (entries.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    /**
     * Codec representation of one persisted heat source.
     */
    private record Entry(long pos, long extinguishAt, boolean locked, boolean suppressSmoke) {
    }
}
