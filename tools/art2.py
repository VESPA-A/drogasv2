"""Arte nova da v2.0: culturas com 8 estagios x 3 niveis de estresse, videira, modulos, documentos, blocos novos."""
from art import *
from PIL import Image
import zlib, random, math

def _rng(*k): return random.Random(zlib.crc32("|".join(map(str, k)).encode()))

def stress_filter(im, level):
    if level == 0: return im
    out = im.copy(); tgt = (196, 170, 70) if level == 1 else (112, 76, 40); k = .38 if level == 1 else .72
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = im.getpixel((x, y))
            if a: out.putpixel((x, y), (cl(r+(tgt[0]-r)*k), cl(g+(tgt[1]-g)*k), cl(b+(tgt[2]-b)*k), a))
    if level == 2:  # murchando: metade superior cai 1px
        s = new(); s.paste(out.crop((0, 0, 16, 8)), (0, 1)); s.paste(out.crop((0, 8, 16, 16)), (0, 8)); out = s
    return out

H8 = (3, 5, 7, 9, 11, 13, 14, 14)

def crop8(kind, stage, c1, c2, flower=None):
    im = new(); r = _rng(kind, stage); stem = (92, 130, 52, 255)
    if kind == "mushroom":
        n = (1, 1, 2, 2, 3, 4, 5, 6)[stage]; spots = [(4, 13), (9, 14), (12, 12), (6, 10), (11, 9), (3, 8)]
        for i in range(n):
            x, y = spots[i]; s = 1 + stage // 3 + (1 if i == 0 and stage >= 5 else 0)
            rect(im, x, y-s, x, y, (228, 214, 190, 255)); ellipse(im, x, y-s-0.4, s+1.4, s*.7+1.0, c1)
            if stage >= 3: px(im, x-1, y-s-1, (240, 240, 235, 255))
            if stage >= 7 - 1 and i % 2 == 0: px(im, x+1, y-s, (240, 240, 235, 255))
        return im
    h = H8[stage]; top = 15 - h; rect(im, 8, top, 8, 15, stem)
    if kind == "jagube":
        for y in range(top, 16): px(im, 8 + int(math.sin(y*0.9)*1.6), y, stem)
    leaves = 1 + stage // 2
    for i in range(leaves):
        y = 14 - i*max(2, h//leaves); side = -1 if i % 2 == 0 else 1; ln = 2 + stage//2 + (1 if kind in ("tobacco", "chacrona") else 0)
        if kind == "cannabis":
            for a in (-35, 0, 35):
                line(im, 8, y, 8+side*int(ln*math.cos(math.radians(a))), y-int(ln*math.sin(math.radians(a))), c1)
            px(im, 8+side*ln, y, c2)
        elif kind == "poppy":
            line(im, 8, y, 8+side*ln, y-1, c1); px(im, 8+side*ln, y-1, c2)
        elif kind in ("tobacco", "chacrona"):
            ellipse(im, 8+side*(ln//2+1), y-1, ln/2+1, 1.4+stage*.15, c1); px(im, 8+side*(ln//2+1), y-1, c2)
        elif kind == "coca":
            ellipse(im, 8+side*(ln//2+1), y, ln/2+.6, 1.2, c1); px(im, 8+side*ln, y, c2)
        else:
            ellipse(im, 8+side*2, y, 1.6, 1.3, c1)
    if stage >= 5 and flower:  # botoes/flores
        if kind == "cannabis": disc(im, 8, top, 1.6 + (stage-5)*.4, flower)
        elif kind == "poppy": disc(im, 8, top, 1.8 + (stage-5)*.3, flower); 
        elif kind == "tobacco": rect(im, 7, top-1, 9, top+1, flower)
        elif kind == "coca": px(im, 8, top-1, flower); px(im, 7, top, flower); px(im, 9, top, flower)
        elif kind == "chacrona": px(im, 8, top-1, flower); px(im, 7, top, sh(flower, .8))
        elif kind == "jagube": disc(im, 8, top, 1.4, flower)
    if stage == 7:  # maduro: visual proprio de colheita
        if kind == "cannabis":
            for (x, y) in ((6, top+3), (10, top+5), (7, top+7)): disc(im, x, y, 1.4, (90, 170, 56, 255)); px(im, x, y-1, (236, 140, 50, 255))
            px(im, 8, top-1, (236, 140, 50, 255))
        elif kind == "poppy":
            disc(im, 8, top, 2.2, (120, 150, 96, 255)); rect(im, 6, top-2, 10, top-2, (96, 120, 80, 255)); disc(im, 5, top+4, 1.3, (120, 150, 96, 255)); disc(im, 11, top+6, 1.3, (120, 150, 96, 255))
        elif kind == "coca":
            for (x, y) in ((5, top+3), (11, top+4), (6, top+7), (10, top+8)): px(im, x, y, (200, 40, 40, 255))
        elif kind == "tobacco": rect(im, 6, top-2, 10, top, (240, 170, 190, 255))
        elif kind == "chacrona":
            for (x, y) in ((5, top+2), (11, top+4)): px(im, x, y, (255, 255, 230, 255))
        elif kind == "jagube":
            for (x, y) in ((6, top+2), (10, top+5)): disc(im, x, y, 1.1, (150, 100, 170, 255))
    return im

def vine_tex(age, seed=1):
    r = _rng("vine", age); im = new(); n = (5, 11, 19, 28)[age]; g1 = (80, 130, 50, 255); g2 = (56, 96, 38, 255)
    for _ in range(n):
        x = r.randint(1, 14); y = r.randint(0, 15); ln = r.randint(1, 3 + age)
        for k in range(ln): px(im, x + (k % 2), min(15, y + k), g1 if r.random() < .6 else g2)
    if age >= 3:
        for _ in range(5): disc(im, r.randint(2, 13), r.randint(1, 14), 1.1, (160, 104, 180, 255))
    for x in range(0, 16, 5): line(im, x, 0, x, 6 + age*3, g2)
    return im

def module(tier):
    cols = {1: (200, 200, 205, 255), 2: (240, 200, 70, 255), 3: (90, 230, 235, 255)}[tier]
    im = new(); rect(im, 3, 4, 12, 12, (34, 100, 66, 255)); rect(im, 4, 5, 11, 11, (44, 128, 84, 255))
    for x in range(3, 13, 2): px(im, x, 3, cols); px(im, x, 13, cols)
    rect(im, 6, 7, 9, 9, (30, 30, 36, 255)); px(im, 5, 6, cols); px(im, 10, 10, cols)
    for k in range(tier): px(im, 5 + k*2, 11, (255, 90, 70, 255))
    line(im, 5, 5, 8, 5, (170, 230, 190, 255)); return outline(im)
def toolbox():
    im = new(); rect(im, 2, 6, 13, 13, (190, 52, 48, 255)); rect(im, 2, 6, 13, 7, (222, 80, 70, 255)); rect(im, 6, 3, 9, 5, (150, 150, 158, 255))
    rect(im, 2, 10, 13, 10, (130, 30, 30, 255)); rect(im, 7, 9, 8, 11, (230, 200, 90, 255)); line(im, 3, 12, 6, 12, (200, 205, 210, 255)); return outline(im)
def book(cover=(40, 110, 70, 255)):
    im = new(); rect(im, 3, 2, 12, 14, cover); rect(im, 3, 2, 4, 14, sh(cover, .65)); rect(im, 12, 3, 12, 13, (236, 232, 214, 255))
    disc(im, 8, 8, 2.6, (230, 200, 90, 255)); disc(im, 8, 8, 1.2, cover)
    line(im, 8, 5, 8, 11, (230, 200, 90, 255)); line(im, 6, 8, 10, 8, (230, 200, 90, 255)); return outline(im)
def doc(color):
    im = new(); rect(im, 3, 2, 12, 14, (238, 232, 212, 255)); rect(im, 3, 2, 12, 3, sh((238, 232, 212, 255), .86))
    for k in (5, 7, 9): line(im, 5, k, 10, k, (150, 140, 120, 255))
    disc(im, 10, 11, 2.2, color); disc(im, 10, 11, 1.0, sh(color, 1.35)); line(im, 9, 13, 8, 15, color); line(im, 11, 13, 12, 15, color)
    return outline(im)
def fertilizer():
    im = new(); ellipse(im, 8, 10, 4.6, 4.4, (120, 84, 50, 255)); ellipse(im, 7, 9, 2.4, 2.0, (150, 108, 66, 255)); rect(im, 6, 4, 9, 5, (96, 66, 40, 255))
    line(im, 5, 5, 10, 5, (190, 160, 100, 255)); ellipse(im, 8, 10, 1.8, 1.4, (90, 170, 70, 255)); line(im, 8, 11, 8, 8, (60, 120, 50, 255)); return outline(im)

# ----------------------------------------------------------------- blocos novos
def regulator_top():
    im = metal((84, 120, 112, 255), 81); rect(im, 3, 3, 12, 12, (50, 80, 74, 255))
    for x in range(4, 12, 2): line(im, x, 4, x, 11, (24, 40, 38, 255))
    return im
def regulator_side():
    im = metal((92, 128, 120, 255), 82); rect(im, 3, 3, 12, 12, (26, 56, 90, 255)); rect(im, 4, 5, 11, 11, (60, 130, 220, 255)); rect(im, 4, 5, 11, 5, (150, 210, 255, 255))
    for (x, y) in ((6, 8), (9, 9)): px(im, x, y, (220, 245, 255, 255))
    return im
def regulator_front(on=False, k=0):
    im = metal((92, 128, 120, 255), 83); rect(im, 3, 3, 12, 12, (22, 30, 30, 255)); disc(im, 8, 8, 3.6, (60, 70, 70, 255))
    a = math.radians(40 + (k*70 if on else 0)); line(im, 8, 8, 8+int(round(math.sin(a)*3)), 8-int(round(math.cos(a)*3)), (255, 230, 120, 255) if on else (170, 170, 170, 255))
    rect(im, 11, 4, 12, 5, (90, 255, 140, 255) if on else (70, 40, 40, 255)); return im
def trellis():
    im = new(); wood = (140, 100, 58, 255)
    for x in (0, 5, 10, 15): rect(im, x, 0, x, 15, wood)
    for y in (0, 5, 10, 15): rect(im, 0, y, 15, y, sh(wood, .8))
    return im
def substrate_top():
    im = tex_noise((70, 48, 34, 255), 91, .12); r = _rng("sub")
    for _ in range(22): px(im, r.randint(0, 15), r.randint(0, 15), (236, 230, 214, 255))
    for _ in range(14): px(im, r.randint(0, 15), r.randint(0, 15), (110, 150, 80, 255))
    return im
def substrate_side():
    im = tex_noise((84, 58, 40, 255), 92, .1); r = _rng("subs")
    rect(im, 0, 0, 15, 3, (60, 42, 30, 255))
    for _ in range(14): px(im, r.randint(0, 15), r.randint(0, 15), (226, 220, 204, 255))
    return im
def notes_top(color):
    im = planks((150, 108, 64, 255), 95); rect(im, 3, 4, 12, 12, (236, 230, 210, 255)); line(im, 8, 4, 8, 12, (170, 160, 140, 255))
    for y in (6, 8, 10): line(im, 4, y, 7, y, (110, 100, 90, 255)); line(im, 9, y, 11, y, (110, 100, 90, 255))
    rect(im, 12, 3, 13, 11, color); return im
def notes_side(): return planks((120, 86, 52, 255), 96)

def anim_strip(frames):
    s = Image.new("RGBA", (16, 16*len(frames)), (0, 0, 0, 0))
    for i, f in enumerate(frames): s.paste(f, (0, 16*i))
    return s
def sparkle_frames(base, n=4, region=(4, 4, 11, 11), seed=7, col=(255, 240, 170, 255)):
    out = []; r = _rng("spark", seed)
    for k in range(n):
        f = base.copy(); rr = _rng("sp", seed, k)
        for _ in range(3): px(f, rr.randint(region[0], region[2]), rr.randint(region[1], region[3]), col)
        out.append(f)
    return out
