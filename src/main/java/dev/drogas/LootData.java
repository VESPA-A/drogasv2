package dev.drogas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GERADO por tools/generate2.py - nao edite a mao. Raridades e regras de injecao de loot em baus vanilla. */
public final class LootData {
    private LootData() {}

    public record E(String item, int weight, int min, int max) {}
    public record Rule(String tier, float chance) {}

    /** A = comum (sementes basicas) ... D = muito raro (produtos finais e equipamentos avancados). */
    public static final Map<String, List<E>> TIERS = Map.of(
            "A", List.of(new E("cannabis_seeds", 10, 1, 3), new E("tobacco_seeds", 8, 1, 3), new E("poppy_seeds", 8, 1, 3), new E("psilocybin_spores", 6, 1, 2), new E("rolling_paper", 8, 1, 4), new E("organic_fertilizer", 5, 1, 2), new E("syringe", 2, 1, 2), new E("doc_field_guide_basic", 3, 1, 1), new E("doc_mycology_notes", 2, 1, 1)),
            "B", List.of(new E("coca_seeds", 4, 1, 2), new E("jagube_seeds", 3, 1, 2), new E("chacrona_seeds", 4, 1, 2), new E("maintenance_kit", 3, 1, 1), new E("speed_module_1", 2, 1, 1), new E("trellis", 3, 1, 3), new E("organic_substrate", 3, 1, 3), new E("solvent", 2, 1, 1), new E("doc_drying_manual", 2, 1, 1), new E("doc_grinder_manual", 2, 1, 1), new E("doc_table_manual", 2, 1, 1), new E("doc_jungle_expedition", 2, 1, 1), new E("doc_greenhouse_blueprint", 2, 1, 1), new E("doc_brewing_notes", 2, 1, 1)),
            "C", List.of(new E("solvent", 4, 1, 2), new E("chem_base", 3, 1, 2), new E("catalyst", 2, 1, 1), new E("raw_crystal", 3, 1, 3), new E("coca_paste", 1, 1, 1), new E("morphine_base", 1, 1, 1), new E("lsd_solution", 1, 1, 1), new E("dmt_crystals", 1, 1, 1), new E("speed_module_2", 1, 1, 1), new E("greenhouse_regulator", 1, 1, 1), new E("doc_chem_safety", 2, 1, 1), new E("doc_opioid_research", 1, 1, 1), new E("doc_stimulant_research", 1, 1, 1), new E("doc_psychedelic_research", 1, 1, 1), new E("doc_synthetics_lab", 1, 1, 1), new E("doc_maintenance_log", 2, 1, 1)),
            "D", List.of(new E("cocaine_powder", 1, 1, 1), new E("crack_rock", 1, 1, 1), new E("meth_shard", 1, 1, 1), new E("heroin_dose", 1, 1, 1), new E("lsd_blotter", 1, 1, 2), new E("mdma_pill", 1, 1, 2), new E("ketamine_vial", 1, 1, 1), new E("dmt_dose", 1, 1, 1), new E("nebulon_tab", 1, 1, 1), new E("zyrax_vial", 1, 1, 1), new E("chemistry_bench", 1, 1, 1), new E("processing_furnace", 1, 1, 1), new E("speed_module_3", 1, 1, 1), new E("doc_quality_study", 1, 1, 1), new E("doc_addiction_study", 1, 1, 1)));

    public static final Map<String, List<Rule>> INJECT = new HashMap<>();
    static {
        INJECT.put("chests/simple_dungeon", List.of(new Rule("A", 0.3f), new Rule("B", 0.1f), new Rule("C", 0.03f)));
        INJECT.put("chests/abandoned_mineshaft", List.of(new Rule("A", 0.2f), new Rule("B", 0.08f), new Rule("C", 0.03f), new Rule("D", 0.005f)));
        INJECT.put("chests/desert_pyramid", List.of(new Rule("A", 0.25f), new Rule("B", 0.15f), new Rule("C", 0.06f), new Rule("D", 0.01f)));
        INJECT.put("chests/jungle_temple", List.of(new Rule("A", 0.25f), new Rule("B", 0.18f), new Rule("C", 0.06f), new Rule("D", 0.012f)));
        INJECT.put("chests/igloo_chest", List.of(new Rule("A", 0.2f), new Rule("B", 0.1f)));
        INJECT.put("chests/pillager_outpost", List.of(new Rule("A", 0.25f), new Rule("B", 0.12f), new Rule("C", 0.04f)));
        INJECT.put("chests/shipwreck_supply", List.of(new Rule("A", 0.25f)));
        INJECT.put("chests/shipwreck_treasure", List.of(new Rule("B", 0.2f), new Rule("C", 0.08f), new Rule("D", 0.01f)));
        INJECT.put("chests/underwater_ruin_small", List.of(new Rule("A", 0.15f)));
        INJECT.put("chests/underwater_ruin_big", List.of(new Rule("B", 0.15f), new Rule("C", 0.05f)));
        INJECT.put("chests/buried_treasure", List.of(new Rule("B", 0.3f), new Rule("C", 0.15f), new Rule("D", 0.03f)));
        INJECT.put("chests/woodland_mansion", List.of(new Rule("A", 0.3f), new Rule("B", 0.25f), new Rule("C", 0.12f), new Rule("D", 0.03f)));
        INJECT.put("chests/stronghold_corridor", List.of(new Rule("A", 0.15f), new Rule("B", 0.12f)));
        INJECT.put("chests/stronghold_crossing", List.of(new Rule("A", 0.15f), new Rule("B", 0.12f)));
        INJECT.put("chests/stronghold_library", List.of(new Rule("B", 0.3f), new Rule("C", 0.1f), new Rule("D", 0.02f)));
        INJECT.put("chests/ruined_portal", List.of(new Rule("A", 0.15f), new Rule("B", 0.1f), new Rule("C", 0.05f)));
        INJECT.put("chests/nether_bridge", List.of(new Rule("C", 0.08f), new Rule("D", 0.01f)));
        INJECT.put("chests/bastion_other", List.of(new Rule("C", 0.12f), new Rule("D", 0.02f)));
        INJECT.put("chests/bastion_treasure", List.of(new Rule("C", 0.2f), new Rule("D", 0.06f)));
        INJECT.put("chests/bastion_bridge", List.of(new Rule("C", 0.05f)));
        INJECT.put("chests/bastion_hoglin_stable", List.of(new Rule("C", 0.05f)));
        INJECT.put("chests/ancient_city", List.of(new Rule("B", 0.3f), new Rule("C", 0.2f), new Rule("D", 0.06f)));
        INJECT.put("chests/ancient_city_ice_box", List.of(new Rule("C", 0.1f)));
        INJECT.put("chests/end_city_treasure", List.of(new Rule("C", 0.2f), new Rule("D", 0.08f)));
        INJECT.put("chests/village/village_plains_house", List.of(new Rule("A", 0.22f), new Rule("B", 0.04f)));
        INJECT.put("chests/village/village_taiga_house", List.of(new Rule("A", 0.22f), new Rule("B", 0.04f)));
        INJECT.put("chests/village/village_savanna_house", List.of(new Rule("A", 0.22f), new Rule("B", 0.04f)));
        INJECT.put("chests/village/village_desert_house", List.of(new Rule("A", 0.22f), new Rule("B", 0.04f)));
        INJECT.put("chests/village/village_snowy_house", List.of(new Rule("A", 0.22f), new Rule("B", 0.04f)));
        INJECT.put("chests/village/village_toolsmith", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_cartographer", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_temple", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_shepherd", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_butcher", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_fisher", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_fletcher", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_mason", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_tannery", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_weaponsmith", List.of(new Rule("A", 0.12f)));
        INJECT.put("chests/village/village_armorer", List.of(new Rule("A", 0.12f)));
    }
}
