package dev.drogas;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Prancheta Medica: atalho para a aba "Condicao" da enciclopedia (tolerancia, dependencia, abstinencia). */
public class ClipboardItem extends Item {
    public ClipboardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() && DrogasBridge.openEncyclopedia != null) DrogasBridge.openEncyclopedia.accept(5);
        return InteractionResult.SUCCESS;
    }
}
