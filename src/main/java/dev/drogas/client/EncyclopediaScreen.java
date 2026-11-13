package dev.drogas.client;

import dev.drogas.CropSpec;
import dev.drogas.DrogasConfig;
import dev.drogas.DrugManager;
import dev.drogas.DrugState;
import dev.drogas.Knowledge;
import dev.drogas.KnowledgeData;
import dev.drogas.MachineRecipes;
import dev.drogas.ModEffects;
import dev.drogas.ModItems;
import dev.drogas.Reg;
import dev.drogas.Substance;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Enciclopedia de Substancias: plantas, equipamentos, substancias, receitas e guias (so o que foi descoberto),
 * a condicao do jogador (tolerancia/dependencia/abstinencia) e opcoes visuais. Usa apenas botoes (sem tratar o mouse
 * diretamente) para ser estavel entre versoes.
 */
public class EncyclopediaScreen extends Screen {
    private static final String[] TABS = {"plants", "equipment", "substances", "recipes", "guides", "status", "options"};
    private static final KnowledgeData.Cat[] CATS = {KnowledgeData.Cat.PLANT, KnowledgeData.Cat.EQUIPMENT, KnowledgeData.Cat.SUBSTANCE, KnowledgeData.Cat.RECIPE, KnowledgeData.Cat.GUIDE};
    private static final int ROW = 14;

    private int tab, scroll, selected;
    private List<KnowledgeData.Entry> list = List.of();
    private int px, py, pw, ph;

    public EncyclopediaScreen(int tab) {
        super(Component.translatable("gui.drogas.title"));
        this.tab = Mth.clamp(tab, 0, TABS.length - 1);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        pw = Math.min(this.width - 12, 410);
        ph = Math.min(this.height - 12, 252);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        rebuild();
    }

    private int rows() { return Math.max(3, (ph - 66) / ROW); }

    private void rebuild() {
        this.clearWidgets();
        int tw = (pw - 12) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            this.addRenderableWidget(Button.builder(Component.translatable("gui.drogas.tab." + TABS[i]), b -> { tab = idx; scroll = 0; selected = 0; rebuild(); })
                    .bounds(px + 6 + i * tw, py + 6, tw - 2, 16).build());
        }
        if (tab < 5) {
            list = new ArrayList<>();
            for (KnowledgeData.Entry e : KnowledgeData.ALL) if (e.cat() == CATS[tab]) list.add(e);
            selected = Mth.clamp(selected, 0, Math.max(0, list.size() - 1));
            Minecraft mc = Minecraft.getInstance();
            for (int r = 0; r < rows(); r++) {
                int idx = scroll + r;
                if (idx >= list.size()) break;
                final int sel = idx;
                KnowledgeData.Entry e = list.get(idx);
                boolean known = mc.player != null && Knowledge.has(mc.player, e.id());
                String label = known ? Component.translatable("enc.drogas." + e.id() + ".name").getString() : Component.translatable("gui.drogas.locked").getString();
                this.addRenderableWidget(Button.builder(Component.literal(this.font.plainSubstrByWidth(label, 122)), b -> selected = sel)
                        .bounds(px + 8, py + 28 + r * ROW, 132, ROW - 1).build());
            }
        }
        if (tab <= 5) {
            this.addRenderableWidget(Button.builder(Component.literal("^"), b -> { scroll = Math.max(0, scroll - rows()); rebuild(); })
                    .bounds(px + 8, py + ph - 24, 62, 16).build());
            this.addRenderableWidget(Button.builder(Component.literal("v"), b -> { scroll = Math.min(Math.max(0, maxScroll()), scroll + rows()); rebuild(); })
                    .bounds(px + 78, py + ph - 24, 62, 16).build());
        }
        if (tab == 6) optionButtons();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.drogas.close"), b -> this.onClose())
                .bounds(px + pw - 78, py + ph - 24, 70, 16).build());
    }

    private int maxScroll() {
        int n = tab < 5 ? list.size() : Substance.values().length;
        return Math.max(0, n - rows());
    }

    private void optionButtons() {
        DrogasConfig c = DrogasConfig.get();
        int x = px + 16, y = py + 32, w = 220;
        this.addRenderableWidget(Button.builder(opt("hud", c.hud), b -> { c.hud = !c.hud; DrogasConfig.save(); rebuild(); }).bounds(x, y, w, 18).build());
        this.addRenderableWidget(Button.builder(opt("sway", c.cameraSway), b -> { c.cameraSway = !c.cameraSway; DrogasConfig.save(); rebuild(); }).bounds(x, y + 22, w, 18).build());
        this.addRenderableWidget(Button.builder(opt("fov", c.fovEffects), b -> { c.fovEffects = !c.fovEffects; DrogasConfig.save(); rebuild(); }).bounds(x, y + 44, w, 18).build());
        this.addRenderableWidget(Button.builder(opt("particles", c.hallucinationParticles), b -> { c.hallucinationParticles = !c.hallucinationParticles; DrogasConfig.save(); rebuild(); }).bounds(x, y + 66, w, 18).build());
        float[] steps = {0f, 0.5f, 1f, 1.5f, 2f};
        this.addRenderableWidget(Button.builder(Component.translatable("gui.drogas.opt.visual", Math.round(c.visualIntensity * 100) + "%"), b -> {
            int next = 0;
            for (int i = 0; i < steps.length; i++) if (Math.abs(steps[i] - c.visualIntensity) < 0.01f) next = (i + 1) % steps.length;
            c.visualIntensity = steps[next]; DrogasConfig.save(); rebuild();
        }).bounds(x, y + 88, w, 18).build());
    }

    private static Component opt(String key, boolean on) {
        return Component.translatable("gui.drogas.opt." + key, Component.translatable(on ? "gui.drogas.opt.on" : "gui.drogas.opt.off"));
    }

    // ------------------------------------------------------------------ desenho
    private int wrap(GuiGraphics g, Component text, int x, int y, int w, int color) {
        for (FormattedCharSequence l : this.font.split(text, w)) { g.drawString(this.font, l, x, y, color, false); y += 10; }
        return y;
    }

    private static Item itemOf(String path) {
        if (path == null || path.isEmpty()) return null;
        Holder<Item> h = Reg.holder(BuiltInRegistries.ITEM, path.contains(":") ? path : "drogas:" + path);
        return h == null ? null : h.value();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        GuiPanel.draw(g, px, py, pw, ph);
        Minecraft mc = Minecraft.getInstance();
        if (tab < 5 && selected - scroll >= 0 && selected - scroll < rows()) {
            g.fill(px + 6, py + 27 + (selected - scroll) * ROW, px + 142, py + 27 + (selected - scroll) * ROW + ROW, 0xFFFFD27C);
        }
        super.render(g, mouseX, mouseY, delta);
        if (mc.player == null) return;
        int dx = px + 148, dy = py + 28, dw = pw - 156;
        if (tab < 5) drawEntry(g, mc, dx, dy, dw);
        else if (tab == 5) drawStatus(g, mc, px + 10, py + 28, pw - 20);
        else drawOptionsNote(g, px + 16, py + 32 + 112, pw - 32);
        int known = 0;
        for (KnowledgeData.Entry e : KnowledgeData.ALL) if (Knowledge.has(mc.player, e.id())) known++;
        g.drawString(this.font, Component.translatable("gui.drogas.tip.knowledge", known, KnowledgeData.ALL.size()).getString(), px + 150, py + ph - 20, 0xFF606060, false);
    }

    private void drawOptionsNote(GuiGraphics g, int x, int y, int w) {
        wrap(g, Component.translatable("gui.drogas.opt.note"), x, y, w, 0xFF404040);
    }

    private void drawEntry(GuiGraphics g, Minecraft mc, int x, int y, int w) {
        if (list.isEmpty() || selected >= list.size()) return;
        KnowledgeData.Entry e = list.get(selected);
        boolean known = Knowledge.has(mc.player, e.id());
        Item icon = itemOf(e.icon());
        if (known && icon != null) g.renderItem(new ItemStack(icon), x, y);
        Component name = known ? Component.translatable("enc.drogas." + e.id() + ".name") : Component.translatable("gui.drogas.locked");
        g.drawString(this.font, name, x + 22, y + 4, 0xFF202020, false);
        y += 24;
        if (!known) {
            wrap(g, Component.translatable("gui.drogas.hint." + switch (e.cat()) {
                case PLANT -> "plant"; case EQUIPMENT -> "equipment"; case SUBSTANCE -> "substance"; case RECIPE -> "recipe"; case GUIDE -> "guide"; }), x, y, w, 0xFF606060);
            return;
        }
        String textKey = "enc.drogas." + e.id() + ".text";
        if (e.cat() != KnowledgeData.Cat.RECIPE) y = wrap(g, Component.translatable(textKey), x, y, w, 0xFF303030) + 4;
        switch (e.cat()) {
            case PLANT -> plantFacts(g, e, x, y, w);
            case SUBSTANCE -> substanceFacts(g, e, x, y, w);
            case RECIPE -> recipeView(g, e, x, y, w);
            default -> { }
        }
    }

    private static String list(Set<CropSpec.Climate> set) {
        List<String> out = new ArrayList<>();
        for (CropSpec.Climate c : CropSpec.Climate.values()) if (set.contains(c)) out.add(Component.translatable("gui.drogas.climate." + c.name().toLowerCase()).getString());
        return String.join(", ", out);
    }

    private void plantFacts(GuiGraphics g, KnowledgeData.Entry e, int x, int y, int w) {
        CropSpec spec = null;
        for (CropSpec s : CropSpec.ALL) if (s.id().equals(e.link())) spec = s;
        if (spec == null) return;
        String soil = switch (spec.soil()) { case FARMLAND -> "farmland"; case DIRT -> "dirt"; case SUBSTRATE -> "substrate"; };
        int c = 0xFF404040;
        y = wrap(g, Component.translatable("gui.drogas.fact.soil", Component.translatable("gui.drogas.soil." + soil)), x, y, w, c);
        if (spec.darkOnly()) y = wrap(g, Component.translatable("gui.drogas.fact.dark"), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.light", spec.minLight(), spec.maxLight()), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.water", Component.translatable(spec.needsWater() ? "gui.drogas.water.yes" : "gui.drogas.water.no")), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.climate", list(spec.ideal())), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.tolerates", list(spec.tolerated())), x, y, w, c);
        String growth = spec.growthChance() <= 5 ? "fast" : spec.growthChance() <= 7 ? "medium" : spec.growthChance() <= 9 ? "slow" : "very_slow";
        wrap(g, Component.translatable("gui.drogas.fact.growth", Component.translatable("gui.drogas.growth." + growth)), x, y, w, c);
    }

    private static String rating(float v, float lo, float mid, float high) {
        return v <= 0f ? "none" : v < lo ? "low" : v < mid ? "mid" : v < high ? "high" : "extreme";
    }

    private void substanceFacts(GuiGraphics g, KnowledgeData.Entry e, int x, int y, int w) {
        Substance s = Substance.byId(e.link());
        if (s == null) return;
        int c = 0xFF404040, secs = s.durationTicks / 20;
        y = wrap(g, Component.translatable("gui.drogas.fact.duration", String.format("%d:%02d", secs / 60, secs % 60)), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.group", Component.translatable("gui.drogas.group." + s.group.name())), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.tolerance", Component.translatable("gui.drogas.rating." + rating(s.tolGain, 0.09f, 0.15f, 0.21f))), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.dependence", Component.translatable("gui.drogas.rating." + rating(s.depGain, 0.07f, 0.13f, 0.19f))), x, y, w, c);
        y = wrap(g, Component.translatable("gui.drogas.fact.overdose", String.format("%.0f", s.odThreshold)), x, y, w, c);
        y = wrap(g, Component.translatable(s.lethal ? "gui.drogas.fact.lethal" : "gui.drogas.fact.nonlethal"), x, y, w, s.lethal ? 0xFF903030 : c);
        List<String> mods = new ArrayList<>();
        if (s.speed != 0) mods.add(Component.translatable("gui.drogas.mod.speed", signed(s.speed * 100)).getString());
        if (s.atkSpeed != 0) mods.add(Component.translatable("gui.drogas.mod.atkspeed", signed(s.atkSpeed * 100)).getString());
        if (s.mining != 0) mods.add(Component.translatable("gui.drogas.mod.mining", signed(s.mining * 100)).getString());
        if (s.damage != 0) mods.add(Component.translatable("gui.drogas.mod.damage", signed(s.damage)).getString());
        if (s.armor != 0) mods.add(Component.translatable("gui.drogas.mod.armor", signed(s.armor)).getString());
        if (s.knockbackRes != 0) mods.add(Component.translatable("gui.drogas.mod.kb", signed(s.knockbackRes)).getString());
        if (s.jump != 0) mods.add(Component.translatable("gui.drogas.mod.jump", signed(s.jump)).getString());
        if (s.reach + s.blockReach != 0) mods.add(Component.translatable("gui.drogas.mod.reach", signed(s.reach + s.blockReach)).getString());
        if (s.safeFall != 0) mods.add(Component.translatable("gui.drogas.mod.fall", signed(s.safeFall)).getString());
        if (s.heal > 0) mods.add(Component.translatable("gui.drogas.mod.heal").getString());
        if (s.hunger > 0) mods.add(Component.translatable("gui.drogas.mod.hunger").getString());
        if (!mods.isEmpty()) wrap(g, Component.translatable("gui.drogas.fact.mods", String.join(", ", mods)), x, y, w, c);
    }

    private static String signed(double v) { return (v >= 0 ? "+" : "") + (Math.abs(v - Math.round(v)) < 0.001 ? String.valueOf(Math.round(v)) : String.format("%.2f", v)); }

    private void recipeView(GuiGraphics g, KnowledgeData.Entry e, int x, int y, int w) {
        int c = 0xFF303030;
        if (e.id().startsWith("rec.machine.")) {
            MachineRecipes.Recipe r = MachineRecipes.byId(e.id());
            if (r == null) return;
            y = wrap(g, Component.translatable("gui.drogas.recipe.machine", Component.translatable("gui.drogas.machine." + r.type().name())), x, y, w, c);
            y = wrap(g, Component.translatable("gui.drogas.recipe.time", String.format("%.0f", r.ticks() / 20f)), x, y, w, c) + 2;
            g.drawString(this.font, Component.translatable("gui.drogas.recipe.ingredients").getString(), x, y, c, false);
            y += 12;
            for (MachineRecipes.Ing ing : r.inputs()) {
                g.renderItem(new ItemStack(ing.item()), x, y);
                g.drawString(this.font, ing.count() + "x " + new ItemStack(ing.item()).getHoverName().getString(), x + 20, y + 4, c, false);
                y += 18;
            }
            g.drawString(this.font, "=> " + r.outCount() + "x " + new ItemStack(r.output()).getHoverName().getString(), x, y + 2, 0xFF206020, false);
        } else {
            KnowledgeData.CraftRecipe cr = null;
            for (KnowledgeData.CraftRecipe k : KnowledgeData.CRAFTS) if (k.id().equals(e.id())) cr = k;
            if (cr == null) return;
            y = wrap(g, Component.translatable("gui.drogas.recipe.machine", Component.translatable("gui.drogas.recipe.craft")), x, y, w, c) + 2;
            g.drawString(this.font, Component.translatable("gui.drogas.recipe.ingredients").getString(), x, y, c, false);
            y += 12;
            java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
            for (String ing : cr.ingredients()) counts.merge(ing, 1, Integer::sum);
            for (java.util.Map.Entry<String, Integer> en : counts.entrySet()) {
                String id = en.getKey();
                Item it = id.startsWith("#") ? null : itemOf(id);
                if (it != null) { g.renderItem(new ItemStack(it), x, y); g.drawString(this.font, en.getValue() + "x " + new ItemStack(it).getHoverName().getString(), x + 20, y + 4, c, false); }
                else g.drawString(this.font, en.getValue() + "x #" + id.substring(id.indexOf(':') + 1), x + 20, y + 4, c, false);
                y += 18;
            }
            Item out = itemOf(cr.result());
            g.drawString(this.font, "=> " + cr.count() + "x " + (out == null ? cr.result() : new ItemStack(out).getHoverName().getString()), x, y + 2, 0xFF206020, false);
        }
    }

    private void drawStatus(GuiGraphics g, Minecraft mc, int x, int y, int w) {
        DrugState st = DrugManager.state(mc.player);
        g.drawString(this.font, Component.translatable("gui.drogas.status.title").getString(), x, y, 0xFF202020, false);
        y += 12;
        int cTol = x + 120, cDep = x + 190, cState = x + 260;
        g.drawString(this.font, Component.translatable("gui.drogas.col.substance").getString(), x, y, 0xFF404040, false);
        g.drawString(this.font, Component.translatable("gui.drogas.col.tolerance").getString(), cTol, y, 0xFF404040, false);
        g.drawString(this.font, Component.translatable("gui.drogas.col.dependence").getString(), cDep, y, 0xFF404040, false);
        g.drawString(this.font, Component.translatable("gui.drogas.col.state").getString(), cState, y, 0xFF404040, false);
        g.fill(x, y + 10, x + w, y + 11, 0xFF555555);
        y += 14;
        Substance[] all = Substance.values();
        for (int i = scroll; i < Math.min(all.length, scroll + rows() - 1); i++) {
            Substance s = all[i];
            boolean active = mc.player.hasEffect(ModEffects.of(s));
            int wd = st.withdrawalLevel(s);
            g.drawString(this.font, Component.translatable(s.translationKey()).getString(), x, y, 0xFF000000 | (s.tint & 0x6F6F6F), false);
            DrugHud.segments(g, cTol, y + 1, 10, st.tol(s), 0xE0C040);
            DrugHud.segments(g, cDep, y + 1, 10, st.dep(s), 0xE05050);
            Component state = active ? Component.translatable("gui.drogas.state.active")
                    : wd > 0 ? Component.translatable("gui.drogas.state.withdrawal." + wd) : Component.translatable("gui.drogas.state.clean");
            g.drawString(this.font, state.getString(), cState, y, active ? 0xFF206020 : wd > 0 ? 0xFF8A2020 : 0xFF404040, false);
            float ratio = st.loadOf(s) / s.odThreshold;
            if (ratio > 0.05f) g.fill(cState, y + 9, cState + Math.round(60f * Math.min(1f, ratio)), y + 11, ratio > 0.6f ? 0xFFFF3030 : 0xFFE0A030);
            y += ROW;
        }
        g.drawString(this.font, Component.translatable("gui.drogas.status.hint").getString(), x, py + ph - 38, 0xFF707070, false);
    }
}
