"""Tabelas de loot por raridade (fonte unica para injecao em baus vanilla e baus das estruturas).
Formato: (item, peso, min, max). A=comum  B=incomum  C=raro  D=muito raro."""
TIERS = {
    "A": [("cannabis_seeds", 10, 1, 3), ("tobacco_seeds", 8, 1, 3), ("poppy_seeds", 8, 1, 3), ("psilocybin_spores", 6, 1, 2),
          ("rolling_paper", 8, 1, 4), ("organic_fertilizer", 5, 1, 2), ("syringe", 2, 1, 2),
          ("doc_field_guide_basic", 3, 1, 1), ("doc_mycology_notes", 2, 1, 1)],
    "B": [("coca_seeds", 4, 1, 2), ("jagube_seeds", 3, 1, 2), ("chacrona_seeds", 4, 1, 2), ("maintenance_kit", 3, 1, 1),
          ("speed_module_1", 2, 1, 1), ("trellis", 3, 1, 3), ("organic_substrate", 3, 1, 3), ("solvent", 2, 1, 1),
          ("doc_drying_manual", 2, 1, 1), ("doc_grinder_manual", 2, 1, 1), ("doc_table_manual", 2, 1, 1),
          ("doc_jungle_expedition", 2, 1, 1), ("doc_greenhouse_blueprint", 2, 1, 1), ("doc_brewing_notes", 2, 1, 1)],
    "C": [("solvent", 4, 1, 2), ("chem_base", 3, 1, 2), ("catalyst", 2, 1, 1), ("raw_crystal", 3, 1, 3), ("coca_paste", 1, 1, 1),
          ("morphine_base", 1, 1, 1), ("lsd_solution", 1, 1, 1), ("dmt_crystals", 1, 1, 1), ("speed_module_2", 1, 1, 1),
          ("greenhouse_regulator", 1, 1, 1), ("doc_chem_safety", 2, 1, 1), ("doc_opioid_research", 1, 1, 1),
          ("doc_stimulant_research", 1, 1, 1), ("doc_psychedelic_research", 1, 1, 1), ("doc_synthetics_lab", 1, 1, 1),
          ("doc_maintenance_log", 2, 1, 1)],
    "D": [("cocaine_powder", 1, 1, 1), ("crack_rock", 1, 1, 1), ("meth_shard", 1, 1, 1), ("heroin_dose", 1, 1, 1),
          ("lsd_blotter", 1, 1, 2), ("mdma_pill", 1, 1, 2), ("ketamine_vial", 1, 1, 1), ("dmt_dose", 1, 1, 1), ("nebulon_tab", 1, 1, 1),
          ("zyrax_vial", 1, 1, 1), ("chemistry_bench", 1, 1, 1), ("processing_furnace", 1, 1, 1), ("speed_module_3", 1, 1, 1),
          ("doc_quality_study", 1, 1, 1), ("doc_addiction_study", 1, 1, 1)],
}
# baus vanilla -> [(tier, chance_do_pool)]
INJECT = {
    "chests/simple_dungeon": [("A", .30), ("B", .10), ("C", .03)],
    "chests/abandoned_mineshaft": [("A", .20), ("B", .08), ("C", .03), ("D", .005)],
    "chests/desert_pyramid": [("A", .25), ("B", .15), ("C", .06), ("D", .01)],
    "chests/jungle_temple": [("A", .25), ("B", .18), ("C", .06), ("D", .012)],
    "chests/igloo_chest": [("A", .20), ("B", .10)],
    "chests/pillager_outpost": [("A", .25), ("B", .12), ("C", .04)],
    "chests/shipwreck_supply": [("A", .25)],
    "chests/shipwreck_treasure": [("B", .20), ("C", .08), ("D", .01)],
    "chests/underwater_ruin_small": [("A", .15)],
    "chests/underwater_ruin_big": [("B", .15), ("C", .05)],
    "chests/buried_treasure": [("B", .30), ("C", .15), ("D", .03)],
    "chests/woodland_mansion": [("A", .30), ("B", .25), ("C", .12), ("D", .03)],
    "chests/stronghold_corridor": [("A", .15), ("B", .12)],
    "chests/stronghold_crossing": [("A", .15), ("B", .12)],
    "chests/stronghold_library": [("B", .30), ("C", .10), ("D", .02)],
    "chests/ruined_portal": [("A", .15), ("B", .10), ("C", .05)],
    "chests/nether_bridge": [("C", .08), ("D", .01)],
    "chests/bastion_other": [("C", .12), ("D", .02)],
    "chests/bastion_treasure": [("C", .20), ("D", .06)],
    "chests/bastion_bridge": [("C", .05)],
    "chests/bastion_hoglin_stable": [("C", .05)],
    "chests/ancient_city": [("B", .30), ("C", .20), ("D", .06)],
    "chests/ancient_city_ice_box": [("C", .10)],
    "chests/end_city_treasure": [("C", .20), ("D", .08)],
}
for v in ("plains", "taiga", "savanna", "desert", "snowy"): INJECT[f"chests/village/village_{v}_house"] = [("A", .22), ("B", .04)]
for v in ("toolsmith", "cartographer", "temple", "shepherd", "butcher", "fisher", "fletcher", "mason", "tannery", "weaponsmith", "armorer"):
    INJECT[f"chests/village/village_{v}"] = [("A", .12)]
