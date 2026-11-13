package dev.drogas.client;

import dev.drogas.Knowledge;
import dev.drogas.MachineBlockEntity;
import dev.drogas.MachineMenu;
import dev.drogas.MachineRecipes;
import dev.drogas.MachineType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Tela das maquinas: slots, seta de progresso, chama e painel informativo (receita, tempo, velocidade, desgaste, combustivel, estado). */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int INFO_X = 180;

    public MachineScreen(MachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 252;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (176 - this.font.width(this.title)) / 2;
    }

    private static int wearState(int wear) {
        return wear >= MachineBlockEntity.WEAR_MAX ? 3 : wear >= MachineBlockEntity.WEAR_WORN ? 2 : wear >= MachineBlockEntity.WEAR_DIRTY ? 1 : 0;
    }

    private int lines(GuiGraphics g, Component text, int x, int y, int w, int color) {
        for (FormattedCharSequence l : this.font.split(text, w)) { g.drawString(this.font, l, x, y, color, false); y += 9; }
        return y;
    }

    @Override
    protected void renderBg(GuiGraphics g, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        GuiPanel.draw(g, x, y, this.imageWidth, this.imageHeight);
        for (Slot s : this.menu.slots) GuiPanel.slot(g, x + s.x, y + s.y);

        MachineType t = this.menu.type;
        int[][] pos = MachineMenu.layout(t);
        int lastInX = pos[t.inputs - 1][0];
        int outX = pos[t.outSlot][0];
        int ax0 = lastInX + 22, ax1 = outX - 5, ay = 36;
        g.fill(x + ax0, y + ay, x + ax1, y + ay + 8, 0xFF8B8B8B);
        g.fill(x + ax0 + 1, y + ay + 1, x + ax1 - 1, y + ay + 7, 0xFF6A6A6A);
        int max = this.menu.maxProgress();
        if (max > 0) {
            int w = (ax1 - ax0 - 2) * Math.min(this.menu.progress(), max) / max;
            g.fill(x + ax0 + 1, y + ay + 1, x + ax0 + 1 + w, y + ay + 7, this.menu.status() == 1 ? 0xFF3CB35A : 0xFF8A8A3A);
        }
        if (t.hasFuel()) {
            int fx = pos[t.fuelSlot][0] + 2, fy = pos[t.fuelSlot][1] - 17;
            g.fill(x + fx, y + fy, x + fx + 14, y + fy + 14, 0xFF404040);
            int total = Math.max(1, this.menu.burnTotal());
            int h = 14 * Math.min(this.menu.burn(), total) / total;
            if (h > 0) g.fill(x + fx + 1, y + fy + 14 - h, x + fx + 13, y + fy + 14, 0xFFFF9A2E);
        }
        // moldura do slot de melhoria (indicador de nivel)
        int ux = pos[t.upgradeSlot][0], uy = pos[t.upgradeSlot][1];
        for (int i = 0; i < 3; i++) g.fill(x + ux - 22 + i * 5, y + uy + 6, x + ux - 22 + i * 5 + 4, y + uy + 10, i < this.menu.tier() ? 0xFF5CE0E6 : 0xFF3A3A3A);

        // painel informativo
        int ix = x + INFO_X, iy = y + 6;
        g.fill(ix - 2, iy - 2, x + this.imageWidth - 5, y + this.imageHeight - 6, 0xFF8B8B8B);
        g.fill(ix - 1, iy - 1, x + this.imageWidth - 6, y + this.imageHeight - 7, 0xFF5A5A5A);
        int ty = iy + 2, tw = this.imageWidth - INFO_X - 10, col = 0xFFE8E8E8;
        Minecraft mc = Minecraft.getInstance();
        MachineRecipes.Recipe r = MachineRecipes.byIndex(this.menu.recipeIndex());
        Component recipe;
        if (r == null) recipe = Component.translatable("gui.drogas.m.recipe", Component.translatable("gui.drogas.m.none"));
        else if (mc.player != null && Knowledge.has(mc.player, r.id())) recipe = Component.translatable("gui.drogas.m.recipe", new ItemStack(r.output()).getHoverName());
        else recipe = Component.translatable("gui.drogas.m.recipe", Component.translatable("gui.drogas.m.unknown"));
        ty = lines(g, recipe, ix, ty, tw, col) + 3;
        ty = lines(g, Component.translatable("gui.drogas.m.status", Component.translatable("gui.drogas.m.status." + this.menu.status())), ix, ty, tw,
                this.menu.status() == 1 ? 0xFF7CFF8A : this.menu.status() == 4 ? 0xFFFF6060 : 0xFFFFD27C) + 3;
        if (max > 0) {
            int speed = Math.max(1, this.menu.speedPercent());
            int remaining = Math.max(0, (int) ((max - this.menu.progress()) * 100f / speed / 20f));
            ty = lines(g, Component.translatable("gui.drogas.m.time", String.format("%d:%02d", remaining / 60, remaining % 60)), ix, ty, tw, col);
        }
        ty = lines(g, Component.translatable("gui.drogas.m.speed", this.menu.speedPercent()), ix, ty, tw, col) + 3;
        int ws = wearState(this.menu.wear());
        ty = lines(g, Component.translatable("gui.drogas.m.wear", Component.translatable("gui.drogas.m.wear." + ws)), ix, ty, tw, ws >= 2 ? 0xFFFF9A6A : col);
        GuiPanel.bar(g, ix, ty, tw, 6, Math.min(1f, this.menu.wear() / (float) MachineBlockEntity.WEAR_MAX), ws >= 2 ? 0xE05050 : ws == 1 ? 0xE0C040 : 0x50C060);
        ty += 10;
        if (t.hasFuel()) {
            int secs = this.menu.burn() / 20;
            lines(g, Component.translatable("gui.drogas.m.fuel_left", String.format("%d:%02d", secs / 60, secs % 60)), ix, ty, tw, col);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        this.renderTooltip(g, mouseX, mouseY);
    }
}
