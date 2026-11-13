package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Cultura da v2.0: 8 idades com visual proprio, VIGOR (0-3, define colheita e qualidade) e ESTRESSE (0-2, a planta
 * murcha e morre em condicoes inadequadas). Condicoes por CropSpec/FarmEnv (luz, agua, clima, substrato, estufa).
 */
public class DrugCropBlock extends CropBlock {
    public static final IntegerProperty STRESS = IntegerProperty.create("stress", 0, 2);
    public static final IntegerProperty VIGOR = IntegerProperty.create("vigor", 0, 3);
    private static final TagKey<Block> SUBSTRATE = TagKey.create(Registries.BLOCK, DrogasMod.id("mushroom_substrate"));

    private final CropSpec spec;

    public DrugCropBlock(BlockBehaviour.Properties properties, CropSpec spec) {
        super(properties);
        this.spec = spec;
        registerDefaultState(defaultBlockState().setValue(STRESS, 0).setValue(VIGOR, 1));
    }

    public CropSpec spec() { return spec; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STRESS, VIGOR);
    }

    /** Item de semente (usado no pick-block). */
    protected ItemLike getBaseSeedId() {
        return ModItems.get(spec.seedId());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return switch (spec.soil()) {
            case FARMLAND -> below.is(Blocks.FARMLAND);
            case DIRT -> below.is(Blocks.FARMLAND) || below.is(BlockTags.DIRT);
            case SUBSTRATE -> below.is(SUBSTRATE);       // cogumelos NAO dependem de terra arada
        };
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = getAge(state), stress = state.getValue(STRESS), vigor = state.getValue(VIGOR);
        FarmEnv.Result env = FarmEnv.rate(level, pos, spec);
        int chance = spec.growthChance();
        if (env.greenhouse()) chance = Math.max(1, chance * 2 / 3);
        BlockState ns = state;
        switch (env.rating()) {
            case BAD -> {
                if (random.nextInt(2) == 0) {
                    if (stress >= 2) { wither(level, pos); return; }
                    ns = ns.setValue(STRESS, stress + 1);
                }
            }
            case POOR -> {
                if (age < getMaxAge() && vigor > 0 && random.nextInt(3) == 0) ns = ns.setValue(VIGOR, vigor - 1);
                if (age < getMaxAge() && random.nextInt(chance * 3) == 0) ns = ns.setValue(getAgeProperty(), age + 1);
            }
            case OK -> {
                if (stress > 0 && random.nextInt(3) == 0) ns = ns.setValue(STRESS, stress - 1);
                if (age < getMaxAge()) {
                    if (vigor < 1 && random.nextInt(4) == 0) ns = ns.setValue(VIGOR, vigor + 1);
                    if (random.nextInt(chance) == 0) ns = ns.setValue(getAgeProperty(), age + 1);
                }
            }
            case IDEAL -> {
                if (stress > 0) ns = ns.setValue(STRESS, stress - 1);
                if (age < getMaxAge() && random.nextInt(chance) == 0) {
                    ns = ns.setValue(getAgeProperty(), age + 1);
                    if (vigor < 3 && random.nextBoolean()) ns = ns.setValue(VIGOR, vigor + 1);
                }
            }
        }
        if (age >= getMaxAge() && spec.id().equals("jagube") && stress == 0 && env.rating().ordinal() <= FarmEnv.Rating.OK.ordinal() && random.nextInt(4) == 0) {
            JagubeClimberBlock.spawnAbove(level, pos);
        }
        if (ns != state) level.setBlock(pos, ns, 2);
    }

    private void wither(ServerLevel level, BlockPos pos) {
        ModSounds.play(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "block.wither", SoundSource.BLOCKS, 0.8f, 1.0f);
        level.destroyBlock(pos, false);
        if (level.getRandom().nextInt(5) < 2) Block.popResource(level, pos, new ItemStack(ModItems.get(spec.seedId())));
    }

    /** Adubo organico: +1 de vigor e -1 de estresse. */
    public boolean fertilize(Level level, BlockPos pos, BlockState state) {
        int v = state.getValue(VIGOR), s = state.getValue(STRESS);
        if (v >= 3 && s == 0) return false;
        level.setBlock(pos, state.setValue(VIGOR, Math.min(3, v + 1)).setValue(STRESS, Math.max(0, s - 1)), 2);
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp && isMaxAge(state)) {
            Knowledge.unlock(sp, "plant." + spec.id());
            int q = state.getValue(VIGOR) - (state.getValue(STRESS) >= 1 ? 1 : 0);
            if (q != 1) Knowledge.unlock(sp, "guide.quality");
            ModSounds.play(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "block.harvest", SoundSource.BLOCKS, 0.8f, 1.0f);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
