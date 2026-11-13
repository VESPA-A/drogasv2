package dev.drogas;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Estufa Reguladora: com combustivel (carvao etc.) e agua (balde), plantas sob vidro a 6 blocos ganham clima ideal. */
public class GreenhouseRegulatorBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GreenhouseRegulatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GreenhouseRegulatorBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseRegulatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.REGULATOR, GreenhouseRegulatorBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof GreenhouseRegulatorBlockEntity be) {
            ItemStack held = player.getMainHandItem();
            boolean creative = player.getAbilities().instabuild;
            if (MachineBlockEntity.isFuel(held)) {
                be.addFuel();
                if (!creative) held.shrink(1);
                player.displayClientMessage(Component.translatable("message.drogas.regulator_fuel"), true);
            } else if (held.is(Items.WATER_BUCKET) && be.needsWater()) {
                be.addWater();
                if (!creative) player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                player.displayClientMessage(Component.translatable("message.drogas.regulator_water"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.drogas.regulator", be.fuelPercent(), be.waterPercent()), true);
            }
            if (player instanceof ServerPlayer sp) Knowledge.unlock(sp, "equip.greenhouse_regulator", "guide.greenhouse");
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || random.nextInt(4) != 0) return;
        level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5, pos.getY() + 1.05, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.03, 0.0);
    }
}
