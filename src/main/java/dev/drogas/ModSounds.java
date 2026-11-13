package dev.drogas;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/** Sons proprios (arquivos .ogg sintetizados em assets/drogas/sounds, mapeados em sounds.json). */
public final class ModSounds {
    private ModSounds() {}

    private static final Map<String, Holder<SoundEvent>> MAP = new HashMap<>();

    public static void init() {
        for (String n : SoundCatalog.NAMES) {
            Identifier id = DrogasMod.id(n);
            MAP.put(n, Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id)));
        }
    }

    public static Holder<SoundEvent> holder(String name) {
        Holder<SoundEvent> h = MAP.get(name);
        if (h == null) throw new IllegalArgumentException("Som desconhecido: " + name);
        return h;
    }

    public static void play(Level level, double x, double y, double z, String name, SoundSource source, float volume, float pitch) {
        level.playSound(null, x, y, z, holder(name).value(), source, volume, pitch);
    }
}
