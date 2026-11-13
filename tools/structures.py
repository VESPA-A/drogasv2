#!/usr/bin/env python3
"""Gera as 4 estruturas (NBT de template, jigsaw, structure_set, tags de bioma) e as loot tables dos baus."""
import gzip, struct, json, os, random, sys
sys.path.insert(0, os.path.dirname(__file__))
from tiers import TIERS
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
D = os.path.join(ROOT, "src/main/resources/data/drogas")
DATA_VERSION = 4671  # Minecraft 1.21.11

# ------------------------------------------------------------------ NBT minimo
def _s(s): b = s.encode("utf-8"); return struct.pack(">H", len(b)) + b
def _tid(v):
    if isinstance(v, bool): return 1
    if isinstance(v, int): return 3
    if isinstance(v, str): return 8
    if isinstance(v, dict): return 10
    if isinstance(v, list): return 9
    raise TypeError(v)
def _pay(v):
    t = _tid(v)
    if t == 1: return struct.pack(">b", int(v))
    if t == 3: return struct.pack(">i", v)
    if t == 8: return _s(v)
    if t == 10: return b"".join(bytes([_tid(x)]) + _s(k) + _pay(x) for k, x in v.items()) + b"\x00"
    if t == 9:
        if not v: return b"\x00" + struct.pack(">i", 0)
        return bytes([_tid(v[0])]) + struct.pack(">i", len(v)) + b"".join(_pay(x) for x in v)
def write_nbt(path, root):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, "wb") as f: f.write(b"\x0a" + _s("") + _pay(root))
def read_nbt(path):  # leitor para validar a saida
    data = gzip.open(path, "rb").read(); pos = [0]
    def rd(n): b = data[pos[0]:pos[0]+n]; pos[0] += n; return b
    def rs(): n = struct.unpack(">H", rd(2))[0]; return rd(n).decode()
    def rp(t):
        if t == 1: return struct.unpack(">b", rd(1))[0]
        if t == 3: return struct.unpack(">i", rd(4))[0]
        if t == 8: return rs()
        if t == 10:
            d = {}
            while True:
                tt = rd(1)[0]
                if tt == 0: return d
                k = rs(); d[k] = rp(tt)
        if t == 9:
            et = rd(1)[0]; n = struct.unpack(">i", rd(4))[0]; return [rp(et) for _ in range(n)]
        raise ValueError(t)
    assert rd(1)[0] == 10; rs(); return rp(10)

class Tpl:
    def __init__(s, sx, sy, sz, seed=1):
        s.size = (sx, sy, sz); s.pal = []; s.idx = {}; s.blocks = {}; s.r = random.Random(seed)
    def state(s, name, props=None):
        key = (name, tuple(sorted((props or {}).items())))
        if key not in s.idx:
            e = {"Name": name}
            if props: e["Properties"] = {k: str(v) for k, v in sorted(props.items())}
            s.idx[key] = len(s.pal); s.pal.append(e)
        return s.idx[key]
    def set(s, x, y, z, name, props=None, nbt=None):
        sx, sy, sz = s.size; assert 0 <= x < sx and 0 <= y < sy and 0 <= z < sz, (x, y, z, s.size)
        s.blocks[(x, y, z)] = (s.state(name, props), nbt)
    def fill(s, x0, y0, z0, x1, y1, z1, name, props=None):
        for x in range(x0, x1+1):
            for y in range(y0, y1+1):
                for z in range(z0, z1+1): s.set(x, y, z, name, props)
    def container(s, x, y, z, kind, facing, loot):
        props = {"facing": facing, "type": "single", "waterlogged": "false"} if kind == "chest" else {"facing": "up", "open": "false"}
        s.set(x, y, z, f"minecraft:{kind}", props, {"id": f"minecraft:{kind}", "LootTable": loot})
    def save(s, path):
        blocks = [{"pos": [x, y, z], "state": st, **({"nbt": nbt} if nbt else {})} for (x, y, z), (st, nbt) in sorted(s.blocks.items())]
        write_nbt(path, {"DataVersion": DATA_VERSION, "size": list(s.size), "palette": s.pal, "blocks": blocks, "entities": []})

AIR = "minecraft:air"
def pick(r, *opts): return r.choice(opts)

def lab():
    t = Tpl(11, 7, 9, 11); r = t.r
    wall = lambda: pick(r, *["minecraft:stone_bricks"]*5, "minecraft:cracked_stone_bricks", "minecraft:cracked_stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:mossy_stone_bricks")
    for x in range(11):
        for z in range(9):
            t.set(x, 0, z, pick(r, "minecraft:cobblestone", "minecraft:cobblestone", "minecraft:mossy_cobblestone"))
            t.set(x, 1, z, pick(r, "minecraft:stone_bricks", "minecraft:cracked_stone_bricks", "minecraft:stone_bricks", "minecraft:andesite"))
    for y in (2, 3, 4):
        for x in range(11):
            for z in (0, 8): t.set(x, y, z, wall())
        for z in range(9):
            for x in (0, 10): t.set(x, y, z, wall())
    for y in (2, 3, 4):
        for x in range(1, 10):
            for z in range(1, 8): t.set(x, y, z, AIR)
    for x in range(11):
        for z in range(9): 
            if r.random() > .18: t.set(x, 5, z, wall())
    for x in (0, 2, 4, 6, 8, 10):
        t.set(x, 6, 0, "minecraft:stone_brick_slab", {"type": "bottom", "waterlogged": "false"}); t.set(x, 6, 8, "minecraft:stone_brick_slab", {"type": "bottom", "waterlogged": "false"})
    for z in (0, 8):
        for x in (2, 3, 7, 8): t.set(x, 3, z, "minecraft:iron_bars", {"east": "false", "north": "false", "south": "false", "waterlogged": "false", "west": "false"})
    for y in (2, 3): t.set(5, y, 8, AIR)           # entrada quebrada
    for (x, y, z) in ((0, 3, 4), (10, 2, 5), (3, 4, 0), (7, 3, 8)): t.set(x, y, z, AIR)
    f = {"facing": "south", "lit": "false"}
    t.set(2, 2, 2, "drogas:chemistry_bench", f); t.set(4, 2, 2, "drogas:grinder", f); t.set(6, 2, 2, "drogas:processing_table", f)
    t.set(8, 2, 2, "minecraft:cauldron"); t.set(9, 2, 2, "minecraft:brewing_stand")
    t.container(1, 2, 6, "chest", "east", "drogas:chests/lab"); t.container(9, 2, 6, "barrel", "up", "drogas:chests/lab")
    t.set(5, 2, 6, "drogas:field_notes_lab"); t.set(1, 2, 2, "drogas:storage_crate")
    for (x, y, z) in ((1, 4, 1), (9, 4, 1), (1, 4, 7), (9, 4, 7)): t.set(x, y, z, "minecraft:cobweb")
    for _ in range(9): t.set(r.randint(1, 9), 2, r.randint(3, 7), "minecraft:cobblestone") if (lambda: True)() else None
    t.set(5, 2, 6, "drogas:field_notes_lab"); t.set(1, 2, 6, "minecraft:chest", {"facing": "east", "type": "single", "waterlogged": "false"}, {"id": "minecraft:chest", "LootTable": "drogas:chests/lab"})
    return t

def greenhouse():
    t = Tpl(13, 7, 9, 22); r = t.r
    for x in range(13):
        for z in range(9):
            t.set(x, 0, z, "minecraft:stone_bricks")
            edge = z in (0, 8) or x in (0, 12)
            if edge: t.set(x, 1, z, "minecraft:stone_bricks")
            elif z in (1, 7): t.set(x, 1, z, "minecraft:dirt_path")
            elif z == 4: t.set(x, 1, z, "minecraft:water", {"level": "0"})
            else: t.set(x, 1, z, "minecraft:farmland", {"moisture": "7"})
    for y in (2, 3, 4):
        for x in range(13):
            for z in (0, 8):
                if x in (0, 4, 8, 12): t.set(x, y, z, "minecraft:oak_log", {"axis": "y"})
                elif r.random() > .12: t.set(x, y, z, "minecraft:glass")
        for z in range(1, 8):
            for x in (0, 12):
                if r.random() > .1: t.set(x, y, z, "minecraft:glass")
        for x in range(1, 12):
            for z in range(1, 8): t.set(x, y, z, AIR)
    for x in range(13):
        for z in range(9):
            if x in (0, 12) or z in (0, 8): t.set(x, 5, z, "minecraft:oak_slab", {"type": "bottom", "waterlogged": "false"}) 
            elif r.random() > .15: t.set(x, 5, z, "minecraft:glass")
    for y in (2, 3): t.set(6, y, 8, AIR)
    kinds = {2: "cannabis", 3: "tobacco", 5: "poppy", 6: "chacrona"}
    for z, k in kinds.items():
        for x in range(1, 12):
            if r.random() < .12: t.set(x, 2, z, "minecraft:dead_bush"); continue
            age = r.choice([2, 3, 4, 5, 6, 7, 7, 7]); st = r.choice([0, 0, 1, 1, 2])
            t.set(x, 2, z, f"drogas:{k}_crop", {"age": age, "stress": st, "vigor": r.choice([0, 1, 1, 2])})
    for (x, z) in ((1, 1), (2, 1)): t.set(x, 1, z, "drogas:organic_substrate"); t.set(x, 2, z, "drogas:psilocybin_crop", {"age": r.choice([4, 6, 7]), "stress": 0, "vigor": 1})
    for y in (2, 3, 4):
        t.set(11, y, 4, "drogas:trellis")
        t.set(10, y, 4, "drogas:jagube_climber", {"age": 3 if y > 2 else 2, "east": "true", "north": "false", "south": "false", "up": "false", "west": "false"})
    t.set(1, 2, 2, AIR); t.set(1, 2, 2, "drogas:cannabis_crop", {"age": 7, "stress": 0, "vigor": 2})
    t.set(2, 2, 7, "drogas:greenhouse_regulator", {"lit": "false"}); t.set(4, 2, 7, "drogas:field_notes_greenhouse")
    t.container(11, 2, 1, "chest", "south", "drogas:chests/greenhouse"); t.container(11, 2, 7, "barrel", "up", "drogas:chests/greenhouse")
    for (x, z) in ((2, 4), (6, 4), (10, 4)): pass
    return t

def researcher():
    t = Tpl(9, 6, 8, 33); r = t.r
    for x in range(9):
        for z in range(8):
            t.set(x, 0, z, "minecraft:cobblestone"); t.set(x, 1, z, "minecraft:spruce_planks")
    for y in (2, 3, 4):
        for x in range(9):
            for z in (0, 7): t.set(x, y, z, "minecraft:oak_log", {"axis": "y"} ) if x in (0, 8) else t.set(x, y, z, "minecraft:spruce_planks")
        for z in range(8):
            for x in (0, 8):
                if z in (0, 7): t.set(x, y, z, "minecraft:oak_log", {"axis": "y"})
                else: t.set(x, y, z, "minecraft:spruce_planks")
        for x in range(1, 8):
            for z in range(1, 7): t.set(x, y, z, AIR)
    for x in range(-1 + 1, 9):
        for z in range(8): t.set(x, 5, z, "minecraft:spruce_slab", {"type": "bottom", "waterlogged": "false"})
    for z in (2, 4): 
        for x in (0, 8): t.set(x, 3, z, "minecraft:glass_pane", {"east": "false", "north": "false", "south": "false", "waterlogged": "false", "west": "false"})
    for x in (2, 6): t.set(x, 3, 0, "minecraft:glass_pane", {"east": "false", "north": "false", "south": "false", "waterlogged": "false", "west": "false"})
    for y in (2, 3): t.set(4, y, 7, AIR)
    t.set(1, 2, 1, "minecraft:crafting_table"); t.set(2, 2, 1, "minecraft:furnace", {"facing": "south", "lit": "false"})
    for x in range(3, 8):
        t.set(x, 2, 1, "minecraft:bookshelf"); 
        if x % 2: t.set(x, 3, 1, "minecraft:bookshelf")
    t.set(1, 2, 3, "drogas:processing_table", {"facing": "east", "lit": "false"}); t.set(1, 2, 5, "drogas:dryer", {"facing": "east", "lit": "false"})
    t.set(4, 2, 3, "drogas:field_notes_researcher")
    t.container(7, 2, 3, "chest", "west", "drogas:chests/researcher"); t.container(7, 2, 5, "barrel", "up", "drogas:chests/researcher")
    t.set(4, 4, 4, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"}); t.set(4, 5, 4, "minecraft:spruce_planks")
    t.set(3, 2, 5, "minecraft:red_carpet"); t.set(4, 2, 5, "minecraft:red_carpet"); t.set(5, 2, 5, "minecraft:red_carpet")
    return t

def depot():
    t = Tpl(7, 5, 7, 44); r = t.r
    for x in range(7):
        for z in range(7):
            t.set(x, 0, z, "minecraft:cobblestone"); t.set(x, 1, z, "minecraft:oak_planks")
    for y in (2, 3):
        for x in range(7):
            for z in (0, 6):
                t.set(x, y, z, "minecraft:oak_log", {"axis": "y"}) if x in (0, 6) else t.set(x, y, z, "minecraft:oak_planks")
        for z in range(1, 6):
            for x in (0, 6): t.set(x, y, z, "minecraft:oak_planks")
        for x in range(1, 6):
            for z in range(1, 6): t.set(x, y, z, AIR)
    for x in (2, 3, 4):
        for y in (2, 3): t.set(x, y, 6, AIR)          # frente aberta
    for x in range(7):
        for z in range(7): t.set(x, 4, z, "minecraft:oak_slab", {"type": "bottom", "waterlogged": "false"})
    t.set(1, 2, 1, "drogas:storage_crate"); t.set(1, 3, 1, "drogas:storage_crate"); t.set(2, 2, 1, "drogas:storage_crate")
    t.container(5, 2, 1, "chest", "west", "drogas:chests/depot"); t.container(5, 2, 2, "chest", "west", "drogas:chests/depot")
    t.set(1, 2, 4, "minecraft:hay_block", {"axis": "y"}); t.set(1, 2, 5, "minecraft:hay_block", {"axis": "y"})
    t.container(5, 2, 4, "barrel", "up", "drogas:chests/depot"); t.set(3, 2, 1, "drogas:grinder", {"facing": "south", "lit": "false"})
    t.set(5, 2, 5, "drogas:field_notes_depot")
    return t

STRUCTS = {
    "abandoned_lab": dict(build=lab, spacing=34, sep=10, salt=1840331, biomes=["#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_hill", "#minecraft:is_mountain", "minecraft:plains", "minecraft:swamp", "#minecraft:is_badlands"]),
    "abandoned_greenhouse": dict(build=greenhouse, spacing=28, sep=9, salt=1840332, biomes=["#minecraft:is_forest", "#minecraft:is_jungle", "#minecraft:is_savanna", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:swamp", "minecraft:meadow"]),
    "researcher_house": dict(build=researcher, spacing=22, sep=8, salt=1840333, biomes=["#minecraft:is_forest", "#minecraft:is_taiga", "#minecraft:is_jungle", "minecraft:swamp", "minecraft:plains"]),
    "equipment_depot": dict(build=depot, spacing=16, sep=6, salt=1840334, biomes=["minecraft:plains", "minecraft:desert", "#minecraft:is_savanna", "#minecraft:is_badlands", "#minecraft:is_taiga", "#minecraft:is_forest"]),
}

def wj(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(o, open(p, "w", encoding="utf-8"), indent=2)

def chest_table(pools):
    out = []
    for (lo, hi, parts, empty) in pools:
        entries = []
        for item, w, mn, mx in parts:
            e = {"type": "minecraft:item", "name": f"drogas:{item}", "weight": w}
            if (mn, mx) != (1, 1): e["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": float(mn), "max": float(mx)}}]
            entries.append(e)
        if empty: entries.append({"type": "minecraft:empty", "weight": empty})
        out.append({"rolls": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}, "entries": entries})
    return {"type": "minecraft:chest", "pools": out}
def scale(tier, k): return [(i, max(1, round(w*k)), a, b) for (i, w, a, b) in TIERS[tier]]
def only(tier, names, k=1): return [(i, max(1, round(w*k)), a, b) for (i, w, a, b) in TIERS[tier] if i in names]
DOCS_ALL = sorted({i for t in TIERS.values() for (i, *_r) in t if i.startswith("doc_")})
LOOT = {
    "lab": [(3, 5, scale("C", 3) + scale("B", 2), 0), (0, 2, scale("D", 3), 22), (1, 1, [(d, 1, 1, 1) for d in ("doc_chem_safety", "doc_opioid_research", "doc_stimulant_research", "doc_psychedelic_research", "doc_synthetics_lab", "doc_maintenance_log")], 0)],
    "greenhouse": [(3, 6, scale("A", 3) + scale("B", 2), 0), (0, 2, scale("C", 2), 10), (1, 1, [("doc_greenhouse_blueprint", 3, 1, 1), ("doc_field_guide_basic", 2, 1, 1), ("doc_mycology_notes", 2, 1, 1), ("doc_jungle_expedition", 2, 1, 1)], 0)],
    "researcher": [(2, 4, [(d, 2, 1, 1) for d in DOCS_ALL], 0), (2, 4, scale("A", 2) + scale("B", 2), 0), (0, 1, scale("D", 1), 14)],
    "depot": [(3, 5, only("B", ("maintenance_kit", "speed_module_1", "trellis", "organic_substrate"), 3) + [("processing_table", 2, 1, 1), ("grinder", 2, 1, 1), ("dryer", 2, 1, 1), ("storage_crate", 4, 1, 2)], 0), (0, 2, only("C", ("speed_module_2", "greenhouse_regulator", "solvent", "chem_base", "doc_maintenance_log"), 2), 8), (0, 1, only("D", ("chemistry_bench", "processing_furnace", "speed_module_3")), 18)],
}

def emit():
    for sid, c in STRUCTS.items():
        c["build"]().save(os.path.join(D, "structure", sid + ".nbt"))
        wj(os.path.join(D, "worldgen/structure", sid + ".json"), {
            "type": "minecraft:jigsaw", "biomes": f"#drogas:has_{sid}", "step": "surface_structures", "spawn_overrides": {},
            "terrain_adaptation": "beard_thin", "start_pool": f"drogas:{sid}/start", "size": 1, "start_height": {"absolute": -2},
            "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 80, "use_expansion_hack": False})
        wj(os.path.join(D, "worldgen/template_pool", sid, "start.json"), {"fallback": "minecraft:empty", "elements": [
            {"weight": 1, "element": {"element_type": "minecraft:single_pool_element", "location": f"drogas:{sid}", "processors": "minecraft:empty", "projection": "rigid"}}]})
        wj(os.path.join(D, "worldgen/structure_set", sid + ".json"), {"structures": [{"structure": f"drogas:{sid}", "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": c["spacing"], "separation": c["sep"], "salt": c["salt"]}})
        wj(os.path.join(D, "tags/worldgen/biome", f"has_{sid}.json"), {"replace": False, "values": c["biomes"]})
    for k, pools in LOOT.items(): wj(os.path.join(D, "loot_table/chests", k + ".json"), chest_table(pools))

if __name__ == "__main__":
    emit()
    for sid in STRUCTS:
        n = read_nbt(os.path.join(D, "structure", sid + ".nbt")); print(sid, n["size"], len(n["blocks"]), "blocos,", len(n["palette"]), "estados")
