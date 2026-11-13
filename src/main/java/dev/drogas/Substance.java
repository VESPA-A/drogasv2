package dev.drogas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Cada substancia tem identidade propria: grupo, duracao, tolerancia, dependencia, abstinencia, overdose,
 * atributos de gameplay e perfil visual/sonoro. Todos os valores sao fictícios e voltados a jogabilidade.
 */
public enum Substance {
    CANNABIS("cannabis", Group.CANNABINOID, p -> p.dur(3600).amp(0.3f, 0.12f).tol(0.06f, 0.6f).dep(0.03f, 0.3f, 0.35f, 7200, 0.15f)
            .load(1f, 0.02f, 14f, false).speed(-0.06).atkSpeed(-0.05).heal(0.5f).hunger(0.04f)
            .visual(0x6fbf4a, 0.12f, 0.22f, 0.5f, 0f, 0f, 0f, false, "none").withdraw("mining_fatigue")),
    COCAINE("cocaine", Group.STIMULANT, p -> p.dur(1800).amp(1.0f, 0.35f).tol(0.12f, 0.6f).dep(0.10f, 0.25f, 0.30f, 6000, 0.12f)
            .load(1f, 0.04f, 4f, true).speed(0.14).atkSpeed(0.10).mining(0.15).hunger(0.08f).strain(0.8f)
            .visual(0xdfeaff, 0.06f, 0.35f, 0f, 0.25f, 4f, 1.8f, false, "none").sound("drogas:effect.heartbeat", 80, 0.7f)
            .withdraw("slowness", "weakness")),
    CRACK("crack", Group.STIMULANT, p -> p.dur(900).amp(1.6f, 0.4f).tol(0.20f, 0.5f).dep(0.20f, 0.15f, 0.15f, 4200, 0.08f)
            .load(1f, 0.05f, 3f, true).speed(0.20).damage(0.5).atkSpeed(0.08).hunger(0.12f).strain(1.0f)
            .visual(0xb04a3a, 0.12f, 0.5f, 0f, 0.6f, 6f, 2.4f, false, "none").sound("drogas:effect.heartbeat", 50, 0.9f)
            .withdraw("weakness", "slowness", "hunger", "nausea")),
    LSD("lsd", Group.PSYCHEDELIC, p -> p.dur(7200).amp(1.0f, 0.2f).tol(0.25f, 1.0f).dep(0f, 0.3f, 1f, 99999, 0.1f)
            .load(1f, 0.02f, 6f, false).reach(0.6)
            .visual(0xc06bff, 0.22f, 0.12f, 2.4f, 0f, 0f, 0.25f, true, "end_rod").sound("drogas:effect.whisper", 300, 0.8f)),
    PSILOCYBIN("psilocybin", Group.PSYCHEDELIC, p -> p.dur(4800).amp(0.8f, 0.2f).tol(0.22f, 0.9f).dep(0f, 0.3f, 1f, 99999, 0.1f)
            .load(1f, 0.02f, 8f, false).blockReach(0.4).extra("nausea:120:0")
            .visual(0xe8a23a, 0.16f, 0.15f, 1.8f, 0f, -2f, 0.35f, false, "witch").sound("drogas:effect.whisper", 360, 0.7f)),
    OPIUM("opium", Group.OPIOID, p -> p.dur(4800).amp(0.5f, 0.25f).tol(0.10f, 0.5f).dep(0.08f, 0.2f, 0.30f, 7200, 0.10f)
            .load(1f, 0.02f, 5f, true).speed(-0.10).armor(2.0).kb(0.1).heal(0.5f)
            .visual(0xb0793a, 0.10f, 0.4f, 0.8f, 0f, -3f, 0.2f, false, "none").sound("drogas:effect.breath_slow", 120, 0.6f)
            .withdraw("weakness", "hunger")),
    MORPHINE("morphine", Group.OPIOID, p -> p.dur(3600).amp(1.0f, 0.3f).tol(0.15f, 0.5f).dep(0.14f, 0.15f, 0.25f, 6000, 0.09f)
            .load(1f, 0.02f, 4f, true).speed(-0.08).armor(3.0).kb(0.15).heal(1.0f)
            .visual(0x7aa7c7, 0.09f, 0.35f, 0.6f, 0f, -2f, 0f, false, "none").sound("drogas:effect.breath_slow", 120, 0.6f)
            .withdraw("slowness", "weakness", "hunger")),
    HEROIN("heroin", Group.OPIOID, p -> p.dur(3000).amp(1.8f, 0.4f).tol(0.20f, 0.4f).dep(0.25f, 0.10f, 0.12f, 4800, 0.06f)
            .load(1f, 0.025f, 3f, true).speed(-0.16).armor(4.0).kb(0.2).heal(0.5f)
            .visual(0x8a5a3a, 0.14f, 0.6f, 1.0f, 0f, -6f, 0.2f, false, "none").sound("drogas:effect.breath_slow", 100, 0.7f)
            .withdraw("weakness", "slowness", "nausea", "hunger", "mining_fatigue")),
    METH("meth", Group.STIMULANT, p -> p.dur(3600).amp(1.7f, 0.35f).tol(0.16f, 0.45f).dep(0.18f, 0.15f, 0.18f, 5400, 0.08f)
            .load(1f, 0.02f, 4f, true).speed(0.18).atkSpeed(0.15).damage(0.5).mining(0.2).hunger(0.10f).strain(1.0f)
            .visual(0x7fe3e0, 0.08f, 0.3f, 0f, 0.5f, 5f, 2.0f, false, "none").sound("drogas:effect.heartbeat", 60, 0.8f)
            .withdraw("slowness", "weakness", "mining_fatigue")),
    MDMA("mdma", Group.ENTACTOGEN, p -> p.dur(4800).amp(1.0f, 0.25f).tol(0.18f, 0.5f).dep(0.05f, 0.2f, 0.5f, 9000, 0.15f)
            .load(1f, 0.015f, 5f, true).speed(0.08).heal(0.5f).hunger(0.05f)
            .visual(0xff5fb4, 0.14f, 0.1f, 0.5f, 0f, 3f, 1.2f, false, "note").withdraw("weakness")),
    KETAMINE("ketamine", Group.DISSOCIATIVE, p -> p.dur(2400).amp(1.0f, 0.3f).tol(0.14f, 0.5f).dep(0.06f, 0.2f, 0.45f, 7200, 0.12f)
            .load(1f, 0.03f, 4f, true).speed(-0.08).fall(4.0)
            .visual(0x8a6bd6, 0.12f, 0.5f, 2.0f, 0f, -8f, 0.15f, false, "none").sound("drogas:effect.warp", 200, 0.5f)
            .withdraw("weakness")),
    DMT("dmt", Group.PSYCHEDELIC, p -> p.dur(1800).amp(2.6f, 0.3f).tol(0.30f, 1.0f).dep(0f, 0.3f, 1f, 99999, 0.1f)
            .load(1f, 0.03f, 3f, false).speed(-0.25)
            .visual(0x4fe0a0, 0.32f, 0.2f, 3.5f, 0f, 12f, 0.8f, true, "end_rod").sound("drogas:effect.warp", 100, 0.6f)),
    AYAHUASCA("ayahuasca", Group.PSYCHEDELIC, p -> p.dur(9600).amp(1.0f, 0.2f).tol(0.08f, 0.6f).dep(0f, 0.3f, 1f, 99999, 0.1f)
            .load(1f, 0.01f, 4f, false).hunger(0.03f).reach(0.5).extra("nausea:300:0").extra("night_vision:3600:0")
            .visual(0x6b8f2e, 0.18f, 0.2f, 2.0f, 0f, 0f, 0.3f, false, "happy").sound("drogas:effect.whisper", 300, 0.7f)),
    TOBACCO("tobacco", Group.STIMULANT, p -> p.dur(3600).amp(0.2f, 0.1f).tol(0.08f, 0.5f).dep(0.12f, 0.15f, 0.20f, 7200, 0.10f)
            .load(1f, 0.015f, 10f, true).mining(0.10).atkSpeed(0.05).hunger(0.02f)
            .visual(0xa08a6a, 0.04f, 0.05f, 0f, 0f, 0f, 0f, false, "none").withdraw("mining_fatigue", "weakness")),
    ALCOHOL("alcohol", Group.DEPRESSANT, p -> p.dur(4800).amp(0f, 0.9f).tol(0.07f, 0.4f).dep(0.06f, 0.1f, 0.35f, 6000, 0.08f)
            .load(1f, 0.03f, 7f, true).speed(-0.06).damage(0.5).kb(0.05)
            .visual(0xd99a4a, 0.08f, 0.3f, 3.0f, 0f, -2f, 0.2f, false, "none").sound("entity.player.burp", 900, 0.6f)
            .withdraw("nausea", "weakness", "hunger")),
    NEBULON("nebulon", Group.HYBRID, p -> p.dur(6000).amp(1.2f, 0.3f).tol(0.20f, 0.5f).dep(0.15f, 0.18f, 0.25f, 6000, 0.10f)
            .load(1f, 0.025f, 4f, true).speed(0.12).jump(0.06).fall(6.0).hunger(0.08f)
            .visual(0x4aa8ff, 0.16f, 0.1f, 1.2f, 0.2f, 8f, 1.0f, false, "end_rod").withdraw("slowness", "nausea")),
    ZYRAX("zyrax", Group.HYBRID, p -> p.dur(3000).amp(1.5f, 0.3f).tol(0.25f, 0.4f).dep(0.20f, 0.15f, 0.20f, 5400, 0.08f)
            .load(1f, 0.03f, 3f, true).speed(-0.15).armor(5.0).kb(0.3).damage(1.5).hunger(0.06f)
            .visual(0xd6344a, 0.14f, 0.55f, 0f, 0.3f, -10f, 1.4f, false, "none").withdraw("weakness", "mining_fatigue", "slowness"));

    public enum Group {
        STIMULANT, DEPRESSANT, OPIOID, PSYCHEDELIC, DISSOCIATIVE, ENTACTOGEN, CANNABINOID, HYBRID;

        public boolean depressantFamily() { return this == DEPRESSANT || this == OPIOID || this == DISSOCIATIVE; }
        public boolean stimulantFamily() { return this == STIMULANT || this == ENTACTOGEN || this == HYBRID; }
    }

    /** Efeito vanilla extra aplicado ao consumir ("id:ticks:amplificador"). */
    public record Extra(String path, int ticks, int amp) {}

    private static final class Props {
        int dur = 3600; float baseAmp = 1f, stackAmp = 0f;
        float tolGain = 0.1f, tolDecayMin = 0.5f;
        float depGain = 0f, depDecayMin = 0.2f, depThr = 0.3f; int onset = 6000; float recoveryMin = 0.1f;
        float loadPerDose = 1f, loadDecaySec = 0.03f, od = 5f; boolean lethal = true;
        float hunger = 0f, heal = 0f, strain = 0f;
        double speed, atkSpeed, mining, damage, armor, kb, jump, reach, blockReach, fall;
        int tint = 0xffffff; float tintAlpha, vignette, sway, shake, fov, pulse; boolean rainbow; String particle = "none";
        String sound = ""; int soundEvery = 0; float soundVol = 0.6f;
        final List<Extra> extras = new ArrayList<>(); final List<String> withdraw = new ArrayList<>();

        Props dur(int v) { dur = v; return this; }
        Props amp(float base, float stack) { baseAmp = base; stackAmp = stack; return this; }
        Props tol(float gain, float decayMin) { tolGain = gain; tolDecayMin = decayMin; return this; }
        Props dep(float gain, float decayMin, float thr, int onsetTicks, float recMin) { depGain = gain; depDecayMin = decayMin; depThr = thr; onset = onsetTicks; recoveryMin = recMin; return this; }
        Props load(float perDose, float decaySec, float odThreshold, boolean isLethal) { loadPerDose = perDose; loadDecaySec = decaySec; od = odThreshold; lethal = isLethal; return this; }
        Props hunger(float v) { hunger = v; return this; }
        Props heal(float v) { heal = v; return this; }
        Props strain(float v) { strain = v; return this; }
        Props speed(double v) { speed = v; return this; }
        Props atkSpeed(double v) { atkSpeed = v; return this; }
        Props mining(double v) { mining = v; return this; }
        Props damage(double v) { damage = v; return this; }
        Props armor(double v) { armor = v; return this; }
        Props kb(double v) { kb = v; return this; }
        Props jump(double v) { jump = v; return this; }
        Props reach(double v) { reach = v; return this; }
        Props blockReach(double v) { blockReach = v; return this; }
        Props fall(double v) { fall = v; return this; }
        Props visual(int rgb, float alpha, float vig, float swayDeg, float shakeDeg, float fovDeg, float pulseHz, boolean rb, String particleKind) {
            tint = rgb; tintAlpha = alpha; vignette = vig; sway = swayDeg; shake = shakeDeg; fov = fovDeg; pulse = pulseHz; rainbow = rb; particle = particleKind; return this;
        }
        Props sound(String path, int every, float vol) { sound = path; soundEvery = every; soundVol = vol; return this; }
        Props extra(String spec) { String[] a = spec.split(":"); extras.add(new Extra(a[0], Integer.parseInt(a[1]), Integer.parseInt(a[2]))); return this; }
        Props withdraw(String... ids) { for (String s : ids) withdraw.add(s); return this; }
    }

    public final String id; public final Group group;
    public final int durationTicks; public final float baseAmp, stackAmp;
    public final float tolGain, tolDecayPerMin;
    public final float depGain, depDecayPerMin, depThreshold; public final int withdrawalOnset; public final float recoveryPerMin;
    public final float loadPerDose, loadDecayPerSec, odThreshold; public final boolean lethal;
    public final float hunger, heal, strain;
    public final double speed, atkSpeed, mining, damage, armor, knockbackRes, jump, reach, blockReach, safeFall;
    public final int tint; public final float tintAlpha, vignette, sway, shake, fov, pulse; public final boolean rainbow; public final String particle;
    public final String sound; public final int soundEvery; public final float soundVolume;
    public final List<Extra> extras; public final List<String> withdrawalEffects;

    Substance(String id, Group group, Consumer<Props> cfg) {
        Props p = new Props(); cfg.accept(p);
        this.id = id; this.group = group;
        durationTicks = p.dur; baseAmp = p.baseAmp; stackAmp = p.stackAmp; tolGain = p.tolGain; tolDecayPerMin = p.tolDecayMin;
        depGain = p.depGain; depDecayPerMin = p.depDecayMin; depThreshold = p.depThr; withdrawalOnset = p.onset; recoveryPerMin = p.recoveryMin;
        loadPerDose = p.loadPerDose; loadDecayPerSec = p.loadDecaySec; odThreshold = p.od; lethal = p.lethal;
        hunger = p.hunger; heal = p.heal; strain = p.strain;
        speed = p.speed; atkSpeed = p.atkSpeed; mining = p.mining; damage = p.damage; armor = p.armor; knockbackRes = p.kb;
        jump = p.jump; reach = p.reach; blockReach = p.blockReach; safeFall = p.fall;
        tint = p.tint; tintAlpha = p.tintAlpha; vignette = p.vignette; sway = p.sway; shake = p.shake; fov = p.fov; pulse = p.pulse;
        rainbow = p.rainbow; particle = p.particle; sound = p.sound; soundEvery = p.soundEvery; soundVolume = p.soundVol;
        extras = List.copyOf(p.extras); withdrawalEffects = List.copyOf(p.withdraw);
    }

    /** Pode gerar dependencia/abstinencia? */
    public boolean addictive() { return depGain > 0f; }

    public String translationKey() { return "substance." + DrogasMod.MOD_ID + "." + id; }

    public static Substance byId(String id) {
        for (Substance s : values()) if (s.id.equals(id)) return s;
        return null;
    }

    /** Fator de sinergia perigosa entre duas substancias (soma parte da carga de uma na outra). */
    public static float synergy(Substance a, Substance b) {
        if (a == b) return 0f;
        if (a.group.depressantFamily() && b.group.depressantFamily()) return 0.6f;
        if (a.group.stimulantFamily() && b.group.stimulantFamily()) return 0.4f;
        return 0f;
    }
}
