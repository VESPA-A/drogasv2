package dev.drogas.client;

import dev.drogas.DrogasBridge;
import dev.drogas.DrogasMod;
import dev.drogas.MachineType;
import dev.drogas.ModBlocks;
import dev.drogas.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public class DrogasClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for (MachineType t : MachineType.values()) {
            MenuScreens.register(ModMenus.get(t), MachineScreen::new);
        }
        // plantas, videira e trelica tem texturas com transparencia: precisam da camada CUTOUT
        for (Block b : ModBlocks.cutoutBlocks()) BlockRenderLayerMap.putBlock(b, ChunkSectionLayer.CUTOUT);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, DrogasMod.id("drug_overlay"), DrugHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEffects::tick);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> Tooltips.add(stack, lines));
        DrogasBridge.openEncyclopedia = tab -> Minecraft.getInstance().setScreen(new EncyclopediaScreen(tab));
    }
}
