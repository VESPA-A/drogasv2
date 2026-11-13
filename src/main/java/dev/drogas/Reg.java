package dev.drogas;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Busca segura de entradas de registro por id (ex.: "nausea", "movement_speed"). Os overloads de wrap()
 * toleram variacoes de retorno entre versoes (Optional, Holder ou valor direto).
 */
public final class Reg {
    private Reg() {}

    public static <T> Holder<T> holder(Registry<T> reg, String path) {
        Identifier id = Identifier.tryParse(path);
        if (id == null) return null;
        return wrap(reg, reg.get(ResourceKey.create(reg.key(), id)));
    }

    private static <T> Holder<T> wrap(Registry<T> reg, Optional<? extends Holder<T>> o) {
        return o.map(h -> (Holder<T>) h).orElse(null);
    }

    private static <T> Holder<T> wrap(Registry<T> reg, Holder<T> h) {
        return h;
    }

    private static <T> Holder<T> wrap(Registry<T> reg, T value) {
        return value == null ? null : reg.wrapAsHolder(value);
    }
}
