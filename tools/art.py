"""Pixel-art procedural para texturas do mod (16x16 itens/blocos, 18x18 icones de efeito)."""
from PIL import Image, ImageDraw
import random, math

def new(n=16): return Image.new("RGBA", (n, n), (0, 0, 0, 0))
def hx(h, a=255):
    h = h.lstrip('#'); return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)
def cl(v): return max(0, min(255, int(v)))
def sh(c, f): return (cl(c[0]*f), cl(c[1]*f), cl(c[2]*f), c[3] if len(c) > 3 else 255)
def px(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height: im.putpixel((x, y), c)
def rect(im, x0, y0, x1, y1, c):
    for y in range(y0, y1+1):
        for x in range(x0, x1+1): px(im, x, y, c)
def line(im, x0, y0, x1, y1, c):
    dx, dy = abs(x1-x0), -abs(y1-y0); sx = 1 if x0 < x1 else -1; sy = 1 if y0 < y1 else -1; err = dx+dy
    while True:
        px(im, x0, y0, c)
        if x0 == x1 and y0 == y1: break
        e2 = 2*err
        if e2 >= dy: err += dy; x0 += sx
        if e2 <= dx: err += dx; y0 += sy
def disc(im, cx, cy, r, c):
    for y in range(int(cy-r-1), int(cy+r+2)):
        for x in range(int(cx-r-1), int(cx+r+2)):
            if (x-cx)**2 + (y-cy)**2 <= r*r: px(im, x, y, c)
def ellipse(im, cx, cy, rx, ry, c):
    for y in range(int(cy-ry-1), int(cy+ry+2)):
        for x in range(int(cx-rx-1), int(cx+rx+2)):
            if ((x-cx)/rx)**2 + ((y-cy)/ry)**2 <= 1: px(im, x, y, c)
def outline(im, col=(34, 26, 24, 255)):
    out = im.copy()
    for y in range(im.height):
        for x in range(im.width):
            if im.getpixel((x, y))[3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x+dx, y+dy
                    if 0 <= nx < im.width and 0 <= ny < im.height and im.getpixel((nx, ny))[3] > 0:
                        out.putpixel((x, y), col); break
    return out
def speckle(im, seed, cols, n, region=None):
    r = random.Random(seed); pts = [(x, y) for y in range(im.height) for x in range(im.width) if im.getpixel((x, y))[3] > 0]
    if region: pts = [p for p in pts if region(p)]
    for _ in range(n):
        if not pts: break
        x, y = r.choice(pts); px(im, x, y, r.choice(cols))
def diag(im, x0, y0, x1, y1, c, w=2):
    line(im, x0, y0, x1, y1, c)
    if w >= 2: line(im, x0+1, y0, x1+1, y1, c)
    if w >= 3: line(im, x0, y0+1, x1, y1+1, c)

# ---------------------------------------------------------------- itens
def vial(liquid, fill=0.65, glass=(205, 228, 238, 255), cork=(150, 104, 62, 255), label=None):
    im = new(); rect(im, 6, 2, 9, 3, cork); rect(im, 6, 4, 9, 6, glass); rect(im, 4, 7, 11, 14, glass)
    h = max(2, int(7*fill)); rect(im, 5, 14-h, 10, 14, liquid)
    rect(im, 5, 14-h, 10, 14-h, sh(liquid, 1.25)); px(im, 5, 8, (255, 255, 255, 255)); px(im, 6, 5, (255, 255, 255, 255))
    if label: rect(im, 5, 10, 10, 12, label)
    return outline(im)
def bottle(body, label=(230, 220, 190, 255), cap=(190, 170, 70, 255)):
    im = new(); rect(im, 6, 1, 9, 2, cap); rect(im, 6, 3, 9, 7, body); rect(im, 4, 8, 11, 14, body)
    rect(im, 4, 10, 11, 12, label); rect(im, 5, 11, 10, 11, sh(label, .75)); px(im, 5, 9, sh(body, 1.5)); px(im, 6, 4, sh(body, 1.5))
    return outline(im)
def wine(body=(70, 24, 40, 255)):
    im = new(); rect(im, 7, 1, 8, 2, (150, 30, 40, 255)); rect(im, 7, 3, 8, 8, body); rect(im, 5, 9, 10, 14, body)
    rect(im, 5, 11, 10, 13, (232, 220, 190, 255)); px(im, 8, 12, (150, 30, 40, 255)); px(im, 6, 10, sh(body, 1.7)); px(im, 7, 4, sh(body, 1.7))
    return outline(im)
def syringe(liquid, empty=False):
    im = new(); steel = (200, 205, 212, 255); glass = (220, 236, 244, 255)
    for i in range(8):
        cx, cy = 4+i, 11-i; px(im, cx, cy, glass); px(im, cx+1, cy, glass); px(im, cx, cy-1, glass)
        if not empty and i in (1, 2, 3, 4): px(im, cx, cy, liquid); px(im, cx+1, cy, liquid)
    line(im, 2, 13, 4, 11, steel); px(im, 1, 14, steel)
    diag(im, 11, 4, 13, 2, (90, 90, 100, 255), 2); line(im, 12, 3, 14, 1, (90, 90, 100, 255)); px(im, 14, 2, (60, 60, 70, 255))
    return outline(im)
def joint(paper=(236, 232, 218, 255), herb=(80, 150, 60, 255), filt=(206, 170, 110, 255), thick=2):
    im = new()
    diag(im, 3, 13, 12, 4, paper, thick)
    diag(im, 3, 13, 5, 11, filt, thick)
    px(im, 13, 3, (255, 140, 30, 255)); px(im, 12, 3, (255, 200, 60, 255)); px(im, 13, 4, (200, 70, 20, 255)); px(im, 14, 2, (120, 120, 120, 200))
    speckle(im, 3, [herb, sh(herb, .7)], 4, lambda p: 7 < p[0] < 13)
    return outline(im)
def cigarette(): return joint(thick=1, herb=(150, 110, 60, 255))
def cigar():
    im = new(); br = (120, 76, 40, 255)
    diag(im, 2, 13, 12, 3, br, 3); diag(im, 4, 11, 6, 9, (200, 160, 70, 255), 2)
    px(im, 13, 2, (255, 150, 40, 255)); px(im, 12, 2, (230, 90, 30, 255)); px(im, 3, 12, (150, 20, 20, 255))
    return outline(im)
def pill(c1, c2=None, kind="tab", mark=None):
    im = new()
    if kind == "tab":
        disc(im, 8, 8, 4.4, c1); disc(im, 7, 7, 2.4, sh(c1, 1.18))
        line(im, 5, 8, 11, 8, sh(c1, .7))
        if mark: px(im, 7, 6, mark); px(im, 9, 6, mark); px(im, 8, 10, mark)
    else:
        c2 = c2 or c1
        for i in range(9):
            cx, cy = 3+i, 12-i; c = c1 if i < 5 else c2
            px(im, cx, cy, c); px(im, cx+1, cy, c); px(im, cx, cy-1, sh(c, 1.15)); px(im, cx+1, cy+1, sh(c, .8))
    return outline(im)
def bag(powder, bagc=(235, 240, 245, 150)):
    im = new(); rect(im, 4, 3, 11, 14, bagc); rect(im, 4, 3, 11, 4, (255, 255, 255, 200))
    ellipse(im, 8, 11, 3.2, 2.4, powder); ellipse(im, 7, 10, 1.8, 1.2, sh(powder, 1.2)); line(im, 4, 5, 11, 5, (190, 200, 210, 255))
    speckle(im, 9, [sh(powder, .8)], 3, lambda p: p[1] > 8)
    return outline(im, (60, 70, 80, 255))
def pile(c, seed=1, sprinkle=None):
    im = new(); ellipse(im, 8, 11, 5.2, 3.2, c); ellipse(im, 8, 9, 3.4, 2.4, sh(c, 1.15)); ellipse(im, 7, 8, 1.6, 1.0, sh(c, 1.35))
    speckle(im, seed, [sh(c, .7), sprinkle or sh(c, 1.4)], 8)
    return outline(im)
def crystal(c):
    im = new()
    for (cx, cy, h, w) in ((6, 11, 7, 2), (10, 12, 5, 2), (8, 9, 8, 2)):
        for i in range(h):
            ww = max(1, w - (1 if i > h-3 else 0))
            rect(im, cx-ww, cy-i, cx+ww-1, cy-i, sh(c, 0.85+0.04*i))
        px(im, cx-1, cy-h+1, (255, 255, 255, 255))
    return outline(im)
def blotter(c, seed=1):
    im = new(); rect(im, 3, 3, 12, 12, (240, 240, 235, 255))
    for k in (3, 6, 9, 12):
        line(im, k, 3, k, 12, (190, 190, 185, 255)); line(im, 3, k, 12, k, (190, 190, 185, 255))
    r = random.Random(seed)
    for gy in range(4):
        for gx in range(4):
            if r.random() < .7: px(im, 4+gx*3, 4+gy*3, c); px(im, 5+gx*3, 4+gy*3, sh(c, .8)); px(im, 4+gx*3, 5+gy*3, sh(c, 1.2))
    return outline(im)
def mushroom(cap, spots=(240, 240, 235, 255), stem=(228, 214, 190, 255)):
    im = new(); rect(im, 7, 9, 8, 14, stem); px(im, 7, 10, sh(stem, .8)); px(im, 8, 13, sh(stem, .8))
    ellipse(im, 8, 8, 5.5, 3.6, cap); rect(im, 3, 10, 12, 12, (0, 0, 0, 0)); rect(im, 2, 9, 13, 9, sh(cap, .75))
    px(im, 6, 6, sh(cap, 1.3)); px(im, 7, 6, sh(cap, 1.3))
    for (x, y) in ((5, 7), (9, 6), (10, 8), (7, 8)): px(im, x, y, spots)
    return outline(im)
def cannabis_leaf(c1=(70, 150, 52, 255), c2=(40, 100, 38, 255)):
    im = new(); base = (8, 14)
    for ang, ln in ((-80, 7), (-48, 9), (0, 11), (48, 9), (80, 7)):
        tx = base[0] + math.sin(math.radians(ang))*ln; ty = base[1] - math.cos(math.radians(ang))*ln
        line(im, base[0], base[1], int(tx), int(ty), c1); line(im, base[0]+1, base[1], int(tx)+1, int(ty), c2)
        if ln > 8: line(im, base[0], base[1], int(tx), int(ty), sh(c1, 1.25))
    px(im, 8, 15, (90, 70, 40, 255)); px(im, 8, 14, (90, 70, 40, 255))
    return outline(im)
def broad_leaf(c1, c2, rib=(190, 200, 110, 255)):
    im = new(); ellipse(im, 8, 8, 4.0, 6.4, c1); ellipse(im, 9, 7, 2.0, 4.0, sh(c1, 1.12))
    line(im, 8, 14, 8, 2, rib)
    for k in range(4): line(im, 8, 11-k*2, 5 if k % 2 else 11, 9-k*2, sh(c2, 1.0))
    px(im, 8, 15, (110, 84, 46, 255))
    return outline(im)
def small_leaf(c1, c2):
    im = new()
    for (cx, cy, rx, ry) in ((5, 9, 2.4, 3.4), (11, 8, 2.4, 3.4), (8, 5, 2.2, 3.2), (8, 11, 2.2, 3.0)):
        ellipse(im, cx, cy, rx, ry, c1); ellipse(im, cx+.5, cy-.5, rx/2, ry/2, sh(c1, 1.2))
    line(im, 8, 14, 8, 4, c2); line(im, 8, 11, 5, 9, c2); line(im, 8, 10, 11, 8, c2)
    return outline(im)
def vine_item(c1, c2):
    im = new(); pts = [(3, 13), (5, 11), (7, 12), (9, 9), (7, 7), (9, 4), (12, 3)]
    for a, b in zip(pts, pts[1:]): line(im, a[0], a[1], b[0], b[1], c1); line(im, a[0], a[1]+1, b[0], b[1]+1, c2)
    for (x, y) in ((5, 10), (10, 9), (6, 6), (11, 5)): disc(im, x, y, 1.3, sh(c1, 1.15))
    return outline(im)
def bud(c=(86, 160, 56, 255), hair=(236, 140, 50, 255)):
    im = new()
    for (cx, cy, r) in ((8, 9, 3.6), (6, 7, 2.8), (10, 7, 2.8), (8, 5, 2.4), (7, 11, 2.4)):
        disc(im, cx, cy, r, c); disc(im, cx-.6, cy-.6, r*.55, sh(c, 1.2))
    r = random.Random(4)
    for _ in range(7): px(im, r.randint(4, 12), r.randint(3, 13), hair)
    px(im, 8, 14, (110, 86, 50, 255)); px(im, 8, 15, (110, 86, 50, 255))
    return outline(im)
def pod(c=(120, 150, 96, 255)):
    im = new(); disc(im, 8, 9, 4.2, c); disc(im, 7, 8, 2.2, sh(c, 1.2))
    for k in (5, 8, 11): line(im, k, 6, k, 12, sh(c, .78))
    rect(im, 5, 4, 11, 4, sh(c, .65)); rect(im, 6, 3, 10, 3, sh(c, .9)); px(im, 8, 2, sh(c, .7))
    rect(im, 8, 13, 8, 15, (96, 120, 70, 255))
    return outline(im)
def gum(c=(110, 70, 36, 255)):
    im = new()
    for (cx, cy, r) in ((8, 9, 4.2), (5, 10, 2.6), (11, 10, 2.8), (9, 6, 2.4)): disc(im, cx, cy, r, c)
    disc(im, 7, 8, 1.8, sh(c, 1.35)); speckle(im, 5, [sh(c, .7), sh(c, 1.6)], 6)
    return outline(im)
def seeds(c, pouch=(150, 118, 74, 255)):
    im = new(); ellipse(im, 8, 10, 4.4, 4.2, pouch); ellipse(im, 7, 9, 2.4, 2.0, sh(pouch, 1.15))
    rect(im, 6, 4, 9, 5, sh(pouch, .8)); line(im, 5, 5, 10, 5, (200, 170, 100, 255)); line(im, 7, 3, 7, 4, (200, 170, 100, 255)); line(im, 9, 3, 9, 4, (200, 170, 100, 255))
    for (x, y) in ((6, 10), (8, 8), (9, 11), (7, 12), (10, 9), (5, 12)): px(im, x, y, c)
    return outline(im)
def brownie():
    im = new(); rect(im, 3, 6, 12, 12, (86, 52, 32, 255)); rect(im, 3, 5, 12, 6, (112, 70, 42, 255)); rect(im, 4, 4, 11, 4, (140, 90, 56, 255))
    rect(im, 3, 11, 12, 12, (58, 34, 22, 255))
    speckle(im, 7, [(86, 160, 56, 255), (110, 190, 70, 255), (60, 120, 40, 255)], 7)
    return outline(im)
def paper_stack():
    im = new()
    for i, c in enumerate(((230, 230, 222, 255), (240, 240, 232, 255), (248, 248, 240, 255))): rect(im, 3+i, 4+i*2, 11+i, 8+i*2, c)
    rect(im, 3, 11, 11, 11, (210, 210, 200, 255)); rect(im, 10, 12, 13, 14, (230, 200, 130, 255)); line(im, 5, 6, 9, 6, (200, 200, 190, 255))
    return outline(im)
def clipboard():
    im = new(); rect(im, 3, 2, 12, 14, (140, 96, 56, 255)); rect(im, 4, 4, 11, 13, (240, 240, 232, 255)); rect(im, 6, 1, 9, 3, (170, 170, 178, 255))
    for k in (6, 8, 10): line(im, 5, k, 10, k, (150, 150, 160, 255))
    line(im, 5, 12, 6, 11, (200, 50, 50, 255)); line(im, 6, 11, 8, 12, (200, 50, 50, 255)); line(im, 8, 12, 10, 9, (200, 50, 50, 255))
    return outline(im)
def kit(cross=(210, 40, 44, 255)):
    im = new(); rect(im, 2, 5, 13, 13, (238, 238, 232, 255)); rect(im, 2, 5, 13, 6, (210, 210, 205, 255)); rect(im, 5, 3, 10, 4, (120, 120, 128, 255))
    rect(im, 7, 7, 8, 12, cross); rect(im, 5, 9, 10, 10, cross); rect(im, 2, 13, 13, 13, (190, 190, 185, 255))
    return outline(im)

# --------------------------------------------------------------- plantas (blocos)
def crop(kind, stage, c1, c2, flower=None):
    im = new(); h = (4, 7, 11, 14)[stage]; stem = (92, 130, 52, 255)
    if kind == "mushroom":
        n = (1, 2, 3, 5)[stage]; spots = [(4, 13), (8, 14), (11, 12), (6, 10), (10, 9)]
        for i in range(n):
            x, y = spots[i]; s = 1 + (stage >= 2) + (i == 0 and stage == 3)
            rect(im, x, y-s, x, y, (228, 214, 190, 255)); ellipse(im, x, y-s-0.4, s+1.4, s*.7+1.0, c1); px(im, x-1, y-s-1, (240, 240, 235, 255))
        return im
    top = 15 - h
    rect(im, 8, top, 8, 15, stem)
    if kind == "jagube":
        for y in range(top, 16): px(im, 8 + int(math.sin(y*0.9)*1.6), y, stem)
    leaves = [1, 2, 3, 4][stage]
    for i in range(leaves):
        y = 14 - i*max(2, h//leaves); side = -1 if i % 2 == 0 else 1; ln = 2 + stage + (1 if kind in ("tobacco", "chacrona") else 0)
        if kind == "cannabis":
            for a in (-35, 0, 35):
                tx = 8 + side*int(ln*math.cos(math.radians(a))); ty = y - int(ln*math.sin(math.radians(a)))
                line(im, 8, y, tx, ty, c1)
            px(im, 8+side*ln, y, c2)
        elif kind == "poppy":
            line(im, 8, y, 8+side*ln, y-1, c1); px(im, 8+side*ln, y-1, c2)
        elif kind in ("tobacco", "chacrona"):
            ellipse(im, 8+side*(ln//2+1), y-1, ln/2+1, 1.4+stage*.3, c1); px(im, 8+side*(ln//2+1), y-1, c2)
        elif kind == "coca":
            ellipse(im, 8+side*(ln//2+1), y, ln/2+.6, 1.2, c1); px(im, 8+side*ln, y, c2)
        else:
            ellipse(im, 8+side*2, y, 1.6, 1.3, c1)
    if stage == 3 and flower:
        if kind == "cannabis": disc(im, 8, top, 2.2, flower); px(im, 7, top-1, sh(flower, 1.3)); px(im, 9, top+1, (236, 140, 50, 255))
        elif kind == "poppy": disc(im, 8, top, 2.4, flower); disc(im, 8, top, 1.0, (40, 30, 50, 255))
        elif kind == "tobacco": rect(im, 7, top-1, 9, top+1, flower); px(im, 8, top-2, sh(flower, 1.3))
        elif kind == "coca": px(im, 8, top-1, flower); px(im, 7, top, flower); px(im, 9, top, flower)
        elif kind == "chacrona": px(im, 8, top-1, flower); px(im, 7, top, sh(flower, .8))
        elif kind == "jagube": disc(im, 8, top, 1.6, flower)
    return im

# ------------------------------------------------------------------ blocos
def tex_noise(base, seed, var=0.10, n=16):
    r = random.Random(seed); im = new(n)
    for y in range(n):
        for x in range(n): px(im, x, y, sh(base, 1 + r.uniform(-var, var)))
    return im
def planks(base, seed, dark=.78):
    im = tex_noise(base, seed, .06)
    for y in (0, 5, 10, 15): line(im, 0, y, 15, y, sh(base, dark))
    r = random.Random(seed+1)
    for _ in range(5): px(im, r.randint(0, 15), r.randint(0, 15), sh(base, .7))
    for (x, y0) in ((4, 1), (11, 6), (6, 11)): line(im, x, y0, x, y0+3, sh(base, dark))
    return im
def metal(base, seed, rivets=True):
    im = tex_noise(base, seed, .05)
    rect(im, 0, 0, 15, 0, sh(base, 1.25)); rect(im, 0, 15, 15, 15, sh(base, .6)); rect(im, 0, 0, 0, 15, sh(base, 1.12)); rect(im, 15, 0, 15, 15, sh(base, .7))
    if rivets:
        for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)): px(im, x, y, sh(base, 1.5))
    return im
def stone(base, seed):
    im = tex_noise(base, seed, .09)
    for y in (0, 7): line(im, 0, y, 15, y, sh(base, .7))
    for (x, y0, y1) in ((4, 0, 6), (11, 0, 6), (8, 7, 14), (1, 7, 14), (15, 7, 14)): line(im, x, y0, x, y1, sh(base, .7))
    line(im, 0, 15, 15, 15, sh(base, .7)); return im

def crate_side():
    im = planks((156, 112, 66, 255), 21)
    for (x0, y0, x1, y1) in ((0, 0, 15, 1), (0, 14, 15, 15), (0, 0, 1, 15), (14, 0, 15, 15)): rect(im, x0, y0, x1, y1, (96, 70, 44, 255))
    line(im, 2, 2, 13, 13, (110, 80, 50, 255)); line(im, 13, 2, 2, 13, (110, 80, 50, 255))
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)): px(im, x, y, (190, 190, 196, 255))
    return im
def crate_top():
    im = planks((170, 126, 76, 255), 22)
    for (x0, y0, x1, y1) in ((0, 0, 15, 1), (0, 14, 15, 15), (0, 0, 1, 15), (14, 0, 15, 15)): rect(im, x0, y0, x1, y1, (96, 70, 44, 255))
    return im

def table_top():
    im = planks((176, 132, 80, 255), 31)
    line(im, 5, 0, 5, 15, (92, 66, 40, 255)); line(im, 10, 0, 10, 15, (92, 66, 40, 255)); line(im, 0, 5, 15, 5, (92, 66, 40, 255)); line(im, 0, 10, 15, 10, (92, 66, 40, 255))
    rect(im, 6, 6, 9, 9, (70, 120, 70, 255)); px(im, 7, 7, (130, 190, 90, 255)); return im
def table_side():
    im = planks((120, 84, 52, 255), 32)
    rect(im, 2, 3, 6, 4, (190, 190, 200, 255)); line(im, 3, 5, 3, 8, (130, 130, 140, 255)); rect(im, 9, 4, 13, 6, (80, 140, 80, 255)); px(im, 11, 5, (150, 210, 110, 255))
    return im
def table_front(on=False):
    im = table_side(); rect(im, 3, 7, 12, 12, (46, 36, 28, 255)); rect(im, 4, 8, 11, 11, (255, 200, 90, 255) if on else (200, 170, 110, 255))
    line(im, 4, 9, 11, 9, (120, 90, 50, 255))
    if on: rect(im, 5, 10, 10, 10, (255, 240, 170, 255))
    return im

def bench_top():
    im = metal((120, 126, 134, 255), 41); rect(im, 3, 3, 12, 12, (86, 92, 100, 255)); rect(im, 4, 4, 11, 11, (60, 110, 96, 255))
    for (x, y, c) in ((5, 5, (230, 90, 90, 255)), (8, 6, (90, 190, 240, 255)), (10, 9, (240, 220, 90, 255))): disc(im, x, y, 1.2, c)
    return im
def bench_side():
    im = metal((98, 104, 112, 255), 42); rect(im, 3, 5, 12, 6, (70, 76, 84, 255)); rect(im, 3, 10, 12, 11, (70, 76, 84, 255)); return im
def bench_front(on=False):
    im = bench_side(); rect(im, 3, 3, 12, 12, (30, 34, 40, 255))
    liq = (90, 230, 160, 255) if on else (50, 120, 90, 255); rect(im, 4, 5, 11, 11, (20, 40, 36, 255)); rect(im, 5, 8, 10, 11, liq)
    if on:
        for (x, y) in ((6, 6), (8, 5), (9, 7)): px(im, x, y, (200, 255, 220, 255))
    return im

def dryer_top():
    im = planks((150, 112, 70, 255), 51)
    for x in range(1, 15, 3): line(im, x, 1, x, 14, (60, 44, 28, 255))
    return im
def dryer_side():
    im = tex_noise((140, 104, 64, 255), 52, .06)
    for y in range(1, 15, 3): line(im, 1, y, 14, y, (70, 50, 32, 255))
    for x in (1, 14): line(im, x, 0, x, 15, (88, 64, 40, 255))
    return im
def dryer_front(on=False):
    im = dryer_side(); rect(im, 2, 2, 13, 13, (36, 28, 20, 255))
    for x in (4, 7, 10):
        line(im, x, 3, x, 12, (80, 60, 40, 255)); rect(im, x-1, 6, x+1, 10, (150, 170, 60, 255) if on else (84, 150, 60, 255))
    if on: rect(im, 3, 12, 12, 12, (255, 170, 70, 255))
    return im

def grinder_top():
    im = metal((112, 112, 118, 255), 61); disc(im, 8, 8, 5.6, (64, 64, 70, 255)); disc(im, 8, 8, 4, (140, 140, 148, 255)); disc(im, 8, 8, 1.6, (40, 40, 44, 255))
    for a in range(0, 360, 45): px(im, 8+int(round(math.cos(math.radians(a))*5)), 8+int(round(math.sin(math.radians(a))*5)), (60, 60, 66, 255))
    return im
def grinder_side():
    im = metal((100, 100, 108, 255), 62)
    for x in range(2, 14, 2): line(im, x, 4, x, 11, (70, 70, 76, 255))
    return im
def grinder_front(on=False):
    im = grinder_side(); rect(im, 4, 6, 11, 12, (30, 30, 34, 255)); rect(im, 5, 7, 10, 11, (190, 120, 50, 255) if on else (70, 70, 76, 255))
    return im

def furn_top(): return stone((104, 100, 98, 255), 71)
def furn_side(): return stone((120, 116, 112, 255), 72)
def furn_front(on=False):
    im = furn_side(); rect(im, 3, 6, 12, 13, (24, 22, 22, 255)); rect(im, 4, 7, 11, 12, (255, 160, 50, 255) if on else (50, 44, 42, 255))
    if on: rect(im, 5, 9, 10, 12, (255, 220, 90, 255))
    rect(im, 3, 3, 12, 4, (60, 56, 54, 255))
    return im

# ------------------------------------------------------------------ icones
def effect_icon(item_img, bg):
    im = new(18)
    for y in range(18):
        for x in range(18):
            d = min(x, y, 17-x, 17-y); px(im, x, y, sh(bg, 0.55 + 0.1*min(d, 3)))
    for k in range(18):
        for (x, y) in ((k, 0), (k, 17), (0, k), (17, k)): px(im, x, y, (20, 16, 18, 255))
    im.paste(item_img, (1, 1), item_img); return im

def mod_icon():
    im = Image.new("RGBA", (128, 128), (28, 22, 36, 255)); d = ImageDraw.Draw(im)
    for r in range(60, 0, -4):
        c = (28+int((60-r)*1.1), 22+int((60-r)*0.9), 36+int((60-r)*1.7), 255); d.ellipse((64-r, 64-r, 64+r, 64+r), fill=c)
    leaf = cannabis_leaf().resize((96, 96), Image.NEAREST); im.paste(leaf, (16, 12), leaf)
    p = pill((255, 90, 150, 255), (250, 250, 250, 255), "cap").resize((48, 48), Image.NEAREST); im.paste(p, (70, 70), p)
    return im
