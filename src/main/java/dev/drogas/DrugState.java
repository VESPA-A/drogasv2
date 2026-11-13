package dev.drogas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Estado de dependencia de um jogador (imutavel; listas indexadas por Substance.ordinal()).
 * E salvo no jogador e sincronizado com o proprio cliente via Fabric Data Attachments.
 */
public record DrugState(List<Float> tolerance, List<Float> dependence, List<Float> load, List<Integer> withdrawal, List<Long> lastUse) {
    public static final int N = Substance.values().length;
    public static final DrugState EMPTY = new DrugState(List.of(), List.of(), List.of(), List.of(), List.of());

    public static final Codec<DrugState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.listOf().optionalFieldOf("tolerance", List.of()).forGetter(DrugState::tolerance),
            Codec.FLOAT.listOf().optionalFieldOf("dependence", List.of()).forGetter(DrugState::dependence),
            Codec.FLOAT.listOf().optionalFieldOf("load", List.of()).forGetter(DrugState::load),
            Codec.INT.listOf().optionalFieldOf("withdrawal", List.of()).forGetter(DrugState::withdrawal),
            Codec.LONG.listOf().optionalFieldOf("last_use", List.of()).forGetter(DrugState::lastUse)
    ).apply(i, DrugState::new));

    public static final StreamCodec<ByteBuf, DrugState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), DrugState::tolerance,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), DrugState::dependence,
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), DrugState::load,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), DrugState::withdrawal,
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), DrugState::lastUse,
            DrugState::new);

    /** Normaliza o tamanho das listas (compatibilidade se novas substancias forem adicionadas). */
    public DrugState {
        tolerance = fit(tolerance, 0f);
        dependence = fit(dependence, 0f);
        load = fit(load, 0f);
        withdrawal = fit(withdrawal, 0);
        lastUse = fit(lastUse, -1L);
    }

    private static <T> List<T> fit(List<T> in, T fill) {
        List<T> out = new ArrayList<>(N);
        for (int i = 0; i < N; i++) out.add(i < in.size() ? in.get(i) : fill);
        return List.copyOf(out);
    }

    public float tol(Substance s) { return tolerance.get(s.ordinal()); }
    public float dep(Substance s) { return dependence.get(s.ordinal()); }
    public float loadOf(Substance s) { return load.get(s.ordinal()); }
    public int withdrawalLevel(Substance s) { return withdrawal.get(s.ordinal()); }
    public long lastUseOf(Substance s) { return lastUse.get(s.ordinal()); }

    public int maxWithdrawal() {
        int m = 0;
        for (int v : withdrawal) m = Math.max(m, v);
        return m;
    }

    public Mutable mutable() { return new Mutable(this); }

    /** Copia editavel usada pela logica de servidor. */
    public static final class Mutable {
        public final float[] tol = new float[N], dep = new float[N], load = new float[N];
        public final int[] wd = new int[N];
        public final long[] last = new long[N];

        Mutable(DrugState s) {
            for (int i = 0; i < N; i++) {
                tol[i] = s.tolerance.get(i); dep[i] = s.dependence.get(i); load[i] = s.load.get(i);
                wd[i] = s.withdrawal.get(i); last[i] = s.lastUse.get(i);
            }
        }

        private static float q(float v) { return Math.round(Math.max(0f, v) * 1000f) / 1000f; }

        public DrugState freeze() {
            List<Float> t = new ArrayList<>(N), d = new ArrayList<>(N), l = new ArrayList<>(N);
            List<Integer> w = new ArrayList<>(N); List<Long> u = new ArrayList<>(N);
            for (int i = 0; i < N; i++) {
                t.add(q(Math.min(1f, tol[i]))); d.add(q(Math.min(1f, dep[i]))); l.add(q(load[i]));
                w.add(wd[i]); u.add(last[i]);
            }
            return new DrugState(t, d, l, w, u);
        }
    }
}
