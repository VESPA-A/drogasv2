package dev.drogas.client;

import dev.drogas.DrogasConfig;
import dev.drogas.ModEffects;
import dev.drogas.Substance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

/** Estado visual do cliente, recalculado a cada tick a partir dos efeitos sincronizados pelo servidor. */
public final class ClientEffects {
    private ClientEffects() {}

    public record Active(Substance substance, int amplifier, int ticksLeft, float intensity) {}

    private static final List<Active> ACTIVE = new ArrayList<>();
    private static float fovDelta, prevYaw, prevPitch;
    public static int withdrawalLevel;
    public static int overdoseAmp = -1;
    public static int badTripAmp = -1;

    public static List<Active> active() { return ACTIVE; }
    public static float fovDelta() { return fovDelta; }

    private static ParticleOptions particle(String kind) {
        return switch (kind) {
            case "end_rod" -> ParticleTypes.END_ROD;
            case "witch" -> ParticleTypes.WITCH;
            case "note" -> ParticleTypes.NOTE;
            case "happy" -> ParticleTypes.HAPPY_VILLAGER;
            default -> null;
        };
    }

    public static void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        ACTIVE.clear();
        if (p == null || mc.level == null) {
            fovDelta = 0f; prevYaw = 0f; prevPitch = 0f; withdrawalLevel = 0; overdoseAmp = -1; badTripAmp = -1;
            return;
        }
        DrogasConfig cfg = DrogasConfig.get();
        float vis = Math.max(0f, cfg.visualIntensity);
        float targetFov = 0f, sway = 0f, shake = 0f;

        for (Substance s : Substance.values()) {
            MobEffectInstance inst = p.getEffect(ModEffects.of(s));
            if (inst == null) continue;
            float intensity = (inst.getAmplifier() + 1) / 5f;
            int left = inst.getDuration();
            float fade = left < 0 ? 1f : Mth.clamp(left / 100f, 0f, 1f);
            ACTIVE.add(new Active(s, inst.getAmplifier(), left, intensity * fade));
            targetFov += s.fov * intensity * fade;
            sway += s.sway * intensity * fade;
            shake += s.shake * intensity * fade;
        }
        MobEffectInstance wd = p.getEffect(ModEffects.WITHDRAWAL);
        withdrawalLevel = wd == null ? 0 : wd.getAmplifier() + 1;
        shake += withdrawalLevel * 0.25f;
        MobEffectInstance od = p.getEffect(ModEffects.OVERDOSE);
        overdoseAmp = od == null ? -1 : od.getAmplifier();
        if (overdoseAmp >= 0) { sway += 2.5f + overdoseAmp; shake += 0.4f; targetFov -= 6f; }
        MobEffectInstance bt = p.getEffect(ModEffects.BAD_TRIP);
        badTripAmp = bt == null ? -1 : bt.getAmplifier();
        if (badTripAmp >= 0) { sway += 3f + badTripAmp; shake += 0.5f + 0.2f * badTripAmp; targetFov += 5f; }

        fovDelta += ((cfg.fovEffects ? targetFov * vis : 0f) - fovDelta) * 0.08f;
        if (Math.abs(fovDelta) < 0.01f) fovDelta = 0f;

        // tremor/balanco da camera: aplicamos so a diferenca entre ticks, sem acumular desvio
        float yaw = 0f, pitch = 0f;
        if (cfg.cameraSway && vis > 0f && (sway > 0f || shake > 0f)) {
            float t = p.tickCount * 0.05f;
            yaw = sway * vis * (Mth.sin(t * 1.3f) + 0.5f * Mth.sin(t * 2.7f)) + (p.getRandom().nextFloat() - 0.5f) * 2f * shake * vis;
            pitch = sway * vis * 0.6f * Mth.cos(t * 0.9f) + (p.getRandom().nextFloat() - 0.5f) * 2f * shake * vis * 0.7f;
        }
        p.setYRot(p.getYRot() + yaw - prevYaw);
        p.setXRot(Mth.clamp(p.getXRot() + pitch - prevPitch, -90f, 90f));
        prevYaw = yaw; prevPitch = pitch;

        // alucinacoes: particulas coloridas ao redor do jogador
        if (cfg.hallucinationParticles && vis > 0f && p.tickCount % 3 == 0) {
            for (Active a : ACTIVE) {
                ParticleOptions opt = particle(a.substance().particle);
                if (opt == null) continue;
                int n = 1 + a.amplifier();
                for (int i = 0; i < n; i++) {
                    double ang = p.getRandom().nextDouble() * Math.PI * 2, r = 1.5 + p.getRandom().nextDouble() * 3.0;
                    mc.level.addParticle(opt, p.getX() + Math.cos(ang) * r, p.getY() + 0.3 + p.getRandom().nextDouble() * 2.2, p.getZ() + Math.sin(ang) * r,
                            0.0, 0.02, 0.0);
                }
            }
        }
    }
}
