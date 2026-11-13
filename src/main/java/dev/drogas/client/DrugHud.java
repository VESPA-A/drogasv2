package dev.drogas.client;

import dev.drogas.DrogasConfig;
import dev.drogas.DrugManager;
import dev.drogas.DrugState;
import dev.drogas.ModItems;
import dev.drogas.Substance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * HUD e sobreposicoes visuais (2D, compativel com Sodium/Iris): tint, vinheta, estilos por substancia, efeitos ativos
 * com icone/tempo/nivel e "sinais vitais" de tolerancia, dependencia e abstinencia.
 */
public final class DrugHud {
    private DrugHud() {}

    /** Icones por substancia, na ordem de Substance. */
    private static final String[] ICONS = {"joint", "cocaine_powder", "crack_rock", "lsd_blotter", "magic_mushroom", "opium_gum", "morphine_vial", "heroin_dose",
            "meth_shard", "mdma_pill", "ketamine_vial", "dmt_dose", "ayahuasca_brew", "cigarette", "beer", "nebulon_tab", "zyrax_vial"};
    private static ItemStack[] iconStacks;

    private static ItemStack icon(Substance s) {
        if (iconStacks == null) {
            iconStacks = new ItemStack[ICONS.length];
            for (int i = 0; i < ICONS.length; i++) iconStacks[i] = new ItemStack(ModItems.get(ICONS[i]));
        }
        return iconStacks[s.ordinal()];
    }

    private static int argb(float alpha, int rgb) {
        return (Mth.clamp((int) (alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static void vignette(GuiGraphics g, int w, int h, float strength, int rgb) {
        int steps = 10;
        int tx = Math.max(2, w / (steps * 3)), ty = Math.max(2, h / (steps * 3));
        for (int i = 0; i < steps; i++) {
            float f = 1f - i / (float) steps;
            float a = strength * f * f * 0.65f;
            if (a < 0.01f) continue;
            int c = argb(a, rgb);
            int x = i * tx, y = i * ty;
            g.fill(x, y, w - x, y + ty, c);
            g.fill(x, h - y - ty, w - x, h - y, c);
            g.fill(x, y + ty, x + tx, h - y - ty, c);
            g.fill(w - x - tx, y + ty, w - x, h - y - ty, c);
        }
    }

    public static String fmt(int ticks) {
        if (ticks < 0) return "--:--";
        int s = ticks / 20;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    /** Barra segmentada (10 segmentos) para tolerancia/dependencia. */
    public static void segments(GuiGraphics g, int x, int y, int count, float value, int color) {
        int lit = Math.round(Mth.clamp(value, 0f, 1f) * count);
        for (int i = 0; i < count; i++) {
            g.fill(x + i * 5, y, x + i * 5 + 4, y + 4, i < lit ? (0xFF000000 | color) : 0xFF2A2A2A);
        }
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        DrogasConfig cfg = DrogasConfig.get();
        float vis = Math.max(0f, cfg.visualIntensity);
        int w = g.guiWidth(), h = g.guiHeight();
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;

        if (vis > 0f) {
            for (ClientEffects.Active a : ClientEffects.active()) {
                Substance s = a.substance();
                float k = a.intensity() * vis;
                int rgb = s.rainbow ? Mth.hsvToRgb((t * 0.12f + s.ordinal() * 0.07f) % 1f, 0.65f, 1f) : s.tint;
                float pulse = s.pulse > 0f ? 0.75f + 0.25f * Mth.sin(t * 6.2832f * s.pulse) : 1f;
                float alpha = Mth.clamp(s.tintAlpha * k * pulse * 1.6f, 0f, 0.5f);
                if (alpha > 0.01f) g.fill(0, 0, w, h, argb(alpha, rgb));
                if (s.vignette > 0f) vignette(g, w, h, Mth.clamp(s.vignette * k * 1.4f, 0f, 1f), s.rainbow ? rgb : (rgb >> 1) & 0x7F7F7F);
                VisualStyles.render(VisualStyles.of(s.ordinal()), g, w, h, Mth.clamp(k, 0f, 1.5f), t, rgb, s.ordinal());
            }
            if (ClientEffects.withdrawalLevel > 0) {
                float k = ClientEffects.withdrawalLevel / 3f;
                g.fill(0, 0, w, h, argb(0.06f + 0.06f * k, 0x9a9a6a));
                vignette(g, w, h, 0.25f * k * (0.8f + 0.2f * Mth.sin(t * 3f)), 0x303010);
                // fissura: pulso de batimento nas bordas, mais rapido e forte conforme o nivel
                float beat = Math.max(0f, Mth.sin(t * 6.2832f * (1.0f + 0.5f * ClientEffects.withdrawalLevel)));
                vignette(g, w, h, 0.18f * k * beat * vis, 0x6a5a10);
            }
            if (ClientEffects.overdoseAmp >= 0) {
                float k = 0.55f + 0.12f * ClientEffects.overdoseAmp;
                vignette(g, w, h, k * (0.7f + 0.3f * Mth.sin(t * 5f)), 0x8a0000);
                g.fill(0, 0, w, h, argb(0.12f, 0x300000));
            }
            if (ClientEffects.badTripAmp >= 0) {
                vignette(g, w, h, 0.7f + 0.1f * ClientEffects.badTripAmp, 0x1a0a2a);
                g.fill(0, 0, w, h, argb(0.15f + 0.05f * Mth.sin(t * 2f), 0x2a1a4a));
                VisualStyles.render(VisualStyles.Style.HYPER, g, w, h, 0.6f, t, 0x8a40ff, 3);
            }
        }
        if (cfg.hud) renderPanels(g, mc, t);
    }

    private static void renderPanels(GuiGraphics g, Minecraft mc, float t) {
        Font font = mc.font;
        DrugState st = DrugManager.state(mc.player);
        int x = 6, y = 6;

        for (ClientEffects.Active a : ClientEffects.active()) {
            Substance s = a.substance();
            g.fill(x - 3, y - 3, x + 130, y + 25, 0x88000000);
            g.renderItem(icon(s), x, y);
            g.drawString(font, Component.translatable(s.translationKey()).getString() + "  " + fmt(a.ticksLeft()), x + 19, y, 0xFF000000 | s.tint, true);
            for (int i = 0; i < 5; i++) g.fill(x + 19 + i * 6, y + 12, x + 19 + i * 6 + 5, y + 16, i <= a.amplifier() ? (0xFF000000 | s.tint) : 0xFF303030);
            int left = a.ticksLeft() < 0 ? 1 : Math.min(1200, a.ticksLeft());
            g.fill(x + 19, y + 18, x + 19 + 100, y + 20, 0xFF202020);
            g.fill(x + 19, y + 18, x + 19 + Math.round(100f * left / 1200f), y + 20, 0xFF000000 | s.tint);
            y += 31;
        }

        if (ClientEffects.withdrawalLevel > 0) {
            Substance worst = null; int lvl = 0;
            for (Substance s : Substance.values()) if (st.withdrawalLevel(s) > lvl) { lvl = st.withdrawalLevel(s); worst = s; }
            if (worst != null) {
                String txt = Component.translatable("gui.drogas.hud.withdrawal", Component.translatable(worst.translationKey()),
                        Component.translatable("gui.drogas.level." + lvl)).getString();
                int shake = (int) (Mth.sin(t * 40f) * lvl);
                float pulse = 0.5f + 0.5f * Mth.sin(t * 6f);
                g.fill(x - 3 + shake, y - 3, x + Math.max(130, font.width(txt) + 3) + shake, y + 11, argb(0.45f + 0.3f * pulse, 0x501010));
                g.drawString(font, txt, x + shake, y, 0xFFE0C040, true);
                for (int i = 0; i < lvl; i++) g.fill(x + i * 7, y + 12, x + i * 7 + 5, y + 15, 0xFFE05050);
                y += 20;
            }
        }
        for (Substance s : Substance.values()) {
            float ratio = st.loadOf(s) / s.odThreshold;
            if (ratio < 0.6f) continue;
            String txt = Component.translatable("gui.drogas.hud.danger", Component.translatable(s.translationKey())).getString();
            g.fill(x - 3, y - 3, x + Math.max(130, font.width(txt) + 3), y + 16, 0x88000000);
            g.drawString(font, txt, x, y, 0xFFFF6060, true);
            g.fill(x, y + 10, x + 100, y + 14, 0xFF202020);
            g.fill(x, y + 10, x + Math.round(100f * Math.min(1f, ratio)), y + 14, ratio > 0.85f ? 0xFFFF2020 : 0xFFFF9A2E);
            y += 22;
        }
        if (ClientEffects.overdoseAmp >= 0) {
            g.drawString(font, Component.translatable("gui.drogas.hud.overdose").getString(), x, y, 0xFFFF3030, true);
        }

        // sinais vitais: ate 4 substancias com tolerancia/dependencia, no canto inferior esquerdo
        List<Substance> rows = new ArrayList<>();
        for (Substance s : Substance.values()) if (st.dep(s) > 0.02f || st.tol(s) > 0.05f) rows.add(s);
        rows.sort((a, b) -> Float.compare(st.dep(b) + st.tol(b) * 0.5f, st.dep(a) + st.tol(a) * 0.5f));
        int vy = mc.getWindow().getGuiScaledHeight() - 30 - Math.min(4, rows.size()) * 20;
        for (int i = 0; i < Math.min(4, rows.size()); i++) {
            Substance s = rows.get(i);
            int wd = st.withdrawalLevel(s);
            int shake = wd > 0 ? (int) (Mth.sin(t * 38f + i) * wd) : 0;
            int rx = 6 + shake;
            g.fill(rx - 3, vy - 2, rx + 92, vy + 17, wd > 0 ? argb(0.35f + 0.25f * (0.5f + 0.5f * Mth.sin(t * 5f)), 0x601010) : 0x66000000);
            g.renderItem(icon(s), rx, vy);
            g.fill(rx + 19, vy + 1, rx + 19 + 3, vy + 5, 0xFFE0C040);
            segments(g, rx + 25, vy + 1, 10, st.tol(s), 0xE0C040);
            g.fill(rx + 19, vy + 9, rx + 19 + 3, vy + 13, 0xFFE05050);
            segments(g, rx + 25, vy + 9, 10, st.dep(s), 0xE05050);
            vy += 20;
        }
    }
}
