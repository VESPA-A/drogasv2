package dev.drogas;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModComponents {
    private ModComponents() {}

    /** Qualidade do produto: 0 Ruim, 1 Padrao (ausente), 2 Boa, 3 Excelente. */
    public static final DataComponentType<Integer> QUALITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, DrogasMod.id("quality"),
            DataComponentType.<Integer>builder().persistent(Codec.INT).build());

    public static void init() {
        // carregar a classe registra o componente
    }
}
