package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Anotacoes de campo achadas nas estruturas: botao direito revela entradas da enciclopedia (exploracao). */
public class FieldNotesBlock extends Block {
    private final String kind;

    public FieldNotesBlock(BlockBehaviour.Properties properties, String kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (Knowledge.unlockGroup(sp, "notes_" + kind) == 0) sp.displayClientMessage(Component.translatable("message.drogas.notes"), true);
            else Fx.sound(sp, "drogas:ui.page", 0.7f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }
}
