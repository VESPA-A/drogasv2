package dev.drogas;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/** Logica de servidor: consumo, tolerancia, dependencia, abstinencia, overdose e recuperacao gradual. */
public final class DrugManager {
    private DrugManager() {}

    public static DrugState state(Player p) {
        return p.getAttachedOrElse(ModAttachments.DRUG_STATE, DrugState.EMPTY);
    }

    private static void save(ServerPlayer p, DrugState old, DrugState neu) {
        if (!neu.equals(old)) p.setAttached(ModAttachments.DRUG_STATE, neu);
    }

    private static Component name(Substance s) {
        return Component.translatable(s.translationKey());
    }

    // ------------------------------------------------------------------ consumo
    public static void consume(ServerPlayer p, Substance s, float dose) {
        consume(p, s, dose, Quality.STANDARD);
    }

    /** quality: 0 Ruim, 1 Padrao, 2 Boa, 3 Excelente (afeta potencia, duracao e impurezas). */
    public static void consume(ServerPlayer p, Substance s, float dose, int quality) {
        int qi = Mth.clamp(quality, 0, 3);
        DrogasConfig cfg = DrogasConfig.get();
        DrugState cur = state(p);
        DrugState.Mutable m = cur.mutable();
        long now = p.level().getGameTime();
        int i = s.ordinal();

        // tolerancia reduz a intensidade (ate 75%); doses seguidas empilham carga e nivel
        float effectiveness = Math.max(0.25f, 1f - m.tol[i] * 0.75f);
        m.load[i] += dose * s.loadPerDose * Quality.LOAD[qi];
        float a = (s.baseAmp * dose + m.load[i] * s.stackAmp) * effectiveness * cfg.intensityScale * Quality.POTENCY[qi];
        int amp = Mth.clamp((int) Math.floor(a + 0.45f), 0, 4);

        float maxDur = s.durationTicks * cfg.durationFor(s);
        int baseDur = Math.round(maxDur * dose * Quality.DURATION[qi] * (1f - 0.25f * m.tol[i]));
        int dur = baseDur;
        MobEffectInstance old = p.getEffect(ModEffects.of(s));
        if (old != null) {
            dur = Math.min(old.getDuration() + baseDur, Math.round(maxDur * 3f));
            amp = Math.max(old.getAmplifier(), amp);
            p.removeEffect(ModEffects.of(s));
        }
        p.addEffect(new MobEffectInstance(ModEffects.of(s), Math.max(40, dur), amp, false, true, true));
        for (Substance.Extra e : s.extras) Fx.give(p, e.path(), Math.max(20, Math.round(e.ticks() * dose)), e.amp());

        if (qi == Quality.POOR && p.getRandom().nextInt(4) == 0) {   // impurezas
            Fx.give(p, "nausea", 120, 0);
            Fx.give(p, "hunger", 200, 0);
            p.displayClientMessage(Component.translatable("message.drogas.impure").withStyle(ChatFormatting.GRAY), true);
        }

        // tolerancia e dependencia
        m.tol[i] = Math.min(1f, m.tol[i] + s.tolGain * dose * cfg.toleranceRate * (1f - m.tol[i] * 0.4f));
        if (s.addictive()) m.dep[i] = Math.min(1f, m.dep[i] + s.depGain * dose * cfg.dependenceRate * (0.6f + 0.8f * m.tol[i]));
        m.last[i] = now;

        if (m.wd[i] > 0) {
            m.wd[i] = 0;
            p.displayClientMessage(Component.translatable("message.drogas.relief").withStyle(ChatFormatting.GRAY), true);
        } else if (effectiveness < 0.6f) {
            p.displayClientMessage(Component.translatable("message.drogas.tolerance").withStyle(ChatFormatting.GRAY), true);
        }
        boolean anyWithdrawal = false;
        for (int w : m.wd) if (w > 0) anyWithdrawal = true;
        if (!anyWithdrawal) p.removeEffect(ModEffects.WITHDRAWAL);

        // overdose: carga propria + parte da carga de substancias com sinergia perigosa
        float danger = m.load[i];
        for (Substance o : Substance.values()) danger += m.load[o.ordinal()] * Substance.synergy(s, o);
        if (danger > s.odThreshold) {
            triggerOverdose(p, s, danger);
            m.load[i] = Math.min(m.load[i], s.odThreshold * 0.5f);
        }
        Knowledge.unlock(p, "sub." + s.id);
        if (m.tol[i] > 0.3f) Knowledge.unlock(p, "guide.tolerance");
        if (m.dep[i] > 0f) Knowledge.unlock(p, "guide.dependence");
        if (qi != Quality.STANDARD) Knowledge.unlock(p, "guide.quality");
        save(p, cur, m.freeze());
    }

    private static void triggerOverdose(ServerPlayer p, Substance s, float danger) {
        int amp = Mth.clamp((int) ((danger / s.odThreshold - 1f) * 4f), 0, 3);
        Knowledge.unlock(p, "guide.overdose");
        if (s.lethal) {
            p.displayClientMessage(Component.translatable("message.drogas.overdose", name(s)).withStyle(ChatFormatting.RED), false);
            p.addEffect(new MobEffectInstance(ModEffects.OVERDOSE, 600 + 200 * amp, amp, false, true, true));
            Fx.give(p, "nausea", 400, 0);
            Fx.give(p, "slowness", 400, 1);
            Fx.give(p, "darkness", 200, 0);
            Fx.sound(p, "entity.player.hurt", 1.0f, 0.6f);
        } else {
            p.displayClientMessage(Component.translatable("message.drogas.bad_trip", name(s)).withStyle(ChatFormatting.DARK_PURPLE), false);
            p.addEffect(new MobEffectInstance(ModEffects.BAD_TRIP, 900 + 300 * amp, amp, false, true, true));
            Fx.give(p, "nausea", 300, 0);
            Fx.give(p, "darkness", 160, 0);
            Fx.sound(p, "entity.enderman.stare", 0.8f, 0.7f);
        }
    }

    // ------------------------------------------------------------------ tratamento
    /** Kit de Emergência: interrompe overdose/bad trip e reduz a carga acumulada. */
    public static void emergency(ServerPlayer p) {
        p.removeEffect(ModEffects.OVERDOSE);
        p.removeEffect(ModEffects.BAD_TRIP);
        DrugState cur = state(p);
        DrugState.Mutable m = cur.mutable();
        for (int i = 0; i < DrugState.N; i++) m.load[i] *= 0.4f;
        save(p, cur, m.freeze());
        Fx.give(p, "weakness", 200, 0);
        p.displayClientMessage(Component.translatable("message.drogas.kit_used").withStyle(ChatFormatting.GREEN), true);
    }

    /** Tônico Desintoxicante: acelera a recuperação (reduz dependência e tolerância). */
    public static void detox(ServerPlayer p) {
        DrugState cur = state(p);
        DrugState.Mutable m = cur.mutable();
        for (int i = 0; i < DrugState.N; i++) {
            m.dep[i] = Math.max(0f, m.dep[i] - 0.10f);
            m.tol[i] = Math.max(0f, m.tol[i] - 0.08f);
        }
        save(p, cur, m.freeze());
        Fx.give(p, "hunger", 100, 0);
        p.displayClientMessage(Component.translatable("message.drogas.detox_used").withStyle(ChatFormatting.GREEN), true);
    }

    public static void reset(ServerPlayer p) {
        p.setAttached(ModAttachments.DRUG_STATE, DrugState.EMPTY);
        p.removeEffect(ModEffects.WITHDRAWAL);
        p.removeEffect(ModEffects.OVERDOSE);
        p.removeEffect(ModEffects.BAD_TRIP);
    }

    // ------------------------------------------------------------------ tick (1x por segundo)
    public static void tickPlayer(ServerPlayer p) {
        DrogasConfig cfg = DrogasConfig.get();
        DrugState cur = state(p);
        DrugState.Mutable m = cur.mutable();
        long now = p.level().getGameTime();
        if ((now / 20) % 3 == 0) Knowledge.scanInventory(p);
        int worstLevel = 0;
        Substance worst = null;

        for (Substance s : Substance.values()) {
            int i = s.ordinal();
            boolean active = p.hasEffect(ModEffects.of(s));
            m.load[i] = Math.max(0f, m.load[i] - s.loadDecayPerSec);

            if (active) {
                m.wd[i] = 0;
                if (s.soundEvery > 0 && (now / 20) % Math.max(1, s.soundEvery / 20) == 0) Fx.sound(p, s.sound, s.soundVolume, 0.9f + p.getRandom().nextFloat() * 0.2f);
                continue;
            }
            // recuperacao gradual da tolerancia quando limpo
            m.tol[i] = Math.max(0f, m.tol[i] - s.tolDecayPerMin / 60f * cfg.recoveryRate);

            if (m.dep[i] <= 0f || m.last[i] < 0) { m.wd[i] = 0; continue; }
            long since = now - m.last[i];
            boolean withdrawing = s.addictive() && m.dep[i] >= s.depThreshold && since >= s.withdrawalOnset;
            if (withdrawing) {
                float span = Math.max(0.01f, 1f - s.depThreshold);
                int level = 1 + Mth.clamp((int) (((m.dep[i] - s.depThreshold) / span) * 3f), 0, 2);
                m.wd[i] = level;
                Knowledge.unlock(p, "guide.withdrawal");
                int symptoms = Math.min(s.withdrawalEffects.size(), level + 1);
                for (int k = 0; k < symptoms; k++) Fx.give(p, s.withdrawalEffects.get(k), 100, level - 1);
                // dependencia cai gradualmente durante a abstinencia (recuperacao)
                m.dep[i] = Math.max(0f, m.dep[i] - s.recoveryPerMin / 60f * cfg.recoveryRate);
                if (level > worstLevel) { worstLevel = level; worst = s; }
            } else {
                m.wd[i] = 0;
                if (since > s.withdrawalOnset) m.dep[i] = Math.max(0f, m.dep[i] - s.depDecayPerMin / 60f * cfg.recoveryRate);
            }
        }

        if (worstLevel > 0) {
            p.addEffect(new MobEffectInstance(ModEffects.WITHDRAWAL, 100, worstLevel - 1, false, false, true));
            if ((now / 20) % 30 == 0 && worst != null) {
                p.displayClientMessage(Component.translatable("message.drogas.craving", name(worst)).withStyle(ChatFormatting.YELLOW), true);
            }
        }
        save(p, cur, m.freeze());
    }
}
