package dev.drogas;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    private ModMenus() {}

    private static final Map<MachineType, MenuType<MachineMenu>> TYPES = new EnumMap<>(MachineType.class);

    public static void init() {
        for (MachineType t : MachineType.values()) {
            MenuType<MachineMenu> mt = new MenuType<>((id, inv) -> new MachineMenu(id, inv, t), FeatureFlags.VANILLA_SET);
            Registry.register(BuiltInRegistries.MENU, DrogasMod.id(t.id), mt);
            TYPES.put(t, mt);
        }
    }

    public static MenuType<MachineMenu> get(MachineType t) {
        return TYPES.get(t);
    }
}
