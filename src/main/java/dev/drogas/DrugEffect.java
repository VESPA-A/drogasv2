package dev.drogas;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * Efeito de status usado por todas as substancias (e por abstinencia, overdose e bad trip).
 * Como e um MobEffect vanilla, duracao/nivel sao sincronizados automaticamente com o cliente,
 * persistem ao salvar o mundo e funcionam em multiplayer.
 */
public class DrugEffect extends MobEffect {
    public enum Kind { SUBSTANCE, WITHDRAWAL, OVERDOSE, BAD_TRIP }

    private final Kind kind;
    private final Substance substance;

    public DrugEffect(Kind kind, Substance substance, int color) {
        super(kind == Kind.SUBSTANCE ? MobEffectCategory.NEUTRAL : MobEffectCategory.HARMFUL, color);
        this.kind = kind;
        this.substance = substance;
        switch (kind) {
            case SUBSTANCE -> {
                String n = substance.id;
                mod("movement_speed", n, substance.speed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                mod("attack_speed", n, substance.atkSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                mod("mining_efficiency", n, substance.mining, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                mod("attack_damage", n, substance.damage, AttributeModifier.Operation.ADD_VALUE);
                mod("armor", n, substance.armor, AttributeModifier.Operation.ADD_VALUE);
                mod("knockback_resistance", n, substance.knockbackRes, AttributeModifier.Operation.ADD_VALUE);
                mod("jump_strength", n, substance.jump, AttributeModifier.Operation.ADD_VALUE);
                mod("entity_interaction_range", n, substance.reach, AttributeModifier.Operation.ADD_VALUE);
                mod("block_interaction_range", n, substance.blockReach, AttributeModifier.Operation.ADD_VALUE);
                mod("safe_fall_distance", n, substance.safeFall, AttributeModifier.Operation.ADD_VALUE);
            }
            case WITHDRAWAL -> {
                mod("attack_speed", "withdrawal", -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                mod("movement_speed", "withdrawal", -0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            case OVERDOSE -> mod("movement_speed", "overdose", -0.12, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case BAD_TRIP -> mod("movement_speed", "bad_trip", -0.08, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    /** Soma um modificador de atributo (valor por nivel). Atributos desconhecidos sao ignorados. */
    private void mod(String attributePath, String owner, double perLevel, AttributeModifier.Operation op) {
        if (perLevel == 0.0) return;
        Holder<Attribute> h = Reg.holder(BuiltInRegistries.ATTRIBUTE, attributePath);
        if (h == null) return;
        addAttributeModifier(h, DrogasMod.id("effect." + owner + "." + attributePath), perLevel, op);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        switch (kind) {
            case SUBSTANCE -> {
                if (substance.hunger > 0f && entity instanceof Player p) p.causeFoodExhaustion(substance.hunger * 4f * (1f + amplifier * 0.3f));
                if (substance.heal > 0f && entity.getHealth() < entity.getMaxHealth()) entity.heal(substance.heal * (1f + amplifier * 0.25f));
                if (substance.strain > 0f && amplifier >= 3 && entity.getHealth() > 6f) {
                    entity.hurtServer(level, level.damageSources().magic(), substance.strain);
                }
            }
            case OVERDOSE -> {
                float dmg = 1.0f + amplifier * 0.5f;
                if (!DrogasConfig.get().overdoseLethal) dmg = Math.min(dmg, entity.getHealth() - 1f);
                if (dmg > 0f) entity.hurtServer(level, level.damageSources().magic(), dmg);
            }
            case WITHDRAWAL -> {
                if (entity instanceof Player p) p.causeFoodExhaustion(0.2f * (amplifier + 1));
            }
            default -> { }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int period = kind == Kind.OVERDOSE ? 20 : 80;
        return duration % period == 0;
    }
}
