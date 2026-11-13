package dev.drogas.client;

import net.minecraft.client.gui.GuiGraphics;

/** Desenha paineis e slots no estilo vanilla usando apenas retangulos (sem texturas, compativel com qualquer resource pack). */
public final class GuiPanel {
    private GuiPanel() {}

    public static void draw(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF000000);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFFFFFFF);
        g.fill(x + 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFFC6C6C6);
    }

    /** Moldura de slot 18x18 cujo item fica em (x, y). */
    public static void slot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        g.fill(x, y, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x, y, x + 17, y + 17, 0xFF8B8B8B);
    }

    public static void bar(GuiGraphics g, int x, int y, int w, int h, float value, int color) {
        g.fill(x, y, x + w, y + h, 0xFF202020);
        int fw = Math.round((w - 2) * Math.max(0f, Math.min(1f, value)));
        if (fw > 0) g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, 0xFF000000 | color);
    }
}
