package dev.drogas;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrogasMod implements ModInitializer {
    public static final String MOD_ID = "drogas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        DrogasConfig.load();
        ModComponents.init();
        ModSounds.init();
        ModAttachments.init();
        ModEffects.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModCreativeTab.init();
        LootInjector.init();
        DrogasCommands.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                try {
                    DrugManager.tickPlayer(player);
                } catch (RuntimeException e) {
                    LOGGER.error("Erro ao processar o estado de substâncias de {}", player.getName().getString(), e);
                }
            }
        });
        LOGGER.info("Drogas Mod carregado: {} substâncias, {} itens.", Substance.values().length, ModItems.count());
    }
}
