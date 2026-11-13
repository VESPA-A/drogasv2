package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Jagube trepadeira: usa a logica de apoio da videira vanilla (VineBlock), cresce para cima ao longo de paredes,
 * troncos e trelicas, e os ramos maduros sao colhidos com o botao direito (voltam ao estagio 1).
 */
public class JagubeClimberBlock extends VineBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final int MAX_HEIGHT = 6;

    public JagubeClimberBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    private static boolean supports(ServerLevel level, BlockPos pos, Direction d) {
        BlockPos n = pos.relative(d);
        return level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite());
    }

    /** Chamado pela base (jagube maduro) para iniciar a trepadeira acima dela. */
    public static void spawnAbove(ServerLevel level, BlockPos cropPos) {
        BlockPos up = cropPos.above();
        if (!level.getBlockState(up).isAir()) return;
        BlockState ns = ModBlocks.climber().defaultBlockState();
        boolean any = false;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (supports(level, up, d)) { ns = ns.setValue(PROPERTY_BY_DIRECTION.get(d), true); any = true; }
        }
        if (any) level.setBlock(up, ns, 3);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        CropSpec spec = CropSpec.JAGUBE;
        FarmEnv.Result env = FarmEnv.rate(level, pos, spec);
        if (env.rating() == FarmEnv.Rating.BAD) return;
        if (env.rating() == FarmEnv.Rating.POOR && random.nextInt(2) == 0) return;
        if (age < 3) {
            if (random.nextInt(Math.max(1, spec.growthChance() / 2)) == 0) level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            return;
        }
        if (random.nextInt(6) != 0) return;
        int h = 1;
        BlockPos p = pos.below();
        while (level.getBlockState(p).getBlock() == this && h < MAX_HEIGHT + 1) { h++; p = p.below(); }
        if (h >= MAX_HEIGHT) return;
        BlockPos up = pos.above();
        if (!level.getBlockState(up).isAir()) return;
        BlockState ns = defaultBlockState();
        boolean any = false;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BooleanProperty prop = PROPERTY_BY_DIRECTION.get(d);
            if (state.getValue(prop) && supports(level, up, d)) { ns = ns.setValue(prop, true); any = true; }
        }
        if (any) level.setBlock(up, ns, 3);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 3) return InteractionResult.PASS;
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            FarmEnv.Result env = FarmEnv.rate(sl, pos, CropSpec.JAGUBE);
            int q = env.rating() == FarmEnv.Rating.IDEAL ? Quality.GOOD : env.rating() == FarmEnv.Rating.OK ? Quality.STANDARD : Quality.POOR;
            ItemStack drop = Quality.with(new ItemStack(ModItems.get("jagube_vine"), 1 + level.getRandom().nextInt(2)), q);
            Block.popResource(level, pos, drop);
            level.setBlock(pos, state.setValue(AGE, 1), 2);
            ModSounds.play(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, "block.harvest", SoundSource.BLOCKS, 0.8f, 1.0f);
            if (player instanceof ServerPlayer sp) Knowledge.unlock(sp, "plant.jagube");
        }
        return InteractionResult.SUCCESS;
    }
}
