package dev.drogas;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class MachineBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public final MachineType type;

    public MachineBlock(Properties properties, MachineType type) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new MachineBlock(p, MachineType.TABLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, ModBlockEntities.MACHINE, MachineBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity be) {
            ItemStack held = player.getMainHandItem();
            if (held.is(ModItems.get("maintenance_kit"))) {
                if (!level.isClientSide()) be.service(player, held);
            } else if (!level.isClientSide()) {
                player.openMenu(be);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Container c) {
            Containers.dropContents(level, pos, c);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Animacao de funcionamento (cliente): particulas conforme o tipo de maquina. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || random.nextInt(3) != 0) return;
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4, y = pos.getY() + 1.05, z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        switch (type) {
            case FURNACE -> { level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.02, 0.0); level.addParticle(ParticleTypes.SMOKE, x, y + 0.1, z, 0.0, 0.03, 0.0); }
            case BENCH -> level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0, 0.04, 0.0);
            case DRYER -> level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.02, 0.0);
            case GRINDER -> level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.01, 0.0);
            case TABLE -> level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.01, 0.0);
        }
    }
}
