package dev.drogas;

import java.util.Set;

/** GERADO por tools/generate2.py - nao edite a mao. Lista de todos os itens do mod. */
public final class ItemCatalog {
    private ItemCatalog() {}

    public enum Rem { NONE, BOTTLE, SYRINGE }
    /** Forma de consumo: define animacao, sons e particulas. */
    public enum Form { PILL, DRINK, SMOKE, SNORT, INJECT, CHEW }

    /** Itens que carregam o componente de qualidade. */
    public static final Set<String> QUALITY_ITEMS = Set.of("ayahuasca_brew", "beer", "cannabis_brownie", "cannabis_bud", "catalyst", "chacrona_leaf", "chem_base", "cigar", "cigarette", "coca_leaf", "coca_paste", "cocaine_powder", "crack_rock", "dmt_crystals", "dmt_dose", "dried_cannabis", "dried_magic_mushroom", "dried_tobacco", "ground_cannabis", "ground_tobacco", "heroin_dose", "jagube_vine", "joint", "ketamine_vial", "lsd_blotter", "lsd_solution", "magic_mushroom", "mdma_pill", "meth_shard", "morphine_base", "morphine_vial", "nebulon_tab", "opium_gum", "poppy_pod", "raw_crystal", "solvent", "spirit", "tobacco_leaf", "wine", "zyrax_vial");

    public interface Definer {
        void plain(String id, int stack);
        void seeds(String id, String cropBlockId);
        void drug(String id, String substanceId, float dose, Form form, Rem remainder, int stack);
        void clipboard(String id);
        void antidote(String id, int stack);
        void detox(String id, int stack);
        void module(String id, int tier);
        void doc(String id);
        void fertilizer(String id);
        void encyclopedia(String id);
    }

    public static void define(Definer d) {
        d.seeds("cannabis_seeds", "cannabis_crop");
        d.seeds("poppy_seeds", "poppy_crop");
        d.seeds("tobacco_seeds", "tobacco_crop");
        d.seeds("psilocybin_spores", "psilocybin_crop");
        d.seeds("coca_seeds", "coca_crop");
        d.seeds("jagube_seeds", "jagube_crop");
        d.seeds("chacrona_seeds", "chacrona_crop");
        d.plain("cannabis_bud", 64);
        d.plain("dried_cannabis", 64);
        d.plain("ground_cannabis", 64);
        d.plain("rolling_paper", 64);
        d.drug("joint", "cannabis", 1.0f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 16);
        d.drug("cannabis_brownie", "cannabis", 1.5f, ItemCatalog.Form.CHEW, ItemCatalog.Rem.NONE, 16);
        d.plain("poppy_pod", 64);
        d.drug("opium_gum", "opium", 1.0f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 16);
        d.plain("morphine_base", 64);
        d.drug("morphine_vial", "morphine", 1.0f, ItemCatalog.Form.INJECT, ItemCatalog.Rem.BOTTLE, 16);
        d.drug("heroin_dose", "heroin", 1.0f, ItemCatalog.Form.INJECT, ItemCatalog.Rem.SYRINGE, 16);
        d.plain("syringe", 16);
        d.plain("tobacco_leaf", 64);
        d.plain("dried_tobacco", 64);
        d.plain("ground_tobacco", 64);
        d.drug("cigarette", "tobacco", 0.7f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 20);
        d.drug("cigar", "tobacco", 1.6f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 8);
        d.drug("magic_mushroom", "psilocybin", 0.8f, ItemCatalog.Form.CHEW, ItemCatalog.Rem.NONE, 16);
        d.drug("dried_magic_mushroom", "psilocybin", 1.1f, ItemCatalog.Form.CHEW, ItemCatalog.Rem.NONE, 16);
        d.drug("coca_leaf", "cocaine", 0.2f, ItemCatalog.Form.CHEW, ItemCatalog.Rem.NONE, 64);
        d.plain("coca_paste", 64);
        d.drug("cocaine_powder", "cocaine", 1.0f, ItemCatalog.Form.SNORT, ItemCatalog.Rem.NONE, 16);
        d.drug("crack_rock", "crack", 1.0f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 16);
        d.plain("lsd_solution", 16);
        d.drug("lsd_blotter", "lsd", 1.0f, ItemCatalog.Form.PILL, ItemCatalog.Rem.NONE, 16);
        d.plain("jagube_vine", 64);
        d.plain("chacrona_leaf", 64);
        d.drug("ayahuasca_brew", "ayahuasca", 1.0f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 8);
        d.plain("dmt_crystals", 16);
        d.drug("dmt_dose", "dmt", 1.0f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 8);
        d.plain("solvent", 16);
        d.plain("chem_base", 32);
        d.plain("catalyst", 16);
        d.plain("raw_crystal", 32);
        d.drug("meth_shard", "meth", 1.0f, ItemCatalog.Form.SMOKE, ItemCatalog.Rem.NONE, 16);
        d.drug("mdma_pill", "mdma", 1.0f, ItemCatalog.Form.PILL, ItemCatalog.Rem.NONE, 16);
        d.drug("ketamine_vial", "ketamine", 1.0f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 16);
        d.drug("beer", "alcohol", 0.5f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 16);
        d.drug("wine", "alcohol", 0.8f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 16);
        d.drug("spirit", "alcohol", 1.4f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 16);
        d.drug("nebulon_tab", "nebulon", 1.0f, ItemCatalog.Form.PILL, ItemCatalog.Rem.NONE, 16);
        d.drug("zyrax_vial", "zyrax", 1.0f, ItemCatalog.Form.DRINK, ItemCatalog.Rem.BOTTLE, 16);
        d.clipboard("medical_clipboard");
        d.antidote("emergency_kit", 8);
        d.detox("detox_tonic", 8);
        d.module("speed_module_1", 1);
        d.module("speed_module_2", 2);
        d.module("speed_module_3", 3);
        d.plain("maintenance_kit", 16);
        d.fertilizer("organic_fertilizer");
        d.encyclopedia("encyclopedia");
        d.doc("doc_field_guide_basic");
        d.doc("doc_mycology_notes");
        d.doc("doc_jungle_expedition");
        d.doc("doc_drying_manual");
        d.doc("doc_grinder_manual");
        d.doc("doc_table_manual");
        d.doc("doc_chem_safety");
        d.doc("doc_opioid_research");
        d.doc("doc_stimulant_research");
        d.doc("doc_psychedelic_research");
        d.doc("doc_synthetics_lab");
        d.doc("doc_brewing_notes");
        d.doc("doc_greenhouse_blueprint");
        d.doc("doc_maintenance_log");
        d.doc("doc_quality_study");
        d.doc("doc_addiction_study");
    }
}
