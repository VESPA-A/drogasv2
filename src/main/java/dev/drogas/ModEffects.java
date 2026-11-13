package dev.drogas;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
    private ModEffects() {}

    private static final Map<Substance, Holder<MobEffect>> BY_SUBSTANCE = new EnumMap<>(Substance.class);
    public static Holder<MobEffect> WITHDRAWAL;
    public static Holder<MobEffect> OVERDOSE;
    public static Holder<MobEffect> BAD_TRIP;

    public static void init() {
        for (Substance s : Substance.values()) {
            BY_SUBSTANCE.put(s, reg(s.id, new DrugEffect(DrugEffect.Kind.SUBSTANCE, s, s.tint)));
        }
        WITHDRAWAL = reg("withdrawal", new DrugEffect(DrugEffect.Kind.WITHDRAWAL, null, 0x9a9a6a));
        OVERDOSE = reg("overdose", new DrugEffect(DrugEffect.Kind.OVERDOSE, null, 0xc01818));
        BAD_TRIP = reg("bad_trip", new DrugEffect(DrugEffect.Kind.BAD_TRIP, null, 0x3a2a5a));
    }

    private static Holder<MobEffect> reg(String path, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, DrogasMod.id(path), effect);
    }

    public static Holder<MobEffect> of(Substance s) {
        return BY_SUBSTANCE.get(s);
    }
}
