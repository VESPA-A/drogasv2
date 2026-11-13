package dev.drogas;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Qualidade variavel dos produtos: afeta potencia, duracao e impurezas (carga de overdose). */
public final class Quality {
    private Quality() {}

    public static final int POOR = 0, STANDARD = 1, GOOD = 2, EXCELLENT = 3;
    public static final float[] POTENCY = {0.70f, 1.00f, 1.15f, 1.30f};
    public static final float[] DURATION = {0.80f, 1.00f, 1.10f, 1.25f};
    public static final float[] LOAD = {1.25f, 1.00f, 0.95f, 0.90f};

    private static Set<Item> capable;

    public static int of(ItemStack stack) {
        Integer q = stack.get(ModComponents.QUALITY);
        return q == null ? STANDARD : Mth.clamp(q, POOR, EXCELLENT);
    }

    public static ItemStack with(ItemStack stack, int quality) {
        int q = Mth.clamp(quality, POOR, EXCELLENT);
        if (q == STANDARD) stack.remove(ModComponents.QUALITY);
        else stack.set(ModComponents.QUALITY, q);
        return stack;
    }

    public static synchronized boolean capable(Item item) {
        if (capable == null) {
            capable = new HashSet<>();
            for (String id : ItemCatalog.QUALITY_ITEMS) capable.add(ModItems.get(id));
        }
        return capable.contains(item);
    }

    public static ChatFormatting color(int q) {
        return switch (q) { case POOR -> ChatFormatting.DARK_RED; case GOOD -> ChatFormatting.GREEN; case EXCELLENT -> ChatFormatting.GOLD; default -> ChatFormatting.GRAY; };
    }

    public static Component label(int q) {
        return Component.translatable("quality.drogas." + Mth.clamp(q, 0, 3)).withStyle(color(q));
    }
}
