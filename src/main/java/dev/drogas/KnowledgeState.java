package dev.drogas;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Entradas da enciclopedia descobertas pelo jogador (persistente e sincronizado com o dono). */
public record KnowledgeState(List<String> ids) {
    public static final KnowledgeState EMPTY = new KnowledgeState(List.of());
    public static final Codec<KnowledgeState> CODEC = Codec.STRING.listOf().xmap(KnowledgeState::new, KnowledgeState::ids);
    public static final StreamCodec<ByteBuf, KnowledgeState> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(KnowledgeState::new, KnowledgeState::ids);

    public KnowledgeState {
        ids = List.copyOf(ids);
    }

    public boolean has(String id) { return ids.contains(id); }

    public KnowledgeState with(Collection<String> add) {
        List<String> out = new ArrayList<>(ids);
        for (String s : add) if (!out.contains(s)) out.add(s);
        return new KnowledgeState(out);
    }
}
