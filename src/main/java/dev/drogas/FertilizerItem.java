package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;

/** Adubo Organico: botao direito numa planta aumenta o vigor e alivia o estresse. */
public class FertilizerItem extends Item {
    public FertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState st = level.getBlockState(pos);
        if (!(st.getBlock() instanceof DrugCropBlock crop)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (crop.fertilize(level, pos, st)) {
                Player player = context.getPlayer();
                if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
                if (level instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
                if (player instanceof ServerPlayer sp) {
                    sp.displayClientMessage(Component.translatable("message.drogas.fertilized"), true);
                    Knowledge.unlock(sp, "equip.organic_fertilizer", "guide.quality");
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
