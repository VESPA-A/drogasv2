package dev.drogas.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Estilos visuais por substancia, todos 2D (fill/gradiente) sobre o HUD: sem shaders, compativel com Sodium/Iris.
 * Cada chamada faz poucas dezenas de retangulos no maximo.
 */
public final class VisualStyles {
    private VisualStyles() {}

    public enum Style { NONE, STROBE, KALEIDO, HYPER, RIPPLE, EYELID, SCAN, GRID, DOUBLE, BEAT, IRON }

    /** Na ordem de Substance: cannabis, cocaine, crack, lsd, psilocybin, opium, morphine, heroin, meth, mdma, ketamine, dmt, ayahuasca, tobacco, alcohol, nebulon, zyrax. */
    private static final Style[] BY_ORDINAL = {Style.NONE, Style.STROBE, Style.STROBE, Style.KALEIDO, Style.RIPPLE, Style.EYELID, Style.EYELID, Style.EYELID,
            Style.SCAN, Style.BEAT, Style.DOUBLE, Style.HYPER, Style.RIPPLE, Style.NONE, Style.DOUBLE, Style.GRID, Style.IRON};

    public static Style of(int ordinal) { return ordinal >= 0 && ordinal < BY_ORDINAL.length ? BY_ORDINAL[ordinal] : Style.NONE; }

    private static int argb(float a, int rgb) { return (Mth.clamp((int) (a * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF); }

    private static void frame(GuiGraphics g, int w, int h, int inset, int thick, int color) {
        if (inset * 2 + thick * 2 >= Math.min(w, h)) return;
        g.fill(inset, inset, w - inset, inset + thick, color);
        g.fill(inset, h - inset - thick, w - inset, h - inset, color);
        g.fill(inset, inset + thick, inset + thick, h - inset - thick, color);
        g.fill(w - inset - thick, inset + thick, w - inset, h - inset - thick, color);
    }

    public static void render(Style style, GuiGraphics g, int w, int h, float k, float t, int rgb, int seed) {
        if (k <= 0.01f) return;
        switch (style) {
            case STROBE -> {
                float f = Mth.sin(t * 9f + seed);
                if (f > 0.93f) {
                    int c = argb(0.20f * k, 0xFFFFFF);
                    g.fill(0, 0, w, 6, c); g.fill(0, h - 6, w, h, c); g.fill(0, 6, 6, h - 6, c); g.fill(w - 6, 6, w, h - 6, c);
                }
            }
            case KALEIDO, HYPER -> {
                boolean hyper = style == Style.HYPER;
                int n = hyper ? 10 : 7, step = Math.max(6, Math.min(w, h) / (n * 2 + 2));
                float speed = hyper ? 2.2f : 0.5f, off = (t * speed * step) % step;
                for (int i = 0; i < n; i++) {
                    int c = argb((hyper ? 0.26f : 0.16f) * k * (1f - i / (float) n), Mth.hsvToRgb((t * (hyper ? 0.35f : 0.12f) + i * 0.11f) % 1f, 0.75f, 1f));
                    frame(g, w, h, (int) (i * step + off), Math.max(2, step / 2), c);
                }
            }
            case RIPPLE -> {
                int base = Math.min(w, h) / 10;
                for (int i = 0; i < 3; i++) {
                    float ph = Mth.sin(t * 0.9f + i * 1.2f + seed) * 0.5f + 0.5f;
                    frame(g, w, h, (int) (base * (0.6f + i * 0.9f + ph * 1.2f)), 3, argb(0.14f * k, rgb));
                }
            }
            case EYELID -> {
                float close = k * (0.10f + 0.12f * (0.5f + 0.5f * Mth.sin(t * 0.5f + seed)));
                int eh = (int) (h * close);
                if (eh > 1) {
                    g.fillGradient(0, 0, w, eh, argb(0.85f * k, 0x000000), argb(0f, 0x000000));
                    g.fillGradient(0, h - eh, w, h, argb(0f, 0x000000), argb(0.85f * k, 0x000000));
                }
            }
            case SCAN -> {
                int sp = 5, off = (int) ((t * 40f) % sp);
                for (int y = off; y < h; y += sp) g.fill(0, y, w, y + 1, argb(0.07f * k, rgb));
            }
            case GRID -> {
                int sp = 14, off = (int) ((t * 20f) % sp);
                for (int x = off; x < w; x += sp) g.fill(x, 0, x + 1, h, argb(0.06f * k, rgb));
                for (int y = off; y < h; y += sp) g.fill(0, y, w, y + 1, argb(0.06f * k, rgb));
            }
            case DOUBLE -> {
                int shift = (int) (Mth.sin(t * 0.8f + seed) * 5f * k) + 4;
                g.fill(0, 0, shift + 3, h, argb(0.10f * k, rgb));
                g.fill(w - shift - 3, 0, w, h, argb(0.10f * k, rgb));
                g.fill(shift, 0, shift + 1, h, argb(0.12f * k, 0xFFFFFF));
                g.fill(w - shift - 1, 0, w - shift, h, argb(0.12f * k, 0xFFFFFF));
            }
            case BEAT -> {
                float beat = Mth.sin(t * 6.2832f * 2f);
                float p = Math.max(0f, beat) * k;
                int bw = (int) (w * 0.04f * (0.5f + p * 1.5f));
                g.fill(0, 0, bw, h, argb(0.22f * k * (0.4f + p), rgb));
                g.fill(w - bw, 0, w, h, argb(0.22f * k * (0.4f + p), rgb));
            }
            case IRON -> {
                int bw = (int) (w * 0.07f * k) + 4;
                g.fill(0, 0, bw, h, argb(0.50f * k, 0x200000));
                g.fill(w - bw, 0, w, h, argb(0.50f * k, 0x200000));
                for (int x = bw; x < bw + 18; x += 6) { g.fill(x, 0, x + 1, h, argb(0.25f * k, 0x600000)); g.fill(w - x, 0, w - x + 1, h, argb(0.25f * k, 0x600000)); }
            }
            default -> { }
        }
    }
}
