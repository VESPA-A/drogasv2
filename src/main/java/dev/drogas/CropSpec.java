package dev.drogas;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Condicoes de cultivo de cada planta (v2.0). growthChance = 1 chance em N por random tick (maior = mais lento).
 * Climas sao tags de bioma em data/drogas/tags/worldgen/biome/climate/*.json (editaveis por datapacks).
 */
public record CropSpec(String id, String seedId, Soil soil, int minLight, int maxLight, int growthChance, boolean needsWater,
                       boolean darkOnly, Set<Climate> ideal, Set<Climate> tolerated) {
    public enum Soil { FARMLAND, DIRT, SUBSTRATE }

    public enum Climate {
        HUMID, TEMPERATE, ARID, COLD;

        public TagKey<Biome> tag() {
            return TagKey.create(Registries.BIOME, DrogasMod.id("climate/" + name().toLowerCase()));
        }
    }

    public String blockId() { return id + "_crop"; }

    public static final CropSpec CANNABIS = new CropSpec("cannabis", "cannabis_seeds", Soil.FARMLAND, 10, 15, 5, false, false,
            Set.of(Climate.TEMPERATE), Set.of(Climate.HUMID, Climate.ARID));
    public static final CropSpec POPPY = new CropSpec("poppy", "poppy_seeds", Soil.FARMLAND, 9, 15, 6, false, false,
            Set.of(Climate.TEMPERATE, Climate.ARID), Set.of(Climate.HUMID));
    public static final CropSpec TOBACCO = new CropSpec("tobacco", "tobacco_seeds", Soil.FARMLAND, 9, 15, 7, true, false,
            Set.of(Climate.HUMID, Climate.TEMPERATE), Set.of(Climate.ARID));
    public static final CropSpec PSILOCYBIN = new CropSpec("psilocybin", "psilocybin_spores", Soil.SUBSTRATE, 0, 9, 5, false, true,
            Set.of(Climate.HUMID), Set.of(Climate.TEMPERATE, Climate.COLD));
    public static final CropSpec COCA = new CropSpec("coca", "coca_seeds", Soil.FARMLAND, 11, 15, 9, false, false,
            Set.of(Climate.HUMID), Set.of(Climate.TEMPERATE));
    public static final CropSpec JAGUBE = new CropSpec("jagube", "jagube_seeds", Soil.FARMLAND, 8, 15, 10, true, false,
            Set.of(Climate.HUMID), Set.of(Climate.TEMPERATE));
    public static final CropSpec CHACRONA = new CropSpec("chacrona", "chacrona_seeds", Soil.DIRT, 5, 13, 8, false, false,
            Set.of(Climate.HUMID), Set.of(Climate.TEMPERATE, Climate.ARID));

    public static final List<CropSpec> ALL = List.of(CANNABIS, POPPY, TOBACCO, PSILOCYBIN, COCA, JAGUBE, CHACRONA);
}
