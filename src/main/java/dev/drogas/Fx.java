package dev.drogas;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/** Utilitarios de servidor: sons, particulas e efeitos vanilla por id. */
public final class Fx {
    private Fx() {}

    public static void sound(ServerPlayer p, String path, float volume, float pitch) {
        Holder<SoundEvent> h = Reg.holder(BuiltInRegistries.SOUND_EVENT, path);
        if (h == null) return;
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), h.value(), SoundSource.PLAYERS, volume, pitch);
    }

    public static void particles(ServerPlayer p, ParticleOptions type, int count, double spread, double speed) {
        if (p.level() instanceof ServerLevel sl) sl.sendParticles(type, p.getX(), p.getY() + 1.0, p.getZ(), count, spread, spread, spread, speed);
    }

    /** Aplica um efeito vanilla pelo id (ex.: "nausea", "slowness"). Ignora silenciosamente ids desconhecidos. */
    public static void give(ServerPlayer p, String effectPath, int ticks, int amplifier) {
        Holder<MobEffect> h = Reg.holder(BuiltInRegistries.MOB_EFFECT, effectPath);
        if (h == null || ticks <= 0) return;
        p.addEffect(new MobEffectInstance(h, ticks, Math.max(0, amplifier), false, false, true));
    }
}
