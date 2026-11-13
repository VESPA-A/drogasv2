package dev.drogas;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Documento de pesquisa: ao ler (botao direito) libera entradas da enciclopedia e e consumido se ensinou algo. */
public class DocumentItem extends Item {
    private final String id;

    public DocumentItem(Properties properties, String id) {
        super(properties);
        this.id = id;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            int learned = Knowledge.unlockGroup(sp, id);
            if (learned > 0) {
                ItemStack held = player.getItemInHand(hand);
                if (!player.getAbilities().instabuild) held.shrink(1);
                Fx.sound(sp, "drogas:ui.page", 0.8f, 1.0f);
            } else {
                sp.displayClientMessage(Component.translatable("message.drogas.doc_known"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
