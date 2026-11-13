package dev.drogas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Injeta itens do mod nos baus de estruturas vanilla; a raridade acompanha a dificuldade de obtencao (LootData). */
public final class LootInjector {
    private LootInjector() {}

    private static final Map<ResourceKey<LootTable>, List<LootData.Rule>> RULES = new HashMap<>();

    public static void init() {
        for (Map.Entry<String, List<LootData.Rule>> e : LootData.INJECT.entrySet()) {
            RULES.put(ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace(e.getKey())), e.getValue());
        }
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            List<LootData.Rule> rules = RULES.get(key);
            if (rules == null || !source.isBuiltin()) return;
            for (LootData.Rule rule : rules) tableBuilder.withPool(pool(rule));
        });
    }

    /** Um pool com 1 rolagem: a chance do pool vem do peso da entrada vazia. */
    private static LootPool.Builder pool(LootData.Rule rule) {
        List<LootData.E> entries = LootData.TIERS.get(rule.tier());
        int total = 0;
        for (LootData.E e : entries) total += e.weight();
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f));
        for (LootData.E e : entries) {
            pool.add(LootItem.lootTableItem(ModItems.get(e.item())).setWeight(e.weight())
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(e.min(), e.max()))));
        }
        int empty = Math.max(1, Math.round(total * (1f - rule.chance()) / rule.chance()));
        pool.add(EmptyLootItem.emptyItem().setWeight(empty));
        return pool;
    }
}
