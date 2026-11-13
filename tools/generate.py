#!/usr/bin/env python3
"""Gera texturas, modelos, blockstates, lang, receitas, loot tables, tags e as classes Java
ItemCatalog / MachineRecipes a partir de UMA unica fonte de dados (este arquivo)."""
import json, os, sys
sys.path.insert(0, os.path.dirname(__file__))
import art
from art import hx

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RES = os.path.join(ROOT, "src/main/resources")
JAVA = os.path.join(ROOT, "src/main/java/dev/drogas")
A = os.path.join(RES, "assets/drogas")
D = os.path.join(RES, "data/drogas")
MC = os.path.join(RES, "data/minecraft")
MOD = "drogas"

def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f: json.dump(obj, f, ensure_ascii=False, indent=2)
def wt(path, txt):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f: f.write(txt)
def wp(path, im):
    os.makedirs(os.path.dirname(path), exist_ok=True); im.save(path)

# ------------------------------------------------------------------ substancias (somente apresentacao)
SUBSTANCES = [
    ("cannabis", "Maconha", "Cannabis", "6fbf4a"), ("cocaine", "Cocaína", "Cocaine", "dfeaff"),
    ("crack", "Crack", "Crack", "b04a3a"), ("lsd", "LSD", "LSD", "c06bff"),
    ("psilocybin", "Cogumelos Alucinógenos", "Magic Mushrooms", "e8a23a"), ("opium", "Ópio", "Opium", "b0793a"),
    ("morphine", "Morfina", "Morphine", "7aa7c7"), ("heroin", "Heroína", "Heroin", "8a5a3a"),
    ("meth", "Metanfetamina", "Methamphetamine", "7fe3e0"), ("mdma", "Ecstasy (MDMA)", "Ecstasy (MDMA)", "ff5fb4"),
    ("ketamine", "Cetamina", "Ketamine", "8a6bd6"), ("dmt", "DMT", "DMT", "4fe0a0"),
    ("ayahuasca", "Ayahuasca", "Ayahuasca", "6b8f2e"), ("tobacco", "Tabaco", "Tobacco", "a08a6a"),
    ("alcohol", "Álcool", "Alcohol", "d99a4a"), ("nebulon", "Nebulon (sintética)", "Nebulon (synthetic)", "4aa8ff"),
    ("zyrax", "Zyrax (sintética)", "Zyrax (synthetic)", "d6344a"),
]
SPECIAL_EFFECTS = [("withdrawal", "Abstinência", "Withdrawal", "9a9a6a", "syringe"),
                   ("overdose", "Overdose", "Overdose", "c01818", "emergency_kit"),
                   ("bad_trip", "Bad Trip", "Bad Trip", "3a2a5a", "lsd_blotter")]
# item usado como glifo do icone de efeito
EFFECT_GLYPH = {"cannabis": "joint", "cocaine": "cocaine_powder", "crack": "crack_rock", "lsd": "lsd_blotter",
                "psilocybin": "magic_mushroom", "opium": "opium_gum", "morphine": "morphine_vial", "heroin": "heroin_dose",
                "meth": "meth_shard", "mdma": "mdma_pill", "ketamine": "ketamine_vial", "dmt": "dmt_dose",
                "ayahuasca": "ayahuasca_brew", "tobacco": "cigarette", "alcohol": "beer", "nebulon": "nebulon_tab", "zyrax": "zyrax_vial"}

# ------------------------------------------------------------------ plantas
# id, pt, en, kind, c1, c2, flower
CROPS = [
    ("cannabis", "Plantação de Maconha", "Cannabis Plant", "cannabis", (70, 150, 52, 255), (40, 100, 38, 255), (120, 190, 70, 255)),
    ("poppy", "Plantação de Papoula", "Poppy Plant", "poppy", (80, 140, 110, 255), (50, 100, 80, 255), (210, 60, 120, 255)),
    ("tobacco", "Plantação de Tabaco", "Tobacco Plant", "tobacco", (110, 150, 50, 255), (70, 100, 40, 255), (240, 150, 170, 255)),
    ("psilocybin", "Cultivo de Cogumelos", "Mushroom Culture", "mushroom", (196, 140, 60, 255), (120, 80, 40, 255), None),
    ("coca", "Plantação de Coca", "Coca Plant", "coca", (60, 140, 60, 255), (40, 100, 45, 255), (240, 240, 200, 255)),
    ("jagube", "Cipó Jagube", "Jagube Vine", "jagube", (80, 120, 50, 255), (60, 90, 40, 255), (150, 100, 170, 255)),
    ("chacrona", "Chacrona", "Chacrona Bush", "chacrona", (50, 150, 70, 255), (30, 110, 50, 255), (220, 230, 150, 255)),
]
CROP_PRODUCE = {"cannabis": ("cannabis_bud", 1, 3), "poppy": ("poppy_pod", 1, 3), "tobacco": ("tobacco_leaf", 2, 4),
                "psilocybin": ("magic_mushroom", 1, 3), "coca": ("coca_leaf", 2, 4), "jagube": ("jagube_vine", 1, 2),
                "chacrona": ("chacrona_leaf", 2, 3)}
CROP_SEED = {"cannabis": "cannabis_seeds", "poppy": "poppy_seeds", "tobacco": "tobacco_seeds", "psilocybin": "psilocybin_spores",
             "coca": "coca_seeds", "jagube": "jagube_seeds", "chacrona": "chacrona_seeds"}

# ------------------------------------------------------------------ itens
# (id, pt, en, imagem, java)
def P(n=64): return ("plain", n)
def SEED(crop): return ("seeds", crop + "_crop")
def DRUG(sub, dose, drink=False, smoke=False, rem="NONE", stack=16): return ("drug", sub, dose, drink, smoke, rem, stack)
ITEMS = [
    ("cannabis_seeds", "Sementes de Maconha", "Cannabis Seeds", art.seeds((90, 170, 60, 255)), SEED("cannabis")),
    ("poppy_seeds", "Sementes de Papoula", "Poppy Seeds", art.seeds((150, 70, 160, 255)), SEED("poppy")),
    ("tobacco_seeds", "Sementes de Tabaco", "Tobacco Seeds", art.seeds((130, 110, 50, 255)), SEED("tobacco")),
    ("psilocybin_spores", "Esporos de Cogumelo", "Mushroom Spores", art.seeds((190, 190, 225, 255), (110, 110, 130, 255)), SEED("psilocybin")),
    ("coca_seeds", "Sementes de Coca", "Coca Seeds", art.seeds((60, 130, 60, 255)), SEED("coca")),
    ("jagube_seeds", "Sementes de Jagube", "Jagube Seeds", art.seeds((110, 150, 60, 255)), SEED("jagube")),
    ("chacrona_seeds", "Sementes de Chacrona", "Chacrona Seeds", art.seeds((80, 170, 100, 255)), SEED("chacrona")),

    ("cannabis_bud", "Flor de Maconha", "Cannabis Bud", art.bud(), P()),
    ("dried_cannabis", "Maconha Seca", "Dried Cannabis", art.bud((120, 128, 60, 255), (150, 110, 50, 255)), P()),
    ("ground_cannabis", "Maconha Triturada", "Ground Cannabis", art.pile((96, 130, 58, 255), 3), P()),
    ("rolling_paper", "Seda de Enrolar", "Rolling Paper", art.paper_stack(), P()),
    ("joint", "Baseado", "Joint", art.joint(), DRUG("cannabis", 1.0, smoke=True, stack=16)),
    ("cannabis_brownie", "Brownie Especial", "Special Brownie", art.brownie(), DRUG("cannabis", 1.5, stack=16)),

    ("poppy_pod", "Cápsula de Papoula", "Poppy Pod", art.pod(), P()),
    ("opium_gum", "Goma de Ópio", "Opium Gum", art.gum(), DRUG("opium", 1.0, smoke=True)),
    ("morphine_base", "Base de Morfina", "Morphine Base", art.bag((236, 226, 206, 255)), P()),
    ("morphine_vial", "Frasco de Morfina", "Morphine Vial", art.vial((120, 170, 230, 255), .7), DRUG("morphine", 1.0, drink=True, rem="BOTTLE")),
    ("heroin_dose", "Dose de Heroína", "Heroin Dose", art.syringe((190, 140, 70, 255)), DRUG("heroin", 1.0, rem="SYRINGE")),
    ("syringe", "Seringa", "Syringe", art.syringe(None, empty=True), P(16)),

    ("tobacco_leaf", "Folha de Tabaco", "Tobacco Leaf", art.broad_leaf((110, 130, 50, 255), (70, 90, 36, 255)), P()),
    ("dried_tobacco", "Tabaco Curado", "Cured Tobacco", art.broad_leaf((150, 104, 52, 255), (100, 66, 34, 255), (190, 150, 90, 255)), P()),
    ("ground_tobacco", "Tabaco Triturado", "Ground Tobacco", art.pile((120, 80, 40, 255), 5), P()),
    ("cigarette", "Cigarro", "Cigarette", art.cigarette(), DRUG("tobacco", 0.7, smoke=True, stack=20)),
    ("cigar", "Charuto", "Cigar", art.cigar(), DRUG("tobacco", 1.6, smoke=True, stack=8)),

    ("magic_mushroom", "Cogumelo Alucinógeno", "Magic Mushroom", art.mushroom((196, 140, 60, 255)), DRUG("psilocybin", 0.8)),
    ("dried_magic_mushroom", "Cogumelo Seco", "Dried Magic Mushroom", art.mushroom((120, 84, 50, 255), (190, 170, 130, 255), (160, 140, 110, 255)), DRUG("psilocybin", 1.1)),

    ("coca_leaf", "Folha de Coca", "Coca Leaf", art.small_leaf((70, 150, 60, 255), (40, 100, 40, 255)), DRUG("cocaine", 0.2, stack=64)),
    ("coca_paste", "Pasta de Coca", "Coca Paste", art.gum((200, 180, 130, 255)), P()),
    ("cocaine_powder", "Pó de Cocaína", "Cocaine Powder", art.bag((250, 250, 252, 255)), DRUG("cocaine", 1.0)),
    ("crack_rock", "Pedra de Crack", "Crack Rock", art.crystal((214, 190, 150, 255)), DRUG("crack", 1.0, smoke=True)),

    ("lsd_solution", "Solução Lisérgica", "Lysergic Solution", art.vial((190, 90, 255, 255), .6), P(16)),
    ("lsd_blotter", "Selo de LSD", "LSD Blotter", art.blotter((170, 60, 255, 255)), DRUG("lsd", 1.0)),

    ("jagube_vine", "Cipó Jagube", "Jagube Vine", art.vine_item((90, 130, 50, 255), (60, 90, 36, 255)), P()),
    ("chacrona_leaf", "Folha de Chacrona", "Chacrona Leaf", art.broad_leaf((50, 150, 70, 255), (30, 110, 50, 255)), P()),
    ("ayahuasca_brew", "Chá de Ayahuasca", "Ayahuasca Brew", art.vial((96, 120, 40, 255), .85, cork=(110, 80, 50, 255)), DRUG("ayahuasca", 1.0, drink=True, rem="BOTTLE", stack=8)),
    ("dmt_crystals", "Cristais de DMT", "DMT Crystals", art.crystal((90, 235, 170, 255)), P(16)),
    ("dmt_dose", "Dose de DMT", "DMT Dose", art.bag((90, 235, 170, 255)), DRUG("dmt", 1.0, smoke=True, stack=8)),

    ("solvent", "Solvente", "Solvent", art.vial((230, 210, 80, 255), .8), P(16)),
    ("chem_base", "Base Química", "Chemical Base", art.bag((245, 245, 245, 255)), P(32)),
    ("catalyst", "Catalisador Instável", "Unstable Catalyst", art.vial((220, 60, 60, 255), .5), P(16)),
    ("raw_crystal", "Cristal Bruto", "Raw Crystal", art.crystal((180, 190, 205, 255)), P(32)),
    ("meth_shard", "Cristal de Metanfetamina", "Methamphetamine Shard", art.crystal((120, 225, 235, 255)), DRUG("meth", 1.0, smoke=True)),
    ("mdma_pill", "Ecstasy", "Ecstasy Pill", art.pill((255, 95, 175, 255), mark=(255, 255, 255, 255)), DRUG("mdma", 1.0)),
    ("ketamine_vial", "Frasco de Cetamina", "Ketamine Vial", art.vial((220, 220, 240, 255), .7, label=(120, 100, 200, 255)), DRUG("ketamine", 1.0, drink=True, rem="BOTTLE")),

    ("beer", "Cerveja", "Beer", art.bottle((140, 84, 30, 255)), DRUG("alcohol", 0.5, drink=True, rem="BOTTLE")),
    ("wine", "Vinho", "Wine", art.wine(), DRUG("alcohol", 0.8, drink=True, rem="BOTTLE")),
    ("spirit", "Cachaça", "Spirit (Cachaça)", art.bottle((205, 225, 225, 255), (230, 230, 200, 255), (200, 60, 60, 255)), DRUG("alcohol", 1.4, drink=True, rem="BOTTLE")),

    ("nebulon_tab", "Nebulon", "Nebulon Tab", art.pill((70, 170, 255, 255), mark=(255, 255, 255, 255)), DRUG("nebulon", 1.0)),
    ("zyrax_vial", "Zyrax", "Zyrax Vial", art.vial((220, 40, 70, 255), .8, cork=(70, 70, 76, 255)), DRUG("zyrax", 1.0, drink=True, rem="BOTTLE")),

    ("medical_clipboard", "Prancheta Médica", "Medical Clipboard", art.clipboard(), ("clipboard",)),
    ("emergency_kit", "Kit de Emergência", "Emergency Kit", art.kit(), ("antidote", 8)),
    ("detox_tonic", "Tônico Desintoxicante", "Detox Tonic", art.vial((70, 200, 170, 255), .75, label=(236, 236, 226, 255)), ("detox", 8)),
]

# ------------------------------------------------------------------ maquinas
MACHINES = [  # id, pt, en, tipo Java, texturas top/side/front
    ("processing_table", "Mesa de Processamento", "Processing Table", "TABLE", art.table_top, art.table_side, art.table_front),
    ("chemistry_bench", "Bancada Química", "Chemistry Bench", "BENCH", art.bench_top, art.bench_side, art.bench_front),
    ("dryer", "Secador", "Dryer", "DRYER", art.dryer_top, art.dryer_side, art.dryer_front),
    ("grinder", "Triturador", "Grinder", "GRINDER", art.grinder_top, art.grinder_side, art.grinder_front),
    ("processing_furnace", "Forno de Processamento", "Processing Furnace", "FURNACE", art.furn_top, art.furn_side, art.furn_front),
]
CRATE = ("storage_crate", "Recipiente de Armazenamento", "Storage Crate")

# ------------------------------------------------------------------ receitas de maquina
def I(tok, n=1): return (tok, n)
M = "minecraft:"; Dm = "drogas:"
MACHINE_RECIPES = [
    ("DRYER", 600, [I(Dm+"cannabis_bud")], (Dm+"dried_cannabis", 1)),
    ("DRYER", 600, [I(Dm+"tobacco_leaf")], (Dm+"dried_tobacco", 1)),
    ("DRYER", 600, [I(Dm+"magic_mushroom")], (Dm+"dried_magic_mushroom", 1)),
    ("GRINDER", 100, [I(Dm+"dried_cannabis")], (Dm+"ground_cannabis", 2)),
    ("GRINDER", 100, [I(Dm+"dried_tobacco")], (Dm+"ground_tobacco", 2)),
    ("GRINDER", 100, [I(M+"quartz")], (Dm+"raw_crystal", 2)),
    ("TABLE", 60, [I(Dm+"ground_cannabis"), I(Dm+"rolling_paper")], (Dm+"joint", 1)),
    ("TABLE", 60, [I(Dm+"ground_tobacco"), I(Dm+"rolling_paper")], (Dm+"cigarette", 2)),
    ("TABLE", 60, [I(Dm+"dried_tobacco", 2), I(Dm+"tobacco_leaf")], (Dm+"cigar", 1)),
    ("TABLE", 80, [I(Dm+"ground_cannabis", 2), I(M+"cocoa_beans"), I(M+"wheat")], (Dm+"cannabis_brownie", 2)),
    ("TABLE", 80, [I(Dm+"lsd_solution"), I(M+"paper")], (Dm+"lsd_blotter", 4)),
    ("TABLE", 80, [I(Dm+"chem_base"), I(Dm+"raw_crystal"), I(M+"sugar")], (Dm+"mdma_pill", 3)),
    ("TABLE", 80, [I(Dm+"chem_base"), I(Dm+"raw_crystal"), I(M+"chorus_fruit")], (Dm+"nebulon_tab", 2)),
    ("TABLE", 60, [I(Dm+"dmt_crystals"), I(M+"paper")], (Dm+"dmt_dose", 2)),
    ("BENCH", 100, [I(M+"coal"), I(M+"glass_bottle"), I(M+"sugar")], (Dm+"solvent", 1)),
    ("BENCH", 200, [I(M+"redstone", 2), I(Dm+"solvent"), I(M+"bone_meal")], (Dm+"chem_base", 2)),
    ("BENCH", 300, [I(M+"glowstone_dust", 2), I(M+"gunpowder"), I(Dm+"solvent")], (Dm+"catalyst", 1)),
    ("BENCH", 200, [I(Dm+"poppy_pod", 3), I(Dm+"solvent")], (Dm+"opium_gum", 1)),
    ("BENCH", 300, [I(Dm+"opium_gum", 2), I(Dm+"solvent")], (Dm+"morphine_base", 1)),
    ("BENCH", 200, [I(Dm+"morphine_base"), I(M+"glass_bottle"), I(Dm+"solvent")], (Dm+"morphine_vial", 2)),
    ("BENCH", 400, [I(Dm+"morphine_base"), I(Dm+"catalyst"), I(Dm+"syringe")], (Dm+"heroin_dose", 1)),
    ("BENCH", 300, [I(Dm+"coca_leaf", 6), I(Dm+"solvent")], (Dm+"coca_paste", 1)),
    ("BENCH", 300, [I(Dm+"coca_paste"), I(Dm+"chem_base")], (Dm+"cocaine_powder", 2)),
    ("BENCH", 300, [I(Dm+"cocaine_powder"), I(Dm+"catalyst")], (Dm+"crack_rock", 2)),
    ("BENCH", 400, [I(Dm+"chem_base"), I(M+"amethyst_shard"), I(M+"glass_bottle")], (Dm+"lsd_solution", 1)),
    ("BENCH", 400, [I(Dm+"chacrona_leaf", 4), I(Dm+"solvent")], (Dm+"dmt_crystals", 1)),
    ("BENCH", 500, [I(Dm+"raw_crystal", 2), I(Dm+"catalyst"), I(Dm+"chem_base")], (Dm+"meth_shard", 2)),
    ("BENCH", 300, [I(Dm+"chem_base"), I(Dm+"raw_crystal"), I(M+"glass_bottle")], (Dm+"ketamine_vial", 1)),
    ("BENCH", 600, [I(Dm+"raw_crystal", 2), I(Dm+"catalyst"), I(M+"ender_pearl")], (Dm+"zyrax_vial", 1)),
    ("FURNACE", 1200, [I(Dm+"jagube_vine", 2), I(Dm+"chacrona_leaf", 2), I(M+"glass_bottle")], (Dm+"ayahuasca_brew", 1)),
    ("FURNACE", 1200, [I(M+"wheat", 3), I(M+"glass_bottle")], (Dm+"beer", 1)),
    ("FURNACE", 1200, [I(M+"sweet_berries", 4), I(M+"glass_bottle")], (Dm+"wine", 1)),
    ("FURNACE", 1600, [I(M+"sugar_cane", 4), I(M+"glass_bottle")], (Dm+"spirit", 1)),
]
# ------------------------------------------------------------------ receitas vanilla (crafting)
def SL(result, count, ingredients): return (result, count, ingredients)
CRAFTING = [
    SL("processing_table", 1, [M+"crafting_table", M+"iron_ingot", M+"iron_ingot"]),
    SL("chemistry_bench", 1, [M+"brewing_stand", M+"cauldron", M+"iron_ingot", M+"iron_ingot"]),
    SL("dryer", 1, ["#minecraft:planks"]*3 + [M+"string"]*3),
    SL("grinder", 1, [M+"grindstone", M+"hopper", M+"iron_ingot"]),
    SL("processing_furnace", 1, [M+"furnace", M+"iron_ingot", M+"iron_ingot", M+"blaze_powder"]),
    SL("storage_crate", 1, [M+"chest", M+"iron_nugget", M+"iron_nugget"]),
    SL("cannabis_seeds", 2, [M+"wheat_seeds", M+"fern", M+"bone_meal"]),
    SL("poppy_seeds", 2, [M+"poppy", M+"wheat_seeds"]),
    SL("tobacco_seeds", 2, [M+"wheat_seeds", M+"fern", M+"dried_kelp"]),
    SL("psilocybin_spores", 2, [M+"brown_mushroom", M+"red_mushroom", M+"bone_meal"]),
    SL("coca_seeds", 2, [M+"cocoa_beans", M+"cocoa_beans", M+"wheat_seeds", M+"glow_berries"]),
    SL("jagube_seeds", 2, [M+"vine", M+"vine", M+"cocoa_beans", M+"bone_meal"]),
    SL("chacrona_seeds", 2, [M+"fern", M+"fern", M+"sweet_berries", M+"bone_meal"]),
    SL("rolling_paper", 3, [M+"paper", M+"sugar"]),
    SL("syringe", 2, [M+"glass_bottle", M+"iron_nugget", M+"iron_nugget"]),
    SL("medical_clipboard", 1, [M+"paper", M+"paper", M+"stick", M+"iron_nugget"]),
    SL("emergency_kit", 1, [M+"golden_carrot", M+"redstone", M+"bone_meal", M+"bone_meal", M+"paper"]),
    SL("detox_tonic", 1, [M+"honey_bottle", M+"sweet_berries", M+"sweet_berries", M+"dried_kelp"]),
]

# ====================================================================== emissao
def item_images():
    return {i[0]: i[3] for i in ITEMS}

def emit_textures():
    imgs = item_images()
    for iid, _, _, im, _ in ITEMS: wp(f"{A}/textures/item/{iid}.png", im)
    for (cid, _, _, kind, c1, c2, fl) in CROPS:
        for s in range(4): wp(f"{A}/textures/block/{cid}_stage{s}.png", art.crop(kind, s, c1, c2, fl))
    for (mid, _, _, _, top, side, front) in MACHINES:
        wp(f"{A}/textures/block/{mid}_top.png", top()); wp(f"{A}/textures/block/{mid}_side.png", side())
        wp(f"{A}/textures/block/{mid}_front.png", front(False)); wp(f"{A}/textures/block/{mid}_front_on.png", front(True))
    wp(f"{A}/textures/block/storage_crate_top.png", art.crate_top()); wp(f"{A}/textures/block/storage_crate_side.png", art.crate_side())
    for (sid, _, _, col) in SUBSTANCES:
        wp(f"{A}/textures/mob_effect/{sid}.png", art.effect_icon(imgs[EFFECT_GLYPH[sid]], hx(col)))
    for (eid, _, _, col, glyph) in SPECIAL_EFFECTS:
        wp(f"{A}/textures/mob_effect/{eid}.png", art.effect_icon(imgs[glyph], hx(col)))
    art.mod_icon().save(f"{A}/icon.png")

def emit_models():
    for iid, *_ in ITEMS:
        wj(f"{A}/models/item/{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{iid}"}})
        wj(f"{A}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{iid}"}})
    stage_of = [0, 0, 1, 1, 2, 2, 2, 3]
    for (cid, *_rest) in CROPS:
        for s in range(4):
            wj(f"{A}/models/block/{cid}_crop_stage{s}.json", {"parent": "minecraft:block/crop", "textures": {"crop": f"{MOD}:block/{cid}_stage{s}"}})
        wj(f"{A}/blockstates/{cid}_crop.json", {"variants": {f"age={a}": {"model": f"{MOD}:block/{cid}_crop_stage{stage_of[a]}"} for a in range(8)}})
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    for (mid, *_r) in MACHINES:
        for on in (False, True):
            wj(f"{A}/models/block/{mid}{'_on' if on else ''}.json", {"parent": "minecraft:block/orientable", "textures": {
                "top": f"{MOD}:block/{mid}_top", "side": f"{MOD}:block/{mid}_side", "front": f"{MOD}:block/{mid}_front{'_on' if on else ''}"}})
        variants = {}
        for f, y in rot.items():
            for on in (False, True):
                v = {"model": f"{MOD}:block/{mid}{'_on' if on else ''}"}
                if y: v["y"] = y
                variants[f"facing={f},lit={'true' if on else 'false'}"] = v
        wj(f"{A}/blockstates/{mid}.json", {"variants": variants})
        wj(f"{A}/items/{mid}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{mid}"}})
    cid = CRATE[0]
    wj(f"{A}/models/block/{cid}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": f"{MOD}:block/{cid}_top", "bottom": f"{MOD}:block/{cid}_top", "side": f"{MOD}:block/{cid}_side"}})
    wj(f"{A}/blockstates/{cid}.json", {"variants": {"": {"model": f"{MOD}:block/{cid}"}}})
    wj(f"{A}/items/{cid}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{cid}"}})

def emit_lang():
    pt = {"itemGroup.drogas.main": "Drogas Mod"}; en = {"itemGroup.drogas.main": "Drugs Mod"}
    for iid, p, e, *_ in ITEMS: pt[f"item.{MOD}.{iid}"] = p; en[f"item.{MOD}.{iid}"] = e
    for (cid, p, e, *_r) in CROPS: pt[f"block.{MOD}.{cid}_crop"] = p; en[f"block.{MOD}.{cid}_crop"] = e
    for (mid, p, e, *_r) in MACHINES: pt[f"block.{MOD}.{mid}"] = p; en[f"block.{MOD}.{mid}"] = e
    pt[f"block.{MOD}.{CRATE[0]}"] = CRATE[1]; en[f"block.{MOD}.{CRATE[0]}"] = CRATE[2]
    for (sid, p, e, _c) in SUBSTANCES:
        pt[f"substance.{MOD}.{sid}"] = p; en[f"substance.{MOD}.{sid}"] = e
        pt[f"effect.{MOD}.{sid}"] = f"{p} (efeito)"; en[f"effect.{MOD}.{sid}"] = f"{e} (effect)"
    for (eid, p, e, *_r) in SPECIAL_EFFECTS: pt[f"effect.{MOD}.{eid}"] = p; en[f"effect.{MOD}.{eid}"] = e
    extra = {
        "screen.drogas.status.title": ("Prancheta Médica", "Medical Clipboard"),
        "gui.drogas.col.substance": ("Substância", "Substance"), "gui.drogas.col.tolerance": ("Tolerância", "Tolerance"),
        "gui.drogas.col.dependence": ("Dependência", "Dependence"), "gui.drogas.col.load": ("Carga", "Load"),
        "gui.drogas.col.state": ("Estado", "State"), "gui.drogas.close": ("Fechar", "Close"),
        "gui.drogas.state.clean": ("Limpo", "Clean"), "gui.drogas.state.active": ("Ativo", "Active"),
        "gui.drogas.state.withdrawal.1": ("Abstinência leve", "Mild withdrawal"),
        "gui.drogas.state.withdrawal.2": ("Abstinência moderada", "Moderate withdrawal"),
        "gui.drogas.state.withdrawal.3": ("Abstinência grave", "Severe withdrawal"),
        "gui.drogas.level.1": ("leve", "mild"), "gui.drogas.level.2": ("moderada", "moderate"), "gui.drogas.level.3": ("grave", "severe"),
        "gui.drogas.hud.withdrawal": ("Abstinência: %s (%s)", "Withdrawal: %s (%s)"),
        "gui.drogas.hud.danger": ("Carga perigosa: %s", "Dangerous load: %s"),
        "gui.drogas.hud.overdose": ("OVERDOSE! Use um Kit de Emergência", "OVERDOSE! Use an Emergency Kit"),
        "gui.drogas.tol": ("Tol", "Tol"), "gui.drogas.dep": ("Dep", "Dep"), "gui.drogas.int": ("Int", "Pot"),
        "gui.drogas.scroll": ("Role para ver mais", "Scroll for more"),
        "message.drogas.overdose": ("Você está sofrendo uma overdose de %s!", "You are overdosing on %s!"),
        "message.drogas.bad_trip": ("Você entrou numa bad trip (%s)...", "You are having a bad trip (%s)..."),
        "message.drogas.craving": ("Fissura: o corpo pede %s...", "Craving: your body wants %s..."),
        "message.drogas.relief": ("O alívio chega, mas a dependência continua.", "Relief arrives, but the dependence remains."),
        "message.drogas.tolerance": ("A tolerância reduziu o efeito.", "Tolerance dulled the effect."),
        "message.drogas.kit_used": ("O kit estabilizou você.", "The kit stabilized you."),
        "message.drogas.detox_used": ("Seu corpo começa a se recuperar.", "Your body starts to recover."),
        "command.drogas.status.header": ("=== Estado das substâncias ===", "=== Substance status ==="),
        "command.drogas.status.line": ("%s: Tol %s%% | Dep %s%% | Carga %s%% | %s", "%s: Tol %s%% | Dep %s%% | Load %s%% | %s"),
        "command.drogas.status.empty": ("Nenhuma substância registrada. Você está limpo.", "No substances recorded. You are clean."),
        "command.drogas.debug_off": ("Comandos de depuração desativados (config/drogas.json: debugCommands).", "Debug commands are disabled (config/drogas.json: debugCommands)."),
        "command.drogas.reset": ("Estado de tolerância/dependência reiniciado.", "Tolerance/dependence state reset."),
        "command.drogas.used": ("Dose aplicada: %s", "Dose applied: %s"),
        "command.drogas.unknown": ("Substância desconhecida: %s", "Unknown substance: %s"),
        "command.drogas.help": ("/drogas status | /drogas ajuda (depuração: /drogas usar <substância> | /drogas reset)",
                                "/drogas status | /drogas help (debug: /drogas use <substance> | /drogas reset)"),
    }
    for k, (p, e) in extra.items(): pt[k] = p; en[k] = e
    wj(f"{A}/lang/pt_br.json", pt); wj(f"{A}/lang/en_us.json", en)

def emit_data():
    # receitas de crafting vanilla
    for (res, n, ing) in CRAFTING:
        ingredients = [x if ":" in x or x.startswith("#") else f"{MOD}:{x}" for x in ing]
        wj(f"{D}/recipe/{res}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ingredients,
                                      "result": {"id": f"{MOD}:{res}", "count": n}})
    # loot tables
    surv = [{"condition": "minecraft:survives_explosion"}]
    for (cid, *_r) in CROPS:
        prod, lo, hi = CROP_PRODUCE[cid]; seed = CROP_SEED[cid]
        mature = {"condition": "minecraft:block_state_property", "block": f"{MOD}:{cid}_crop", "properties": {"age": "7"}}
        wj(f"{D}/loot_table/blocks/{cid}_crop.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "conditions": [mature], "entries": [{"type": "minecraft:item", "name": f"{MOD}:{prod}", "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}}]}]},
            {"rolls": 1, "conditions": surv, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{seed}", "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 2.0}}]}]}]})
    for bid in [m[0] for m in MACHINES] + [CRATE[0]]:
        wj(f"{D}/loot_table/blocks/{bid}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "conditions": surv, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{bid}"}]}]})
    # tags
    wj(f"{MC}/tags/block/mineable/pickaxe.json", {"replace": False, "values": [f"{MOD}:{m}" for m in ("chemistry_bench", "grinder", "processing_furnace")]})
    wj(f"{MC}/tags/block/mineable/axe.json", {"replace": False, "values": [f"{MOD}:{m}" for m in ("processing_table", "dryer", "storage_crate")]})
    crops = [f"{MOD}:{c[0]}_crop" for c in CROPS]
    wj(f"{MC}/tags/block/crops.json", {"replace": False, "values": crops})
    wj(f"{MC}/tags/block/maintains_farmland.json", {"replace": False, "values": crops})
    wj(f"{D}/tags/block/drug_crops.json", {"replace": False, "values": crops})
    drugs = [i[0] for i in ITEMS if i[4][0] == "drug"]
    wj(f"{D}/tags/item/drugs.json", {"replace": False, "values": [f"{MOD}:{x}" for x in drugs]})
    wj(f"{D}/tags/item/smokables.json", {"replace": False, "values": [f"{MOD}:{i[0]}" for i in ITEMS if i[4][0] == "drug" and i[4][4]]})
    wj(f"{D}/tags/item/seeds.json", {"replace": False, "values": [f"{MOD}:{CROP_SEED[c[0]]}" for c in CROPS]})
    wj(f"{D}/tags/item/machine_fuel.json", {"replace": False, "values": [M+"coal", M+"charcoal", M+"blaze_powder", M+"blaze_rod"]})
    wj(f"{D}/tags/item/precursors.json", {"replace": False, "values": [f"{MOD}:{x}" for x in ("solvent", "chem_base", "catalyst", "raw_crystal")]})

def jtok(tok):
    ns, path = tok.split(":")
    return f"Items.{path.upper()}" if ns == "minecraft" else f'ModItems.get("{path}")'

def emit_java():
    lines = []
    for iid, _, _, _, j in ITEMS:
        k = j[0]
        if k == "plain": lines.append(f'        d.plain("{iid}", {j[1]});')
        elif k == "seeds": lines.append(f'        d.seeds("{iid}", "{j[1]}");')
        elif k == "drug":
            lines.append(f'        d.drug("{iid}", "{j[1]}", {j[2]}f, {str(j[3]).lower()}, {str(j[4]).lower()}, ItemCatalog.Rem.{j[5]}, {j[6]});')
        elif k == "clipboard": lines.append(f'        d.clipboard("{iid}");')
        elif k == "antidote": lines.append(f'        d.antidote("{iid}", {j[1]});')
        elif k == "detox": lines.append(f'        d.detox("{iid}", {j[1]});')
    body = "\n".join(lines)
    wt(f"{JAVA}/ItemCatalog.java", f'''package dev.drogas;

/** GERADO por tools/generate.py - nao edite a mao. Lista de todos os itens do mod. */
public final class ItemCatalog {{
    private ItemCatalog() {{}}

    public enum Rem {{ NONE, BOTTLE, SYRINGE }}

    public interface Definer {{
        void plain(String id, int stack);
        void seeds(String id, String cropBlockId);
        void drug(String id, String substanceId, float dose, boolean drink, boolean smoke, Rem remainder, int stack);
        void clipboard(String id);
        void antidote(String id, int stack);
        void detox(String id, int stack);
    }}

    public static void define(Definer d) {{
{body}
    }}
}}
''')
    rl = []
    for (t, ticks, ings, (out, cnt)) in MACHINE_RECIPES:
        ing = ", ".join(f"ing({jtok(tk)}, {n})" for tk, n in ings)
        rl.append(f"        add(MachineType.{t}, {ticks}, {jtok(out)}, {cnt}, {ing});")
    wt(f"{JAVA}/MachineRecipes.java", f'''package dev.drogas;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** GERADO por tools/generate.py - nao edite a mao. Receitas das maquinas (todas fictícias, so itens de jogo). */
public final class MachineRecipes {{
    private MachineRecipes() {{}}

    public record Ing(Item item, int count) {{}}

    public record Recipe(MachineType type, int ticks, Item output, int outCount, List<Ing> inputs) {{
        /** Retorna os indices dos slots de entrada usados, ou null se a receita nao casa. */
        public int[] match(NonNullList<ItemStack> items) {{
            int n = type.inputs;
            boolean[] used = new boolean[n];
            int[] slots = new int[inputs.size()];
            for (int k = 0; k < inputs.size(); k++) {{
                Ing ing = inputs.get(k);
                int found = -1;
                for (int s = 0; s < n; s++) {{
                    ItemStack st = items.get(s);
                    if (!used[s] && !st.isEmpty() && st.is(ing.item()) && st.getCount() >= ing.count()) {{ found = s; break; }}
                }}
                if (found < 0) return null;
                used[found] = true; slots[k] = found;
            }}
            for (int s = 0; s < n; s++) if (!used[s] && !items.get(s).isEmpty()) return null;
            return slots;
        }}
    }}

    private static final List<Recipe> ALL = new ArrayList<>();
    private static boolean ready = false;

    private static Ing ing(Item item, int count) {{ return new Ing(item, count); }}
    private static void add(MachineType type, int ticks, Item out, int outCount, Ing... ings) {{
        ALL.add(new Recipe(type, ticks, out, outCount, List.of(ings)));
    }}

    public static List<Recipe> all() {{ init(); return ALL; }}

    public static Recipe find(MachineType type, NonNullList<ItemStack> items) {{
        init();
        for (Recipe r : ALL) if (r.type() == type && r.match(items) != null) return r;
        return null;
    }}

    private static synchronized void init() {{
        if (ready) return;
        ready = true;
{chr(10).join(rl)}
    }}
}}
''')

def emit_docs():
    pt = {i[0]: i[1] for i in ITEMS}
    for (m, p, *_r) in MACHINES: pt[m] = p
    pt[CRATE[0]] = CRATE[1]
    def nm(tok):
        ns, path = tok.split(":")
        return pt.get(path, path.replace("_", " ").title()) if ns == MOD else path.replace("_", " ").title() + " (vanilla)"
    mpt = {m[3]: m[1] for m in MACHINES}
    out = ["# Receitas do mod\n", "Todas as receitas são **fictícias** e usam apenas itens do jogo. Ingredientes marcados \"(vanilla)\" vêm do Minecraft.\n",
           "## Crafting (mesa de trabalho / inventário)\n", "| Resultado | Ingredientes |", "|---|---|"]
    for (res, n, ing) in CRAFTING:
        ings = {}
        for x in ing: ings[x] = ings.get(x, 0) + 1
        out.append(f"| {n}× {pt.get(res, res)} | " + ", ".join(f"{c}× {nm(x if ':' in x else MOD+':'+x) if not x.startswith('#') else 'Tábuas (qualquer)'}" for x, c in ings.items()) + " |")
    for t in ("DRYER", "GRINDER", "TABLE", "BENCH", "FURNACE"):
        out += [f"\n## {mpt[t]}\n", "| Resultado | Ingredientes | Tempo |", "|---|---|---|"]
        for (tt, ticks, ings, (o, c)) in MACHINE_RECIPES:
            if tt == t: out.append(f"| {c}× {nm(o)} | " + ", ".join(f"{n}× {nm(tk)}" for tk, n in ings) + f" | {ticks/20:.0f}s |")
    out.append("\n**Combustível do Forno de Processamento:** carvão, carvão vegetal, pó de blaze, vara de blaze.\n")
    wt(os.path.join(ROOT, "RECEITAS.md"), "\n".join(out))

if __name__ == "__main__":
    emit_textures(); emit_models(); emit_lang(); emit_data(); emit_java(); emit_docs()
    # sanidade: ids de receita existem
    ids = {i[0] for i in ITEMS} | {m[0] for m in MACHINES} | {CRATE[0]}
    for (_, _, ings, (o, _)) in MACHINE_RECIPES:
        for tk, _n in ings + [(o, 0)]:
            if tk.startswith(Dm): assert tk[len(Dm):] in ids, tk
    for (res, _, ing) in CRAFTING:
        assert res in ids, res
        for x in ing:
            if x.startswith(Dm): assert x[len(Dm):] in ids, x
    for c in CROPS: assert CROP_PRODUCE[c[0]][0] in ids and CROP_SEED[c[0]] in ids
    print("OK", len(ITEMS), "itens;", len(MACHINE_RECIPES), "receitas de maquina;", len(CRAFTING), "receitas de crafting")
