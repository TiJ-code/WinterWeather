package dk.tij.winterweather.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.tij.winterweather.torch.TorchState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class TorchData extends SavedData {
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
            Codec.LONG.fieldOf("extinguish_at").forGetter(Entry::extinguishAt),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(Entry::locked),
            Codec.BOOL.optionalFieldOf("suppress_smoke", false).forGetter(Entry::suppressSmoke)
    ).apply(instance, Entry::new));
    private static final Codec<TorchData> CODEC = ENTRY_CODEC.listOf()
            .xmap(TorchData::new, data -> data.entries().stream()
                    .map(entry -> new Entry(
                            entry.getKey(),
                            entry.getValue().extinguishAt(),
                            entry.getValue().locked(), entry.getValue().suppressSmoke()))
                    .toList());
    private static final SavedDataType<TorchData> TYPE = new SavedDataType<>(
            Identifier.parse("winterweather/torches"),
            TorchData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<Long, TorchState> entries = new HashMap<>();

    public TorchData() {
    }

    private TorchData(java.util.List<Entry> entries) {
        for (Entry entry : entries) {
            this.entries.put(entry.pos(), new TorchState(entry.extinguishAt(), entry.locked(), entry.suppressSmoke()));
        }
    }

    public static TorchData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public TorchState get(BlockPos pos) {
        return entries.get(pos.asLong());
    }

    public Set<Map.Entry<Long, TorchState>> entries() {
        return entries.entrySet();
    }

    public void set(BlockPos pos, TorchState state) {
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
