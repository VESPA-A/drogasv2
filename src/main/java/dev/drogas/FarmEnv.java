package dev.drogas;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Avalia o ambiente de uma planta: luz, agua/umidade, clima (bioma), substrato e estufa funcional. */
public final class FarmEnv {
    private FarmEnv() {}

    public enum Rating { IDEAL, OK, POOR, BAD }
    public record Result(Rating rating, boolean greenhouse) {}

    private static final TagKey<Block> GLASS = TagKey.create(Registries.BLOCK, DrogasMod.id("greenhouse_glass"));
    private static final TagKey<Block> PRIME = TagKey.create(Registries.BLOCK, DrogasMod.id("mushroom_substrate_prime"));

    /** Vidro por cima (ate 8 blocos) + Estufa Reguladora ativa a ate 6 blocos. */
    public static boolean greenhouse(Level level, BlockPos pos) {
        boolean covered = false;
        for (int i = 1; i <= 8; i++) {
            BlockState s = level.getBlockState(pos.above(i));
            if (s.isAir()) continue;
            covered = s.is(GLASS);
            break;
        }
        if (!covered) return false;
        Block regulator = ModBlocks.regulator();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -3, -6), pos.offset(6, 3, 6))) {
            BlockState s = level.getBlockState(p);
            if (s.is(regulator) && s.getValue(GreenhouseRegulatorBlock.LIT)) return true;
        }
        return false;
    }

    private static boolean nearWater(Level level, BlockPos pos, int r) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -1, -r), pos.offset(r, 0, r))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    private static boolean matches(Holder<Biome> biome, Set<CropSpec.Climate> set) {
        for (CropSpec.Climate c : set) if (biome.is(c.tag())) return true;
        return false;
    }

    public static Result rate(ServerLevel level, BlockPos pos, CropSpec spec) {
        boolean gh = greenhouse(level, pos);
        int poor = 0, bonus = 0;
        int light = level.getRawBrightness(pos, 0);
        if (spec.darkOnly()) {
            if (level.canSeeSky(pos)) return new Result(Rating.BAD, gh);      // o sol mata o cultivo de sombra
        } else if (gh && light < 13) {
            light = 13;                                                          // lampadas/estufa garantem luz minima
        }
        if (light < spec.minLight() || light > spec.maxLight()) {
            int d = light < spec.minLight() ? spec.minLight() - light : light - spec.maxLight();
            if (d >= 3) return new Result(Rating.BAD, gh);
            poor++;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean farmland = below.is(Blocks.FARMLAND);
        boolean wet = nearWater(level, pos, 3) || (farmland && below.getValue(FarmBlock.MOISTURE) >= 7);
        if (spec.needsWater() && !wet) poor++;
        else if (farmland && below.getValue(FarmBlock.MOISTURE) == 0) poor++;
        else if (wet) bonus++;
        Holder<Biome> biome = level.getBiome(pos);
        if (gh || matches(biome, spec.ideal())) bonus++;
        else if (!matches(biome, spec.tolerated())) poor++;
        if (spec.soil() == CropSpec.Soil.SUBSTRATE && below.is(PRIME)) bonus++;
        Rating r = poor >= 2 ? Rating.BAD : poor == 1 ? Rating.POOR : bonus >= 2 ? Rating.IDEAL : Rating.OK;
        return new Result(r, gh);
    }
}
