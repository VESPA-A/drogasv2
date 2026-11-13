package dev.drogas;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Kit de Emergencia (trata overdose) e Tonico Desintoxicante (acelera a recuperacao). */
public class TreatmentItem extends Item {
    public enum Kind { EMERGENCY, DETOX }

    private final Kind kind;

    public TreatmentItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (kind == Kind.EMERGENCY) DrugManager.emergency(player);
            else DrugManager.detox(player);
        }
        return result;
    }
}
