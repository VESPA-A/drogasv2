#!/usr/bin/env python3
"""Orquestrador da v2.0: estende o catalogo da v1 (generate.py) e emite assets, dados e classes Java geradas."""
import os, sys, glob, json
sys.path.insert(0, os.path.dirname(__file__))
import generate as g, content as c, art, art2, structures, sounds, tiers
from generate import wj, wt, wp, A, D, MC, JAVA, RES, MOD, ROOT

# formas de consumo: define animacao e som (ver ModItems / DrugItem)
FORM_OVERRIDE = {"cocaine_powder": "SNORT", "heroin_dose": "INJECT", "cannabis_brownie": "CHEW", "magic_mushroom": "CHEW",
                 "dried_magic_mushroom": "CHEW", "coca_leaf": "CHEW", "morphine_vial": "INJECT"}
PT = {}; EN = {}
def names():
    for i in g.ITEMS: PT[i[0]] = i[1]; EN[i[0]] = i[2]
    for m in g.MACHINES: PT[m[0]] = m[1]; EN[m[0]] = m[2]
    PT[g.CRATE[0]] = g.CRATE[1]; EN[g.CRATE[0]] = g.CRATE[2]
    for (i, p, e) in c.NEW_BLOCK_ITEMS: PT[i] = p; EN[i] = e
    PT["speed_module"] = "Módulos de Velocidade"; EN["speed_module"] = "Speed Modules"

# ====================================================================== culturas 8 estagios x estresse
def emit_crops():
    for f in glob.glob(f"{A}/textures/block/*_stage*.png") + glob.glob(f"{A}/models/block/*_crop_stage*.json"): os.remove(f)
    for (cid, pt, en, kind, c1, c2, fl) in g.CROPS:
        for s in range(8):
            base = art2.crop8(kind, s, c1, c2, fl)
            for st in range(3):
                wp(f"{A}/textures/block/{cid}_a{s}_s{st}.png", art2.stress_filter(base, st))
                wj(f"{A}/models/block/{cid}_crop_a{s}_s{st}.json", {"parent": "minecraft:block/crop", "textures": {"crop": f"{MOD}:block/{cid}_a{s}_s{st}"}})
        wj(f"{A}/blockstates/{cid}_crop.json", {"variants": {f"age={a},stress={st},vigor={v}": {"model": f"{MOD}:block/{cid}_crop_a{a}_s{st}"}
                                                         for a in range(8) for st in range(3) for v in range(4)}})
        # loot: producao por vigor/estresse + qualidade; semente devolvida (imaturo) ou 1-2 (maduro)
        prod = g.CROP_PRODUCE[cid][0]; seed = g.CROP_SEED[cid]; pools = []
        for v in range(4):
            for st in range(3):
                lo, hi = [(1, 1), (1, 2), (2, 3), (3, 4)][v]
                if st == 1: lo, hi = max(1, lo-1), max(1, hi-1)
                if st == 2: lo = hi = 1
                q = max(0, min(3, v - (1 if st >= 1 else 0)))
                fn = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}}]
                if q != 1: fn.append({"function": "minecraft:set_components", "components": {"drogas:quality": q}})
                pools.append({"rolls": 1, "conditions": [{"condition": "minecraft:block_state_property", "block": f"{MOD}:{cid}_crop",
                              "properties": {"age": "7", "vigor": str(v), "stress": str(st)}}], "entries": [{"type": "minecraft:item", "name": f"{MOD}:{prod}", "functions": fn}]})
        mature = {"condition": "minecraft:block_state_property", "block": f"{MOD}:{cid}_crop", "properties": {"age": "7"}}
        pools.append({"rolls": 1, "conditions": [mature], "entries": [{"type": "minecraft:item", "name": f"{MOD}:{seed}", "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 2.0}}]}]})
        pools.append({"rolls": 1, "conditions": [{"condition": "minecraft:inverted", "term": mature}], "entries": [{"type": "minecraft:item", "name": f"{MOD}:{seed}"}]})
        wj(f"{D}/loot_table/blocks/{cid}_crop.json", {"type": "minecraft:block", "pools": pools})

def emit_vine():
    for a in range(4):
        wp(f"{A}/textures/block/jagube_climber_a{a}.png", art2.vine_tex(a))
        tex = {"particle": "#vine", "vine": f"{MOD}:block/jagube_climber_a{a}"}
        wj(f"{A}/models/block/jagube_climber_side_a{a}.json", {"ambientocclusion": False, "textures": tex, "elements": [{"from": [0, 0, 15.2], "to": [16, 16, 15.2], "shade": False,
            "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#vine"}, "south": {"uv": [0, 0, 16, 16], "texture": "#vine"}}}]})
        wj(f"{A}/models/block/jagube_climber_up_a{a}.json", {"ambientocclusion": False, "textures": tex, "elements": [{"from": [0, 15.2, 0], "to": [16, 15.2, 16], "shade": False,
            "faces": {"up": {"uv": [0, 0, 16, 16], "texture": "#vine"}, "down": {"uv": [0, 0, 16, 16], "texture": "#vine"}}}]})
    none = {"north": "false", "east": "false", "south": "false", "west": "false", "up": "false"}; parts = []
    for a in range(4):
        for key, rot in (("south", 0), ("west", 90), ("north", 180), ("east", 270)):
            ap = {"model": f"{MOD}:block/jagube_climber_side_a{a}"}
            if rot: ap["y"] = rot
            parts.append({"when": {"OR": [{**none, "age": str(a)}, {"age": str(a), key: "true"}]}, "apply": ap})
        parts.append({"when": {"age": str(a), "up": "true"}, "apply": {"model": f"{MOD}:block/jagube_climber_up_a{a}"}})
    wj(f"{A}/blockstates/jagube_climber.json", {"multipart": parts})
    wj(f"{D}/loot_table/blocks/jagube_climber.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "conditions": [
        {"condition": "minecraft:block_state_property", "block": f"{MOD}:jagube_climber", "properties": {"age": "3"}}],
        "entries": [{"type": "minecraft:item", "name": f"{MOD}:jagube_vine", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 2.0}}]}]}]})

# ====================================================================== blocos novos + animacao
def cube_bt(name, top, side, items_model=True):
    wj(f"{A}/models/block/{name}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {"top": f"{MOD}:block/{top}", "bottom": f"{MOD}:block/{side}", "side": f"{MOD}:block/{side}"}})
    wj(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    wj(f"{A}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{name}"}})

def emit_blocks():
    # maquinas: front_on animado (4 quadros)
    for (mid, _p, _e, _t, top, side, front) in g.MACHINES:
        frames = art2.sparkle_frames(front(True), 4, (4, 4, 11, 11), seed=hash(mid) % 997 if False else sum(map(ord, mid)))
        wp(f"{A}/textures/block/{mid}_front_on.png", art2.anim_strip(frames))
        wj(f"{A}/textures/block/{mid}_front_on.png.mcmeta", {"animation": {"frametime": 5}})
    # estufa reguladora
    wp(f"{A}/textures/block/greenhouse_regulator_top.png", art2.regulator_top()); wp(f"{A}/textures/block/greenhouse_regulator_side.png", art2.regulator_side())
    wp(f"{A}/textures/block/greenhouse_regulator_front.png", art2.regulator_front(False))
    wp(f"{A}/textures/block/greenhouse_regulator_front_on.png", art2.anim_strip([art2.regulator_front(True, k) for k in range(4)]))
    wj(f"{A}/textures/block/greenhouse_regulator_front_on.png.mcmeta", {"animation": {"frametime": 4}})
    for on in (False, True):
        wj(f"{A}/models/block/greenhouse_regulator{'_on' if on else ''}.json", {"parent": "minecraft:block/orientable", "textures": {
            "top": f"{MOD}:block/greenhouse_regulator_top", "side": f"{MOD}:block/greenhouse_regulator_side", "front": f"{MOD}:block/greenhouse_regulator_front{'_on' if on else ''}"}})
    wj(f"{A}/blockstates/greenhouse_regulator.json", {"variants": {"lit=false": {"model": f"{MOD}:block/greenhouse_regulator"}, "lit=true": {"model": f"{MOD}:block/greenhouse_regulator_on"}}})
    wj(f"{A}/items/greenhouse_regulator.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/greenhouse_regulator"}})
    # treliça (recorte)
    wp(f"{A}/textures/block/trellis.png", art2.trellis())
    wj(f"{A}/models/block/trellis.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/trellis"}})
    wj(f"{A}/blockstates/trellis.json", {"variants": {"": {"model": f"{MOD}:block/trellis"}}})
    wj(f"{A}/items/trellis.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/trellis"}})
    # substrato e anotacoes
    wp(f"{A}/textures/block/organic_substrate_top.png", art2.substrate_top()); wp(f"{A}/textures/block/organic_substrate_side.png", art2.substrate_side())
    cube_bt("organic_substrate", "organic_substrate_top", "organic_substrate_side")
    wp(f"{A}/textures/block/field_notes_side.png", art2.notes_side())
    for k, v in c.NOTES_META.items():
        wp(f"{A}/textures/block/field_notes_{k}_top.png", art2.notes_top(v[2])); cube_bt(f"field_notes_{k}", f"field_notes_{k}_top", "field_notes_side")
    surv = [{"condition": "minecraft:survives_explosion"}]
    for b in ("greenhouse_regulator", "trellis", "organic_substrate"):
        wj(f"{D}/loot_table/blocks/{b}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "conditions": surv, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{b}"}]}]})
    for k in c.NOTES_META: wj(f"{D}/loot_table/blocks/field_notes_{k}.json", {"type": "minecraft:block", "pools": []})

# ====================================================================== dados: receitas, tags, advancement
def emit_data2():
    wj(f"{D}/advancement/recipes/starter.json", {"criteria": {"auto": {"trigger": "minecraft:tick"}}, "requirements": [["auto"]],
        "rewards": {"recipes": [f"{MOD}:{r}" for r in c.STARTER_RECIPES]}})
    mine = lambda p: (f"{MC}/tags/block/mineable/{p}.json")
    wj(mine("pickaxe"), {"replace": False, "values": [f"{MOD}:{m}" for m in ("chemistry_bench", "grinder", "processing_furnace", "greenhouse_regulator")]})
    wj(mine("axe"), {"replace": False, "values": [f"{MOD}:{m}" for m in ["processing_table", "dryer", "storage_crate", "trellis"] + [f"field_notes_{k}" for k in c.NOTES_META]]})
    wj(mine("shovel"), {"replace": False, "values": [f"{MOD}:organic_substrate"]})
    wj(f"{MC}/tags/block/climbable.json", {"replace": False, "values": [f"{MOD}:jagube_climber"]})
    glass = ["minecraft:glass", "minecraft:tinted_glass"] if False else ["minecraft:glass"]
    colors = "white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black".split()
    glass += [f"minecraft:{col}_stained_glass" for col in colors] + ["minecraft:glass_pane"] + [f"minecraft:{col}_stained_glass_pane" for col in colors]
    wj(f"{D}/tags/block/greenhouse_glass.json", {"replace": False, "values": glass})
    wj(f"{D}/tags/block/mushroom_substrate.json", {"replace": False, "values": ["minecraft:mycelium", "minecraft:podzol", "minecraft:mud", "minecraft:muddy_mangrove_roots",
        "minecraft:moss_block", "minecraft:rooted_dirt", "minecraft:dirt", "minecraft:coarse_dirt", f"{MOD}:organic_substrate"]})
    wj(f"{D}/tags/block/mushroom_substrate_prime.json", {"replace": False, "values": ["minecraft:mycelium", "minecraft:podzol", f"{MOD}:organic_substrate"]})
    wj(f"{D}/tags/block/drug_crops.json", {"replace": False, "values": [f"{MOD}:{x[0]}_crop" for x in g.CROPS]})
    cl = lambda n, v: wj(f"{D}/tags/worldgen/biome/climate/{n}.json", {"replace": False, "values": v})
    cl("humid", ["#minecraft:is_jungle", "minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:dark_forest", "minecraft:lush_caves", "minecraft:river"])
    cl("temperate", ["minecraft:plains", "minecraft:sunflower_plains", "#minecraft:is_forest", "minecraft:meadow", "minecraft:cherry_grove", "minecraft:birch_forest", "#minecraft:is_taiga", "minecraft:beach", "#minecraft:is_hill"])
    cl("arid", ["#minecraft:is_badlands", "minecraft:desert", "#minecraft:is_savanna", "minecraft:stony_shore"])
    cl("cold", ["minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:snowy_taiga", "minecraft:snowy_slopes", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:grove", "minecraft:frozen_river", "minecraft:snowy_beach"])
    wj(f"{D}/tags/item/documents.json", {"replace": False, "values": [f"{MOD}:{d}" for d in c.DOC_META]})
    wj(f"{D}/tags/item/speed_modules.json", {"replace": False, "values": [f"{MOD}:speed_module_{i}" for i in (1, 2, 3)]})

# ====================================================================== enciclopedia
CAT_OF = {"plant": "PLANT", "equip": "EQUIPMENT", "sub": "SUBSTANCE", "rec": "RECIPE", "guide": "GUIDE"}
def build_entries():
    E = []  # (id, cat, default, icon, link, pt_name, en_name, pt_text, en_text)
    plant_pt = {"cannabis": "Maconha", "poppy": "Papoula", "tobacco": "Tabaco", "psilocybin": "Cogumelos Alucinógenos", "coca": "Coca", "jagube": "Jagube", "chacrona": "Chacrona"}
    plant_en = {"cannabis": "Cannabis", "poppy": "Poppy", "tobacco": "Tobacco", "psilocybin": "Magic Mushrooms", "coca": "Coca", "jagube": "Jagube", "chacrona": "Chacrona"}
    for (cid, *_r) in g.CROPS: E.append((f"plant.{cid}", "PLANT", False, g.CROP_PRODUCE[cid][0], cid, plant_pt[cid], plant_en[cid], *c.PLANT_TXT[cid]))
    icon_of = {"speed_module": "speed_module_1"}
    for k, (p, e, tp, te) in c.EQUIP_TXT.items(): E.append((f"equip.{k}", "EQUIPMENT", False, icon_of.get(k, k), "", p, e, tp, te))
    for (sid, p, e, _col) in g.SUBSTANCES: E.append((f"sub.{sid}", "SUBSTANCE", False, g.EFFECT_GLYPH[sid], sid, p, e, *c.SUB_TXT[sid]))
    for (t, ticks, ings, (o, cnt)) in g.MACHINE_RECIPES:
        op = o.split(":")[1]; mp = {m[3]: m for m in g.MACHINES}[t]
        E.append((f"rec.machine.{t}.{op}", "RECIPE", False, op, f"rec.machine.{t}.{op}", f"{mp[1]}: {PT.get(op, op)}", f"{mp[2]}: {EN.get(op, op)}", "", ""))
    for (res, n, ing) in g.CRAFTING: E.append((f"rec.craft.{res}", "RECIPE", False, res, res, f"Receita: {PT.get(res, res)}", f"Recipe: {EN.get(res, res)}", "", ""))
    for k, (p, e, tp, te) in c.GUIDE_TXT.items(): E.append((f"guide.{k}", "GUIDE", k == "intro", "encyclopedia", "", p, e, tp, te))
    return E

def emit_knowledge(E):
    ids = {e[0] for e in E}
    for d, m in c.DOC_META.items():
        for x in m[3]: assert x in ids, (d, x)
    for k, m in c.NOTES_META.items():
        for x in m[3]: assert x in ids, (k, x)
    adds = "\n".join(f'        add("{e[0]}", Cat.{e[1]}, {str(e[2]).lower()}, "{e[3]}", "{e[4]}");' for e in E)
    groups = "\n".join(f'        group("{d}", {", ".join(chr(34)+x+chr(34) for x in m[3])});' for d, m in c.DOC_META.items()) + "\n" + \
             "\n".join(f'        group("notes_{k}", {", ".join(chr(34)+x+chr(34) for x in m[3])});' for k, m in c.NOTES_META.items())
    # itens que ao serem possuidos revelam equipamento / receita de crafting
    trig = {}
    for e in E:
        if e[0].startswith("rec.craft."): trig.setdefault(e[0][len("rec.craft."):], []).append(e[0])
    equip_items = {"processing_table": "processing_table", "chemistry_bench": "chemistry_bench", "dryer": "dryer", "grinder": "grinder", "processing_furnace": "processing_furnace",
                   "storage_crate": "storage_crate", "greenhouse_regulator": "greenhouse_regulator", "trellis": "trellis", "organic_substrate": "organic_substrate",
                   "speed_module_1": "speed_module", "speed_module_2": "speed_module", "speed_module_3": "speed_module", "maintenance_kit": "maintenance_kit",
                   "emergency_kit": "emergency_kit", "detox_tonic": "detox_tonic", "organic_fertilizer": "organic_fertilizer"}
    for it, ek in equip_items.items(): trig.setdefault(it, []).append(f"equip.{ek}")
    trigs = "\n".join(f'        trigger("{k}", {", ".join(chr(34)+x+chr(34) for x in v)});' for k, v in sorted(trig.items()))
    crafts = "\n".join(f'        CRAFTS.add(new CraftRecipe("rec.craft.{res}", "{res}", {n}, List.of({", ".join(chr(34)+(x if ":" in x else MOD+":"+x)+chr(34) for x in ing)})));' for (res, n, ing) in g.CRAFTING)
    wt(f"{JAVA}/KnowledgeData.java", f'''package dev.drogas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GERADO por tools/generate2.py - nao edite a mao. Entradas da enciclopedia, documentos e gatilhos de descoberta. */
public final class KnowledgeData {{
    private KnowledgeData() {{}}

    public enum Cat {{ PLANT, EQUIPMENT, SUBSTANCE, RECIPE, GUIDE }}
    public record Entry(String id, Cat cat, boolean unlockedByDefault, String icon, String link) {{}}
    public record CraftRecipe(String id, String result, int count, List<String> ingredients) {{}}

    public static final List<Entry> ALL = new ArrayList<>();
    public static final Map<String, Entry> BY_ID = new HashMap<>();
    public static final Map<String, List<String>> GROUPS = new HashMap<>();
    public static final Map<String, List<String>> ITEM_TRIGGERS = new HashMap<>();
    public static final List<CraftRecipe> CRAFTS = new ArrayList<>();

    private static void add(String id, Cat cat, boolean def, String icon, String link) {{
        Entry e = new Entry(id, cat, def, icon, link);
        ALL.add(e); BY_ID.put(id, e);
    }}
    private static void group(String id, String... entries) {{ GROUPS.put(id, List.of(entries)); }}
    private static void trigger(String item, String... entries) {{ ITEM_TRIGGERS.put(item, List.of(entries)); }}

    static {{
{adds}
{groups}
{trigs}
{crafts}
    }}
}}
''')

def emit_lang2(E):
    L = {}
    def put(k, p, e): L[k] = (p, e)
    for e in E:
        put(f"enc.{MOD}.{e[0]}.name", e[5], e[6])
        if e[7]: put(f"enc.{MOD}.{e[0]}.text", e[7], e[8])
    for (i, p, e) in c.NEW_BLOCK_ITEMS: put(f"block.{MOD}.{i}", p, e)
    put(f"block.{MOD}.jagube_climber", "Trepadeira de Jagube", "Jagube Climber")
    tabs = [("plants", "Plantas", "Plants"), ("equipment", "Equip.", "Equip."), ("substances", "Substâncias", "Substances"), ("recipes", "Receitas", "Recipes"),
            ("guides", "Guias", "Guides"), ("status", "Condição", "Status"), ("options", "Opções", "Options")]
    for k, p, e in tabs: put(f"gui.{MOD}.tab.{k}", p, e)
    G = {
     "title": ("Enciclopédia de Substâncias", "Substance Encyclopedia"), "locked": ("???", "???"), "up": ("▲", "▲"), "down": ("▼", "▼"),
     "hint.plant": ("Planta não descoberta. Procure anotações de campo e documentos, ou colha-a.", "Undiscovered plant. Find field notes and documents, or harvest it."),
     "hint.equipment": ("Equipamento não descoberto. Encontre-o, estude-o ou leia um manual.", "Undiscovered equipment. Find it, study it or read a manual."),
     "hint.substance": ("Substância não descoberta. Leia pesquisas ou experimente-a.", "Undiscovered substance. Read research or try it."),
     "hint.recipe": ("Receita não descoberta. Experimente combinar ingredientes numa máquina ou leia manuais.", "Undiscovered recipe. Experiment in a machine or read manuals."),
     "hint.guide": ("Guia não descoberto. Explore, leia documentos e sobreviva às consequências.", "Undiscovered guide. Explore, read documents and survive the consequences."),
     "fact.soil": ("Solo: %s", "Soil: %s"), "soil.farmland": ("terra arada", "farmland"), "soil.dirt": ("terra comum ou arada", "dirt or farmland"), "soil.substrate": ("substrato de cogumelo", "mushroom substrate"),
     "fact.light": ("Luz: %s a %s", "Light: %s to %s"), "fact.water": ("Água: %s", "Water: %s"), "water.yes": ("necessária por perto", "needed nearby"), "water.no": ("não exige", "not required"),
     "fact.climate": ("Clima ideal: %s", "Ideal climate: %s"), "fact.tolerates": ("Tolera: %s", "Tolerates: %s"), "fact.growth": ("Crescimento: %s", "Growth: %s"),
     "growth.fast": ("rápido", "fast"), "growth.medium": ("médio", "medium"), "growth.slow": ("lento", "slow"), "growth.very_slow": ("muito lento", "very slow"),
     "fact.yield": ("Colheita: %s", "Harvest: %s"), "fact.dark": ("Exige escuro e sem sol direto", "Needs darkness, no direct sun"),
     "climate.humid": ("úmido", "humid"), "climate.temperate": ("temperado", "temperate"), "climate.arid": ("árido", "arid"), "climate.cold": ("frio", "cold"),
     "fact.duration": ("Duração: %s", "Duration: %s"), "fact.group": ("Tipo: %s", "Type: %s"), "fact.tolerance": ("Tolerância: %s", "Tolerance: %s"),
     "fact.dependence": ("Dependência: %s", "Dependence: %s"), "fact.overdose": ("Limite de overdose: %s doses", "Overdose limit: %s doses"),
     "fact.lethal": ("Overdose letal", "Lethal overdose"), "fact.nonlethal": ("Excesso causa bad trip (não letal)", "Excess causes a bad trip (non-lethal)"),
     "rating.none": ("nenhuma", "none"), "rating.low": ("baixa", "low"), "rating.mid": ("média", "medium"), "rating.high": ("alta", "high"), "rating.extreme": ("extrema", "extreme"),
     "group.STIMULANT": ("estimulante", "stimulant"), "group.DEPRESSANT": ("depressor", "depressant"), "group.OPIOID": ("opioide", "opioid"), "group.PSYCHEDELIC": ("psicodélico", "psychedelic"),
     "group.DISSOCIATIVE": ("dissociativo", "dissociative"), "group.ENTACTOGEN": ("entactógeno", "entactogen"), "group.CANNABINOID": ("canabinoide", "cannabinoid"), "group.HYBRID": ("sintético", "synthetic"),
     "fact.mods": ("Efeitos: %s", "Effects: %s"), "mod.speed": ("velocidade %s%%", "speed %s%%"), "mod.damage": ("dano %s", "damage %s"), "mod.armor": ("armadura %s", "armor %s"),
     "mod.kb": ("resist. a empurrão %s", "knockback res. %s"), "mod.jump": ("pulo %s", "jump %s"), "mod.reach": ("alcance %s", "reach %s"), "mod.mining": ("mineração %s%%", "mining %s%%"),
     "mod.atkspeed": ("ataque %s%%", "attack speed %s%%"), "mod.fall": ("queda segura %s", "safe fall %s"), "mod.heal": ("regeneração", "regeneration"), "mod.hunger": ("fome", "hunger"),
     "recipe.machine": ("Máquina: %s", "Machine: %s"), "recipe.time": ("Tempo base: %s s", "Base time: %s s"), "recipe.ingredients": ("Ingredientes:", "Ingredients:"), "recipe.craft": ("Mesa de trabalho", "Crafting table"),
     "recipe.unknown_name": ("Receita desconhecida", "Unknown recipe"),
     "machine.TABLE": ("Mesa de Processamento", "Processing Table"), "machine.BENCH": ("Bancada Química", "Chemistry Bench"), "machine.DRYER": ("Secador", "Dryer"),
     "machine.GRINDER": ("Triturador", "Grinder"), "machine.FURNACE": ("Forno de Processamento", "Processing Furnace"),
     "m.upgrade": ("Melhoria", "Upgrade"), "m.fuel": ("Combustível", "Fuel"), "m.recipe": ("Receita: %s", "Recipe: %s"), "m.unknown": ("reação desconhecida", "unknown reaction"),
     "m.none": ("nenhuma", "none"), "m.time": ("Restante: %s", "Remaining: %s"), "m.speed": ("Velocidade: %s%%", "Speed: %s%%"), "m.wear": ("Desgaste: %s", "Wear: %s"),
     "m.fuel_left": ("Combustível: %s", "Fuel: %s"), "m.status": ("Estado: %s", "Status: %s"),
     "m.status.0": ("ocioso", "idle"), "m.status.1": ("funcionando", "working"), "m.status.2": ("sem combustível", "out of fuel"), "m.status.3": ("saída cheia", "output full"),
     "m.status.4": ("QUEBRADA", "BROKEN"), "m.status.5": ("sem sol (use combustível)", "no sun (use fuel)"),
     "m.wear.0": ("bom", "good"), "m.wear.1": ("sujo", "dirty"), "m.wear.2": ("gasto", "worn"), "m.wear.3": ("quebrado", "broken"),
     "opt.hud": ("HUD: %s", "HUD: %s"), "opt.sway": ("Balanço da câmera: %s", "Camera sway: %s"), "opt.fov": ("Efeitos de FOV: %s", "FOV effects: %s"),
     "opt.particles": ("Partículas de alucinação: %s", "Hallucination particles: %s"), "opt.visual": ("Intensidade visual: %s", "Visual intensity: %s"),
     "opt.on": ("ligado", "on"), "opt.off": ("desligado", "off"), "opt.note": ("Opções de jogabilidade (duração, overdose...) ficam em config/drogas.json no servidor.", "Gameplay options (duration, overdose...) live in config/drogas.json on the server."),
     "status.title": ("Estado do jogador", "Player status"), "status.hint": ("Barras: tolerância (amarelo), dependência (vermelho), carga/overdose (laranja).", "Bars: tolerance (yellow), dependence (red), load/overdose (orange)."),
     "tip.knowledge": ("Entradas descobertas: %s / %s", "Discovered entries: %s / %s"),
    }
    for k, (p, e) in G.items(): put(f"gui.{MOD}.{k}", p, e)
    M2 = {
     "message.drogas.discovered": ("Enciclopédia: %s", "Encyclopedia: %s"), "message.drogas.discovered_many": ("Enciclopédia: %s novas entradas", "Encyclopedia: %s new entries"),
     "message.drogas.doc_known": ("Você já conhece tudo neste documento.", "You already know everything in this document."),
     "message.drogas.machine_broken": ("A máquina quebrou. Use um Kit de Manutenção.", "The machine broke. Use a Maintenance Kit."),
     "message.drogas.serviced": ("Manutenção feita.", "Serviced."), "message.drogas.no_wear": ("A máquina não precisa de manutenção.", "The machine needs no service."),
     "message.drogas.fertilized": ("A planta ganhou vigor.", "The plant gained vigor."), "message.drogas.impure": ("Impurezas! Você não se sente bem...", "Impurities! You feel unwell..."),
     "message.drogas.regulator": ("Estufa: combustível %s%% | água %s%%", "Greenhouse: fuel %s%% | water %s%%"),
     "message.drogas.regulator_fuel": ("Combustível adicionado.", "Fuel added."), "message.drogas.regulator_water": ("Água adicionada.", "Water added."),
     "message.drogas.notes": ("Você revisa as anotações.", "You review the notes."),
     "tooltip.drogas.quality": ("Qualidade: %s", "Quality: %s"), "tooltip.drogas.substance": ("Substância: %s", "Substance: %s"),
     "tooltip.drogas.document": ("Documento de pesquisa — botão direito para ler", "Research document — right-click to read"),
     "tooltip.drogas.module": ("Melhoria nível %s: velocidade x%s", "Upgrade tier %s: speed x%s"),
     "tooltip.drogas.kit": ("Botão direito numa máquina para fazer manutenção", "Right-click a machine to service it"),
     "tooltip.drogas.encyclopedia": ("Botão direito para abrir", "Right-click to open"),
     "quality.drogas.0": ("Ruim", "Poor"), "quality.drogas.1": ("Padrão", "Standard"), "quality.drogas.2": ("Boa", "Good"), "quality.drogas.3": ("Excelente", "Excellent"),
     "command.drogas.knowledge": ("Enciclopédia: %s entradas liberadas.", "Encyclopedia: %s entries unlocked."), "command.drogas.knowledge_clear": ("Enciclopédia reiniciada.", "Encyclopedia reset."),
    }
    for k, (p, e) in M2.items(): put(k, p, e)
    for k, v in sounds.SUB.items(): put(f"subtitles.drogas.{k.replace('/', '.')}", v, v)
    for fn, lang in (("pt_br", 0), ("en_us", 1)):
        path = f"{A}/lang/{fn}.json"; cur = json.load(open(path, encoding="utf-8"))
        for k, v in L.items(): cur[k] = v[lang]
        wj(path, cur)

# ====================================================================== Java gerado: catalogo de itens, receitas, loot, sons
def emit_java2():
    lines = []; quality = []
    for iid, _p, _e, _im, j in g.ITEMS:
        k = j[0]
        if k == "plain":
            lines.append(f'        d.plain("{iid}", {j[1]});')
            if iid not in c.QUALITY_EXCLUDE: quality.append(iid)
        elif k == "seeds": lines.append(f'        d.seeds("{iid}", "{j[1]}");')
        elif k == "drug":
            form = FORM_OVERRIDE.get(iid, "SMOKE" if j[4] else "DRINK" if j[3] else "PILL")
            lines.append(f'        d.drug("{iid}", "{j[1]}", {j[2]}f, ItemCatalog.Form.{form}, ItemCatalog.Rem.{j[5]}, {j[6]});'); quality.append(iid)
        elif k == "clipboard": lines.append(f'        d.clipboard("{iid}");')
        elif k == "antidote": lines.append(f'        d.antidote("{iid}", {j[1]});')
        elif k == "detox": lines.append(f'        d.detox("{iid}", {j[1]});')
        elif k == "module": lines.append(f'        d.module("{iid}", {j[1]});')
        elif k == "doc": lines.append(f'        d.doc("{iid}");')
        elif k == "fertilizer": lines.append(f'        d.fertilizer("{iid}");')
        elif k == "encyclopedia": lines.append(f'        d.encyclopedia("{iid}");')
        else: raise ValueError(k)
    q = ", ".join(f'"{x}"' for x in sorted(set(quality)))
    wt(f"{JAVA}/ItemCatalog.java", f'''package dev.drogas;

import java.util.Set;

/** GERADO por tools/generate2.py - nao edite a mao. Lista de todos os itens do mod. */
public final class ItemCatalog {{
    private ItemCatalog() {{}}

    public enum Rem {{ NONE, BOTTLE, SYRINGE }}
    /** Forma de consumo: define animacao, sons e particulas. */
    public enum Form {{ PILL, DRINK, SMOKE, SNORT, INJECT, CHEW }}

    /** Itens que carregam o componente de qualidade. */
    public static final Set<String> QUALITY_ITEMS = Set.of({q});

    public interface Definer {{
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
    }}

    public static void define(Definer d) {{
{chr(10).join(lines)}
    }}
}}
''')
    rl = []
    for (t, ticks, ings, (out, cnt)) in g.MACHINE_RECIPES:
        ing = ", ".join(f"ing({g.jtok(tk)}, {n})" for tk, n in ings)
        rl.append(f'        add("rec.machine.{t}.{out.split(":")[1]}", MachineType.{t}, {ticks}, {g.jtok(out)}, {cnt}, {ing});')
    wt(f"{JAVA}/MachineRecipes.java", f'''package dev.drogas;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** GERADO por tools/generate2.py - nao edite a mao. Receitas das maquinas (todas ficticias, so itens de jogo). */
public final class MachineRecipes {{
    private MachineRecipes() {{}}

    public record Ing(Item item, int count) {{}}

    public record Recipe(String id, MachineType type, int ticks, Item output, int outCount, List<Ing> inputs) {{
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
    private static void add(String id, MachineType type, int ticks, Item out, int outCount, Ing... ings) {{
        ALL.add(new Recipe(id, type, ticks, out, outCount, List.of(ings)));
    }}

    public static List<Recipe> all() {{ init(); return ALL; }}

    public static Recipe byId(String id) {{
        init();
        for (Recipe r : ALL) if (r.id().equals(id)) return r;
        return null;
    }}

    public static int indexOf(Recipe r) {{ init(); return ALL.indexOf(r); }}
    public static Recipe byIndex(int i) {{ init(); return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }}

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
    # loot (tiers + injecao em baus vanilla)
    tl = ",\n".join(f'            "{k}", List.of({", ".join(f"new E({chr(34)}{i}{chr(34)}, {w}, {a}, {b})" for (i, w, a, b) in v)})' for k, v in tiers.TIERS.items())
    inj = "\n".join(f'        INJECT.put("{p}", List.of({", ".join(f"new Rule({chr(34)}{t}{chr(34)}, {ch}f)" for (t, ch) in rules)}));' for p, rules in tiers.INJECT.items())
    wt(f"{JAVA}/LootData.java", f'''package dev.drogas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** GERADO por tools/generate2.py - nao edite a mao. Raridades e regras de injecao de loot em baus vanilla. */
public final class LootData {{
    private LootData() {{}}

    public record E(String item, int weight, int min, int max) {{}}
    public record Rule(String tier, float chance) {{}}

    /** A = comum (sementes basicas) ... D = muito raro (produtos finais e equipamentos avancados). */
    public static final Map<String, List<E>> TIERS = Map.of(
{tl});

    public static final Map<String, List<Rule>> INJECT = new HashMap<>();
    static {{
{inj}
    }}
}}
''')
    names, _sub = sounds.build() if False else (None, None)
    snames = sorted(k.replace("/", ".") for k in sounds.S)
    wt(f"{JAVA}/SoundCatalog.java", f'''package dev.drogas;

/** GERADO por tools/generate2.py - nao edite a mao. Eventos de som (arquivos .ogg em assets/drogas/sounds). */
public final class SoundCatalog {{
    private SoundCatalog() {{}}

    public static final String[] NAMES = {{ {", ".join(chr(34)+n+chr(34) for n in snames)} }};
}}
''')

def emit_docs2():
    out = ["# Receitas do mod (v2.0)\n", "Todas as receitas são **fictícias** e usam apenas itens do jogo. No jogo elas **não vêm reveladas**: descubra por experimentação, documentos e exploração.\n",
           "## Crafting (mesa de trabalho / inventário)\n", "| Resultado | Ingredientes |", "|---|---|"]
    def nm(tok):
        ns, path = tok.split(":") if ":" in tok else (MOD, tok)
        return PT.get(path, path.replace("_", " ").title()) if ns == MOD else path.replace("_", " ").title() + " (vanilla)"
    for (res, n, ing) in g.CRAFTING:
        cnt = {}
        for x in ing: cnt[x] = cnt.get(x, 0) + 1
        out.append(f"| {n}× {PT.get(res, res)} | " + ", ".join(f"{k}× " + ("Tábuas (qualquer)" if x.startswith("#") else nm(x)) for x, k in cnt.items()) + " |")
    mpt = {m[3]: m[1] for m in g.MACHINES}
    for t in ("DRYER", "GRINDER", "TABLE", "BENCH", "FURNACE"):
        out += [f"\n## {mpt[t]}\n", "| Resultado | Ingredientes | Tempo base |", "|---|---|---|"]
        for (tt, ticks, ings, (o, cn)) in g.MACHINE_RECIPES:
            if tt == t: out.append(f"| {cn}× {nm(o)} | " + ", ".join(f"{n}× {nm(tk)}" for tk, n in ings) + f" | {ticks/20:.0f}s |")
    out.append("\n**Combustível** (Forno de Processamento, Bancada Química e Secador à noite): carvão, carvão vegetal, pó de blaze, vara de blaze.\n")
    wt(os.path.join(ROOT, "RECEITAS.md"), "\n".join(out))

def checks(E):
    items = {i[0] for i in g.ITEMS} | {i[0] for i in c.NEW_BLOCK_ITEMS} | {m[0] for m in g.MACHINES} | {g.CRATE[0]}
    for tname, rows in tiers.TIERS.items():
        for (i, *_r) in rows: assert i in items, ("tier", tname, i)
    for k, pools in structures.LOOT.items():
        for (_a, _b, parts, _e) in pools:
            for (i, *_r) in parts: assert i in items, ("loot", k, i)
    for (res, _n, ing) in g.CRAFTING:
        assert res in items, res
        for x in ing:
            if ":" not in x: assert x in items, (res, x)
    for e in E:
        if e[3]: assert e[3] in items, e
    print("verificacoes de consistencia: OK;", len(E), "entradas;", len(items), "itens/blocos")

def main():
    names_before = None
    g.ITEMS.extend(c.NEW_ITEMS); g.CRAFTING.extend(c.NEW_CRAFTING); names()
    g.emit_textures(); g.emit_models(); g.emit_lang(); g.emit_data()
    emit_crops(); emit_vine(); emit_blocks(); emit_data2()
    structures.emit(); sounds.build()
    E = build_entries(); emit_knowledge(E); emit_lang2(E); emit_java2(); emit_docs2(); checks(E)

if __name__ == "__main__":
    main()
