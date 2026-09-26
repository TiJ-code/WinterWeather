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

    public HeatSourceData() {
    }

    private HeatSourceData(java.util.List<Entry> entries) {
        for (Entry entry : entries) {
            this.entries.put(entry.pos(), new HeatSourceState(entry.extinguishAt(), entry.locked(), entry.suppressSmoke()));
        }
    }

    public static HeatSourceData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public HeatSourceState get(BlockPos pos) {
        return entries.get(pos.asLong());
    }

    public Set<Map.Entry<Long, HeatSourceState>> entries() {
        return entries.entrySet();
    }

    public void set(BlockPos pos, HeatSourceState state) {
        entries.put(pos.asLong(), state);
        setDirty();
    }

    public void remove(BlockPos pos) {
        if (entries.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    private record Entry(long pos, long extinguishAt, boolean locked, boolean suppressSmoke) {
    }
}
