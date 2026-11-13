package dev.drogas;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Item consumivel de uma substancia: usar o item aplica os efeitos automaticamente (somente no servidor). */
public class DrugItem extends Item {
    private final Substance substance;
    private final float dose;
    private final ItemCatalog.Form form;
    private final ItemCatalog.Rem remainder;

    public DrugItem(Properties properties, Substance substance, float dose, ItemCatalog.Form form, ItemCatalog.Rem remainder) {
        super(properties);
        this.substance = substance;
        this.dose = dose;
        this.form = form;
        this.remainder = remainder;
    }

    public Substance substance() { return substance; }
    public ItemCatalog.Form form() { return form; }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer p)) return;
        if (form == ItemCatalog.Form.SMOKE && remainingUseDuration % 8 == 0) Fx.particles(p, ParticleTypes.SMOKE, 1, 0.08, 0.005);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int quality = Quality.of(stack);
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            DrugManager.consume(player, substance, dose, quality);
            switch (form) {
                case SMOKE -> Fx.particles(player, ParticleTypes.SMOKE, 8, 0.18, 0.012);
                case SNORT -> Fx.particles(player, ParticleTypes.CLOUD, 3, 0.1, 0.01);
                default -> { }
            }
            if (remainder != ItemCatalog.Rem.NONE && !player.getAbilities().instabuild) {
                ItemStack rest = new ItemStack(remainder == ItemCatalog.Rem.BOTTLE ? Items.GLASS_BOTTLE : ModItems.get("syringe"));
                if (result.isEmpty()) return rest;
                if (!player.getInventory().add(rest)) player.drop(rest, false);
            }
        }
        return result;
    }
}
