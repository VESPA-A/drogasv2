#!/usr/bin/env python3
"""Sintetiza os sons do mod (numpy/scipy) e codifica em OGG Vorbis mono via ffmpeg. Gera sounds.json e a lista Java."""
import numpy as np, subprocess, os, json, tempfile
from scipy import signal
SR = 44100
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
OUT = os.path.join(ROOT, "src/main/resources/assets/drogas")
rng = np.random.default_rng(2026)

def t(d): return np.linspace(0, d, int(SR*d), endpoint=False)
def noise(d): return rng.uniform(-1, 1, int(SR*d))
def lp(x, f, o=2): return signal.lfilter(*signal.butter(o, f/(SR/2), "low"), x)
def hp(x, f, o=2): return signal.lfilter(*signal.butter(o, f/(SR/2), "high"), x)
def bp(x, lo, hi, o=2): return signal.lfilter(*signal.butter(o, [lo/(SR/2), hi/(SR/2)], "band"), x)
def env(n, a, d):  # ataque/decaimento exponencial em fracao
    e = np.ones(n); na = max(1, int(n*a)); e[:na] = np.linspace(0, 1, na)
    e[na:] = np.exp(-np.linspace(0, d, n-na)); return e
def fade(x, ms=8):
    n = int(SR*ms/1000); x = x.copy(); x[:n] *= np.linspace(0, 1, n); x[-n:] *= np.linspace(1, 0, n); return x
def norm(x, peak=0.8): return x / (np.max(np.abs(x)) + 1e-9) * peak
def sine(f, d, ph=0): return np.sin(2*np.pi*f*t(d) + ph)
def sweep(f0, f1, d): 
    tt = t(d); f = np.linspace(f0, f1, len(tt)); return np.sin(2*np.pi*np.cumsum(f)/SR)
def impulses(d, rate, seed=0):
    r = np.random.default_rng(seed); x = np.zeros(int(SR*d)); n = int(rate*d)
    for i in r.integers(0, len(x)-400, n): x[i:i+200] += r.uniform(.3, 1) * np.exp(-np.arange(200)/25) * r.choice([-1, 1])
    return x

S = {}
def pill():
    x = np.zeros(int(SR*.4))
    for st, f in ((0, 3600), (.09, 4200), (.17, 3100), (.26, 3900)):
        i = int(SR*st); d = np.exp(-np.arange(int(SR*.06))/(SR*.008)) * np.sin(2*np.pi*f*t(.06)); x[i:i+len(d)] += d*.7
    return x + hp(noise(.4), 2500)*0.04*np.exp(-t(.4)*10)
S["item/pill"] = pill()
S["item/swallow"] = lp(sweep(240, 110, .28)*env(int(SR*.28), .1, 6) + noise(.28)*.08, 900)
def rustle():
    x = np.zeros(int(SR*.55))
    for st in (0, .07, .15, .22, .33): 
        i = int(SR*st); d = bp(noise(.09), 1800, 7000)*np.exp(-np.arange(int(SR*.09))/(SR*.02)); x[i:i+len(d)] += d
    return x
S["item/rustle"] = rustle()
S["item/smoke_puff"] = bp(noise(.9), 900, 3200)*np.sin(np.linspace(0, np.pi, int(SR*.9)))**1.5*.8 + impulses(.9, 25, 3)*.12
S["item/exhale"] = lp(noise(1.5), 1400)*np.sin(np.linspace(0, np.pi, int(SR*1.5)))**1.2 + bp(noise(1.5), 2000, 5000)*.1*env(int(SR*1.5), .2, 2.5)
sn = hp(noise(.28), 1500)*np.sin(np.linspace(0, np.pi, int(SR*.28)))**.7; S["item/tick_puff"] = bp(noise(.14), 1200, 4500)*np.sin(np.linspace(0, np.pi, int(SR*.14)))**1.2*.8
S["item/snort"] = sn + .3*sweep(900, 1900, .28)*env(int(SR*.28), .05, 7)
inj = np.zeros(int(SR*.6)); inj[:300] = np.exp(-np.arange(300)/40)*np.sign(np.sin(np.arange(300)*.6)); inj[2000:] += (hp(noise(.6), 3000)*env(int(SR*.6), .02, 6))[2000:]*.4; inj += lp(sine(70, .6), 200)*np.exp(-t(.6)*14)*.5
S["item/inject"] = inj
lg = np.zeros(int(SR*.6)); lg[:200] = np.sign(np.sin(np.arange(200)*.9))*np.exp(-np.arange(200)/30); lg += lp(noise(.6), 2500)*env(int(SR*.6), .15, 4)*.5*(np.arange(int(SR*.6)) > int(SR*.12)); S["item/lighter"] = lg
tt = t(1.6); S["machine/hum"] = (np.sin(2*np.pi*70*tt) + .6*np.sin(2*np.pi*140*tt + .4*np.sin(2*np.pi*3*tt)) + .3*np.sin(2*np.pi*210*tt))*.5
g = noise(1.0)*(0.6+0.4*np.sin(2*np.pi*9*t(1.0)))**2; S["machine/grind"] = bp(g, 500, 2800)*.9 + lp(noise(1.0), 160)*.8
def bubbles():
    x = np.zeros(int(SR*1.3)); r = np.random.default_rng(5)
    for st in sorted(r.uniform(0, 1.1, 9)):
        i = int(SR*st); d = .09 + r.uniform(0, .05); b = sweep(r.uniform(250, 450), r.uniform(700, 1100), d)*np.exp(-t(d)*22); x[i:i+len(b)] += b*r.uniform(.4, 1)
    return x
S["machine/bubble"] = bubbles()
S["machine/fire"] = lp(noise(1.3), 300)*.25 + hp(impulses(1.3, 40, 8), 800)*.9
S["machine/fan"] = bp(noise(1.5), 700, 3200)*(0.55+0.45*np.sin(2*np.pi*1.5*t(1.5)))*.6
S["machine/done"] = (sine(880, .6)*np.exp(-t(.6)*7) + .7*np.sine(1320, .6)*0 if False else sine(880, .6)*np.exp(-t(.6)*7) + .7*np.concatenate([np.zeros(int(SR*.12)), sine(1320, .48)*np.exp(-t(.48)*7)]))
brk = bp(noise(.7), 300, 4000)*np.exp(-t(.7)*18) + sine(180, .7)*np.exp(-t(.7)*10) + .4*sine(620, .7)*np.exp(-t(.7)*8); S["machine/break"] = brk
def heartbeat():
    x = np.zeros(int(SR*.9))
    for st, a in ((0, 1.0), (.28, .7)):
        i = int(SR*st); d = lp(sine(58, .22), 120)*np.exp(-t(.22)*16)*a; x[i:i+len(d)] += d
    return x
S["effect/heartbeat"] = heartbeat()
def whisper():
    n = noise(1.9); sig = np.zeros_like(n); 
    for fc, bw in ((500, 120), (1500, 220), (2500, 300)): sig += bp(n, fc-bw, fc+bw)*(0.5+0.5*np.sin(2*np.pi*(.7+fc/9000)*t(1.9)+fc))
    return sig*np.sin(np.linspace(0, np.pi, len(sig)))**1.3
S["effect/whisper"] = whisper()
S["effect/tinnitus"] = (sine(6500, 2.0) + .6*sine(7300, 2.0))*np.sin(np.linspace(0, np.pi, int(SR*2.0)))**2*.35
S["effect/warp"] = sweep(200, 1250, .75).tolist() and np.concatenate([sweep(200, 1250, .75), sweep(1250, 260, .75)])*(0.7+0.3*np.sin(2*np.pi*8*t(1.5)))
S["effect/breath_slow"] = lp(noise(2.0), 1000)*np.sin(np.linspace(0, 2*np.pi, int(SR*2.0)) - np.pi/2)**2
al = np.zeros(int(SR*.9)); 
for st in (0, .3, .6): i = int(SR*st); d = np.sign(sine(110, .2))*.6*np.exp(-t(.2)*6) + sine(220, .2)*.3*np.exp(-t(.2)*6); al[i:i+len(d)] += lp(d, 700)
S["effect/alarm"] = al
S["effect/shiver"] = lp(noise(.8), 900)*(0.5+0.5*np.sin(2*np.pi*22*t(.8)))*np.sin(np.linspace(0, np.pi, int(SR*.8)))
ch = np.zeros(int(SR*1.0))
for st, f in ((0, 523), (.11, 659), (.22, 784), (.33, 1047)):
    i = int(SR*st); d = sine(f, .6)*np.exp(-t(.6)*6) + .4*sine(f*2, .6)*np.exp(-t(.6)*9); ch[i:i+len(d)] += d*.5
S["ui/discover"] = ch
S["ui/page"] = hp(noise(.3), 1200)*np.sin(np.linspace(0, np.pi, int(SR*.3)))**.6*.7
S["block/wither"] = bp(noise(.55), 1200, 6000)*np.exp(-t(.55)*5) * (0.5+0.5*np.sin(2*np.pi*14*t(.55)))
S["block/harvest"] = bp(noise(.4), 1500, 6500)*np.exp(-t(.4)*9) + lp(sweep(500, 220, .12), 600).tolist().__len__()*0 if False else bp(noise(.4), 1500, 6500)*np.exp(-t(.4)*9)

VOL = {"item/pill": .9, "item/tick_puff": .5, "item/swallow": .8, "item/rustle": .7, "item/smoke_puff": .8, "item/exhale": .8, "item/snort": .8, "item/inject": .8,
       "item/lighter": .8, "machine/hum": .45, "machine/grind": .6, "machine/bubble": .6, "machine/fire": .5, "machine/fan": .5,
       "machine/done": .7, "machine/break": .8, "effect/heartbeat": .9, "effect/whisper": .6, "effect/tinnitus": .4, "effect/warp": .5,
       "effect/breath_slow": .6, "effect/alarm": .8, "effect/shiver": .6, "ui/discover": .7, "ui/page": .6, "block/wither": .6, "block/harvest": .6}
SUB = {"item/tick_puff": "Alguém inala", "item/pill": "Comprimido chacoalha", "item/swallow": "Alguém engole", "item/rustle": "Papel amassa", "item/smoke_puff": "Alguém traga",
       "item/exhale": "Alguém solta fumaça", "item/snort": "Alguém cheira", "item/inject": "Injeção", "item/lighter": "Isqueiro acende",
       "machine/hum": "Máquina zumbe", "machine/grind": "Triturador funciona", "machine/bubble": "Líquido borbulha", "machine/fire": "Fornalha crepita",
       "machine/fan": "Secador sopra", "machine/done": "Processo concluído", "machine/break": "Máquina quebra", "effect/heartbeat": "Coração acelera",
       "effect/whisper": "Sussurros", "effect/tinnitus": "Zumbido nos ouvidos", "effect/warp": "Distorção", "effect/breath_slow": "Respiração lenta",
       "effect/alarm": "Alarme de overdose", "effect/shiver": "Tremor", "ui/discover": "Descoberta", "ui/page": "Página vira",
       "block/wither": "Planta murcha", "block/harvest": "Colheita"}

def build():
    names = []; sj = {}
    for key, x in S.items():
        x = np.asarray(x, dtype=float); x = fade(norm(x, .85)); path = os.path.join(OUT, "sounds", key + ".ogg"); os.makedirs(os.path.dirname(path), exist_ok=True)
        wav = tempfile.mktemp(suffix=".wav")
        import scipy.io.wavfile as wf; wf.write(wav, SR, (x*32767).astype(np.int16))
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-ac", "1", "-c:a", "libvorbis", "-q:a", "3", path], check=True); os.remove(wav)
        ev = key.replace("/", "."); names.append(ev)
        sj[ev] = {"sounds": [{"name": f"drogas:{key}", "volume": VOL[key]}], "subtitle": f"subtitles.drogas.{ev}"}
    with open(os.path.join(OUT, "sounds.json"), "w", encoding="utf-8") as f: json.dump(sj, f, indent=2)
    return names, {f"subtitles.drogas.{k.replace('/', '.')}": v for k, v in SUB.items()}

if __name__ == "__main__":
    n, sub = build(); print(len(n), "sons gerados;", sum(os.path.getsize(os.path.join(dp, f)) for dp, _, fs in os.walk(os.path.join(OUT, "sounds")) for f in fs)//1024, "KB")
