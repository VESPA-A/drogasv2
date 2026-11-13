package dev.drogas;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Descoberta de informacoes: documentos, anotacoes de estruturas, uso, colheita e experimentacao em maquinas. */
public final class Knowledge {
    private Knowledge() {}

    public static KnowledgeState state(Player p) {
        return p.getAttachedOrElse(ModAttachments.KNOWLEDGE, KnowledgeState.EMPTY);
    }

    public static boolean has(Player p, String id) {
        KnowledgeData.Entry e = KnowledgeData.BY_ID.get(id);
        return e != null && (e.unlockedByDefault() || state(p).has(id));
    }

    /** Libera entradas; retorna quantas eram novas. */
    public static int unlock(ServerPlayer p, String... ids) {
        KnowledgeState cur = state(p);
        Set<String> add = new LinkedHashSet<>();
        for (String id : ids) {
            KnowledgeData.Entry e = KnowledgeData.BY_ID.get(id);
            if (e != null && !e.unlockedByDefault() && !cur.has(id)) add.add(id);
        }
        if (add.isEmpty()) return 0;
        p.setAttached(ModAttachments.KNOWLEDGE, cur.with(add));
        Component msg = add.size() == 1
                ? Component.translatable("message.drogas.discovered", Component.translatable("enc.drogas." + add.iterator().next() + ".name"))
                : Component.translatable("message.drogas.discovered_many", add.size());
        p.displayClientMessage(msg.copy().withStyle(ChatFormatting.GOLD), true);
        Fx.sound(p, "drogas:ui.discover", 0.7f, 1.0f);
        return add.size();
    }

    public static int unlockGroup(ServerPlayer p, String group) {
        List<String> ids = KnowledgeData.GROUPS.get(group);
        return ids == null ? 0 : unlock(p, ids.toArray(new String[0]));
    }

    public static void unlockAll(ServerPlayer p) {
        List<String> all = new ArrayList<>();
        for (KnowledgeData.Entry e : KnowledgeData.ALL) all.add(e.id());
        p.setAttached(ModAttachments.KNOWLEDGE, state(p).with(all));
    }

    /** Possuir um equipamento/semente revela o equipamento e como fabricá-lo (engenharia reversa). */
    public static void scanInventory(ServerPlayer p) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (st.isEmpty()) continue;
            List<String> ids = KnowledgeData.ITEM_TRIGGERS.get(BuiltInRegistries.ITEM.getKey(st.getItem()).getPath());
            if (ids != null) unlock(p, ids.toArray(new String[0]));
        }
    }
}
