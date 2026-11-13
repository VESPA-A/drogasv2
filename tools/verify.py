#!/usr/bin/env python3
"""Verificacao cruzada do projeto (codigo x assets x dados). Rode: python3 tools/verify.py"""
import os, re, json, glob, sys
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
R = f"{ROOT}/src/main/resources"; J = f"{ROOT}/src/main/java/dev/drogas"
err = []; ok = []
def chk(cond, msg): (ok if cond else err).append(msg)
java = {os.path.basename(f)[:-5]: open(f, encoding="utf-8").read() for f in glob.glob(J + "/**/*.java", recursive=True)}
alltext = "\n".join(java.values())

# 1) substancias: enum x icones do HUD x estilos visuais
subs = re.findall(r'^\s{4}([A-Z_]+)\("([a-z_]+)", Group', java["Substance"], re.M)
icons = re.findall(r'"(\w+)"', re.search(r'ICONS = \{(.*?)\};', java["DrugHud"], re.S).group(1))
styles = re.findall(r'Style\.(\w+)', re.search(r'BY_ORDINAL = \{(.*?)\};', java["VisualStyles"], re.S).group(1))
chk(len(subs) == len(icons) == len(styles), f"substancias({len(subs)}) = icones({len(icons)}) = estilos({len(styles)})")

# 2) chaves de traducao usadas no Java existem nos dois idiomas
pt = json.load(open(f"{R}/assets/drogas/lang/pt_br.json", encoding="utf-8")); en = json.load(open(f"{R}/assets/drogas/lang/en_us.json", encoding="utf-8"))
chk(set(pt) == set(en), "pt_br e en_us tem as mesmas chaves")
keys = set(re.findall(r'translatable\("([a-zA-Z0-9_.]+)"', alltext))
prefixes = {k for k in keys if k.endswith(".")}
missing = sorted(k for k in keys if not k.endswith(".") and k not in pt)
chk(not missing, f"chaves translatable ausentes: {missing}")
# chaves compostas (prefixo + variavel)
for pre, vals in {"gui.drogas.level.": "123", "gui.drogas.state.withdrawal.": "123", "gui.drogas.m.status.": "012345", "gui.drogas.m.wear.": "0123", "quality.drogas.": "0123",
                  "gui.drogas.tab.": None, "gui.drogas.climate.": None}.items():
    if pre in prefixes or any(pre in k for k in prefixes):
        if vals: chk(all(pre + v in pt for v in vals), f"chaves {pre}*")
chk(all(f"gui.drogas.tab.{t}" in pt for t in ["plants", "equipment", "substances", "recipes", "guides", "status", "options"]), "abas")
chk(all(f"gui.drogas.climate.{c}" in pt for c in ["humid", "temperate", "arid", "cold"]), "climas")
chk(all(f"gui.drogas.group.{g}" in pt for g in ["STIMULANT", "DEPRESSANT", "OPIOID", "PSYCHEDELIC", "DISSOCIATIVE", "ENTACTOGEN", "CANNABINOID", "HYBRID"]), "grupos")
chk(all(f"gui.drogas.machine.{m}" in pt for m in ["TABLE", "BENCH", "DRYER", "GRINDER", "FURNACE"]), "nomes de maquina")
chk(all(f"gui.drogas.{k}" in pt for k in ["soil.farmland", "soil.dirt", "soil.substrate", "water.yes", "water.no", "growth.fast", "growth.medium", "growth.slow", "growth.very_slow"]), "fatos de planta")
chk(all(f"gui.drogas.rating.{k}" in pt for k in ["none", "low", "mid", "high", "extreme"]), "ratings")
chk(all(f"gui.drogas.hint.{k}" in pt for k in ["plant", "equipment", "substance", "recipe", "guide"]), "dicas de entradas bloqueadas")

# 3) entradas da enciclopedia tem nome/texto
kd = java["KnowledgeData"]; ids = re.findall(r'add\("([^"]+)", Cat\.(\w+)', kd)
nm = [i for i, c in ids if f"enc.drogas.{i}.name" not in pt]; chk(not nm, f"entradas sem nome: {nm[:5]}")
tx = [i for i, c in ids if c != "RECIPE" and f"enc.drogas.{i}.text" not in pt]; chk(not tx, f"entradas sem texto: {tx[:5]}")

# 4) itens: modelo, item json, textura; blocos: blockstate, modelos, texturas
items = re.findall(r'd\.\w+\("([a-z_0-9]+)"', java["ItemCatalog"])
for i in items:
    for p in (f"assets/drogas/textures/item/{i}.png", f"assets/drogas/models/item/{i}.json", f"assets/drogas/items/{i}.json"):
        chk(os.path.exists(f"{R}/{p}"), f"asset {p}")
    chk(f"item.drogas.{i}" in pt, f"lang item.drogas.{i}")
blocks = [os.path.basename(f)[:-5] for f in glob.glob(f"{R}/assets/drogas/blockstates/*.json")]
def texpath(ref): return f"{R}/assets/{ref.split(':')[0]}/textures/{ref.split(':')[1]}.png"
def modelpath(ref): return f"{R}/assets/{ref.split(':')[0]}/models/{ref.split(':')[1]}.json"
nmod = 0
for b in blocks:
    bs = json.load(open(f"{R}/assets/drogas/blockstates/{b}.json"))
    models = set()
    if "variants" in bs:
        for v in bs["variants"].values(): models.add(v["model"] if isinstance(v, dict) else v[0]["model"])
    else:
        for part in bs["multipart"]: models.add(part["apply"]["model"])
    for m in models:
        if m.startswith("minecraft:"): continue
        chk(os.path.exists(modelpath(m)), f"modelo {m}"); nmod += 1
        md = json.load(open(modelpath(m)))
        for t in md.get("textures", {}).values():
            if t.startswith("drogas:"): chk(os.path.exists(texpath(t)), f"textura {t} (modelo {m})")
    chk(os.path.exists(f"{R}/data/drogas/loot_table/blocks/{b}.json"), f"loot table do bloco {b}")
    chk(f"block.drogas.{b}" in pt, f"lang block.drogas.{b}")
# ids de bloco registrados no Java == blockstates
reg = set(re.findall(r'register\("([a-z_]+)"', java["ModBlocks"])) | {s + "_crop" for s in re.findall(r'new CropSpec\("(\w+)"', java["CropSpec"])} | {m for m in re.findall(r'^\s+\w+\("(\w+)", \d, \d, -?\d, \d, \d, (?:true|false)', java["MachineType"], re.M)}
reg |= {"field_notes_" + k for k in ("lab", "greenhouse", "researcher", "depot")}; reg.discard("field_notes_")
chk(reg == set(blocks), f"blocos registrados == blockstates (dif: {sorted(reg ^ set(blocks))})")

# 5) loot/receitas/advancement/tags: todo id drogas citado existe
all_ids = set(items) | set(blocks)
def collect(o, acc):
    if isinstance(o, dict):
        for v in o.values(): collect(v, acc)
    elif isinstance(o, list):
        for v in o: collect(v, acc)
    elif isinstance(o, str) and o.startswith("drogas:"): acc.add(o[7:])
bad = []
for f in glob.glob(f"{R}/data/**/*.json", recursive=True):
    if "/worldgen/" in f or "/tags/worldgen/" in f: continue
    acc = set(); collect(json.load(open(f)), acc)
    for a in acc:
        if a not in all_ids and not a.startswith(("chests/", "quality", "climate/", "starter")) and not any(a.endswith(s) for s in ("_crop", "_climber")) and a not in ("greenhouse_glass",): bad.append((os.path.relpath(f, R), a))
chk(not bad, f"ids drogas: inexistentes em data/: {bad[:6]}")
# receitas vanilla: ingredientes de mod existem
# 6) sons
sj = json.load(open(f"{R}/assets/drogas/sounds.json")); cat = re.findall(r'"([a-z_.]+)"', re.search(r'NAMES = \{(.*?)\};', java["SoundCatalog"], re.S).group(1))
chk(set(sj) == set(cat), "sounds.json == SoundCatalog")
for ev, d in sj.items():
    for s in d["sounds"]: chk(os.path.exists(f"{R}/assets/drogas/sounds/{s['name'].split(':')[1]}.ogg"), f"ogg {s['name']}")
    chk(d["subtitle"] in pt, f"subtitulo {d['subtitle']}")
used = set(re.findall(r'"((?:item|machine|effect|ui|block)\.[a-z_]+)"', alltext)) | set(re.findall(r'"drogas:((?:item|machine|effect|ui|block)\.[a-z_]+)"', alltext))
chk(all(u in cat for u in used if u.split(".")[0] in ("item", "machine", "effect", "ui", "block") and u not in ("item.drogas",) and not u.startswith("block.drogas") and not u.startswith("item.drogas")), f"sons usados no Java existem: {sorted(u for u in used if u not in cat and not u.startswith(('block.drogas','item.drogas')))}")

# 7) estruturas
S = f"{R}/data/drogas"
for sid in ("abandoned_lab", "abandoned_greenhouse", "researcher_house", "equipment_depot"):
    for p in (f"structure/{sid}.nbt", f"worldgen/structure/{sid}.json", f"worldgen/structure_set/{sid}.json", f"worldgen/template_pool/{sid}/start.json", f"tags/worldgen/biome/has_{sid}.json"):
        chk(os.path.exists(f"{S}/{p}"), f"estrutura {p}")
for k in ("lab", "greenhouse", "researcher", "depot"): chk(os.path.exists(f"{S}/loot_table/chests/{k}.json"), f"loot chests/{k}")
# 8) propriedades/variantes de culturas
bs = json.load(open(f"{R}/assets/drogas/blockstates/cannabis_crop.json")); chk(len(bs["variants"]) == 96, "cannabis_crop: 96 variantes (idade x estresse x vigor)")
print(f"OK: {len(ok)} verificacoes | FALHAS: {len(err)}")
for e in err: print("  FALHA:", e)
sys.exit(1 if err else 0)
