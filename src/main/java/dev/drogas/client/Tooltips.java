package dev.drogas.client;

import dev.drogas.DocumentItem;
import dev.drogas.DrugItem;
import dev.drogas.EncyclopediaItem;
import dev.drogas.ModComponents;
import dev.drogas.ModItems;
import dev.drogas.Quality;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Linhas extras nos tooltips: qualidade, substancia, documentos, modulos e kit de manutencao. */
public final class Tooltips {
    private Tooltips() {}

    private static final String[] SPEED = {"", "1.5", "2", "3"};

    public static void add(ItemStack stack, List<Component> lines) {
        Item item = stack.getItem();
        Integer q = stack.get(ModComponents.QUALITY);
        if (q != null) lines.add(Component.translatable("tooltip.drogas.quality", Quality.label(q)).withStyle(ChatFormatting.GRAY));
        if (item instanceof DrugItem d) {
            lines.add(Component.translatable("tooltip.drogas.substance", Component.translatable(d.substance().translationKey())).withStyle(ChatFormatting.DARK_GRAY));
        } else if (item instanceof DocumentItem) {
            lines.add(Component.translatable("tooltip.drogas.document").withStyle(ChatFormatting.DARK_GRAY));
        } else if (item instanceof EncyclopediaItem) {
            lines.add(Component.translatable("tooltip.drogas.encyclopedia").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            int t = ModItems.moduleTier(stack);
            if (t > 0) lines.add(Component.translatable("tooltip.drogas.module", t, SPEED[t]).withStyle(ChatFormatting.DARK_GRAY));
            else if (item == ModItems.get("maintenance_kit")) lines.add(Component.translatable("tooltip.drogas.kit").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
