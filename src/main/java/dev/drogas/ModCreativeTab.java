package dev.drogas;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
    private ModCreativeTab() {}

    public static void init() {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, DrogasMod.id("main"));
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.drogas.main"))
                .icon(() -> new ItemStack(ModItems.get("joint")))
                .displayItems((params, output) -> {
                    for (Item i : ModItems.all()) output.accept(i);
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
    }
}
