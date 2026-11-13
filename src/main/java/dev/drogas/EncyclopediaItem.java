package dev.drogas;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Enciclopedia de Substancias: botao direito abre a tela (plantas, equipamentos, substancias, receitas, guias, condicao). */
public class EncyclopediaItem extends Item {
    public EncyclopediaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() && DrogasBridge.openEncyclopedia != null) DrogasBridge.openEncyclopedia.accept(0);
        return InteractionResult.SUCCESS;
    }
}
