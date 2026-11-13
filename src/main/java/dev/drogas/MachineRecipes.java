package dev.drogas;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** GERADO por tools/generate2.py - nao edite a mao. Receitas das maquinas (todas ficticias, so itens de jogo). */
public final class MachineRecipes {
    private MachineRecipes() {}

    public record Ing(Item item, int count) {}

    public record Recipe(String id, MachineType type, int ticks, Item output, int outCount, List<Ing> inputs) {
        /** Retorna os indices dos slots de entrada usados, ou null se a receita nao casa. */
        public int[] match(NonNullList<ItemStack> items) {
            int n = type.inputs;
            boolean[] used = new boolean[n];
            int[] slots = new int[inputs.size()];
            for (int k = 0; k < inputs.size(); k++) {
                Ing ing = inputs.get(k);
                int found = -1;
                for (int s = 0; s < n; s++) {
                    ItemStack st = items.get(s);
                    if (!used[s] && !st.isEmpty() && st.is(ing.item()) && st.getCount() >= ing.count()) { found = s; break; }
                }
                if (found < 0) return null;
                used[found] = true; slots[k] = found;
            }
            for (int s = 0; s < n; s++) if (!used[s] && !items.get(s).isEmpty()) return null;
            return slots;
        }
    }

    private static final List<Recipe> ALL = new ArrayList<>();
    private static boolean ready = false;

    private static Ing ing(Item item, int count) { return new Ing(item, count); }
    private static void add(String id, MachineType type, int ticks, Item out, int outCount, Ing... ings) {
        ALL.add(new Recipe(id, type, ticks, out, outCount, List.of(ings)));
    }

    public static List<Recipe> all() { init(); return ALL; }

    public static Recipe byId(String id) {
        init();
        for (Recipe r : ALL) if (r.id().equals(id)) return r;
        return null;
    }

    public static int indexOf(Recipe r) { init(); return ALL.indexOf(r); }
    public static Recipe byIndex(int i) { init(); return i >= 0 && i < ALL.size() ? ALL.get(i) : null; }

    public static Recipe find(MachineType type, NonNullList<ItemStack> items) {
        init();
        for (Recipe r : ALL) if (r.type() == type && r.match(items) != null) return r;
        return null;
    }

    private static synchronized void init() {
        if (ready) return;
        ready = true;
        add("rec.machine.DRYER.dried_cannabis", MachineType.DRYER, 600, ModItems.get("dried_cannabis"), 1, ing(ModItems.get("cannabis_bud"), 1));
        add("rec.machine.DRYER.dried_tobacco", MachineType.DRYER, 600, ModItems.get("dried_tobacco"), 1, ing(ModItems.get("tobacco_leaf"), 1));
        add("rec.machine.DRYER.dried_magic_mushroom", MachineType.DRYER, 600, ModItems.get("dried_magic_mushroom"), 1, ing(ModItems.get("magic_mushroom"), 1));
        add("rec.machine.GRINDER.ground_cannabis", MachineType.GRINDER, 100, ModItems.get("ground_cannabis"), 2, ing(ModItems.get("dried_cannabis"), 1));
        add("rec.machine.GRINDER.ground_tobacco", MachineType.GRINDER, 100, ModItems.get("ground_tobacco"), 2, ing(ModItems.get("dried_tobacco"), 1));
        add("rec.machine.GRINDER.raw_crystal", MachineType.GRINDER, 100, ModItems.get("raw_crystal"), 2, ing(Items.QUARTZ, 1));
        add("rec.machine.TABLE.joint", MachineType.TABLE, 60, ModItems.get("joint"), 1, ing(ModItems.get("ground_cannabis"), 1), ing(ModItems.get("rolling_paper"), 1));
        add("rec.machine.TABLE.cigarette", MachineType.TABLE, 60, ModItems.get("cigarette"), 2, ing(ModItems.get("ground_tobacco"), 1), ing(ModItems.get("rolling_paper"), 1));
        add("rec.machine.TABLE.cigar", MachineType.TABLE, 60, ModItems.get("cigar"), 1, ing(ModItems.get("dried_tobacco"), 2), ing(ModItems.get("tobacco_leaf"), 1));
        add("rec.machine.TABLE.cannabis_brownie", MachineType.TABLE, 80, ModItems.get("cannabis_brownie"), 2, ing(ModItems.get("ground_cannabis"), 2), ing(Items.COCOA_BEANS, 1), ing(Items.WHEAT, 1));
        add("rec.machine.TABLE.lsd_blotter", MachineType.TABLE, 80, ModItems.get("lsd_blotter"), 4, ing(ModItems.get("lsd_solution"), 1), ing(Items.PAPER, 1));
        add("rec.machine.TABLE.mdma_pill", MachineType.TABLE, 80, ModItems.get("mdma_pill"), 3, ing(ModItems.get("chem_base"), 1), ing(ModItems.get("raw_crystal"), 1), ing(Items.SUGAR, 1));
        add("rec.machine.TABLE.nebulon_tab", MachineType.TABLE, 80, ModItems.get("nebulon_tab"), 2, ing(ModItems.get("chem_base"), 1), ing(ModItems.get("raw_crystal"), 1), ing(Items.CHORUS_FRUIT, 1));
        add("rec.machine.TABLE.dmt_dose", MachineType.TABLE, 60, ModItems.get("dmt_dose"), 2, ing(ModItems.get("dmt_crystals"), 1), ing(Items.PAPER, 1));
        add("rec.machine.BENCH.solvent", MachineType.BENCH, 100, ModItems.get("solvent"), 1, ing(Items.COAL, 1), ing(Items.GLASS_BOTTLE, 1), ing(Items.SUGAR, 1));
        add("rec.machine.BENCH.chem_base", MachineType.BENCH, 200, ModItems.get("chem_base"), 2, ing(Items.REDSTONE, 2), ing(ModItems.get("solvent"), 1), ing(Items.BONE_MEAL, 1));
        add("rec.machine.BENCH.catalyst", MachineType.BENCH, 300, ModItems.get("catalyst"), 1, ing(Items.GLOWSTONE_DUST, 2), ing(Items.GUNPOWDER, 1), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.opium_gum", MachineType.BENCH, 200, ModItems.get("opium_gum"), 1, ing(ModItems.get("poppy_pod"), 3), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.morphine_base", MachineType.BENCH, 300, ModItems.get("morphine_base"), 1, ing(ModItems.get("opium_gum"), 2), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.morphine_vial", MachineType.BENCH, 200, ModItems.get("morphine_vial"), 2, ing(ModItems.get("morphine_base"), 1), ing(Items.GLASS_BOTTLE, 1), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.heroin_dose", MachineType.BENCH, 400, ModItems.get("heroin_dose"), 1, ing(ModItems.get("morphine_base"), 1), ing(ModItems.get("catalyst"), 1), ing(ModItems.get("syringe"), 1));
        add("rec.machine.BENCH.coca_paste", MachineType.BENCH, 300, ModItems.get("coca_paste"), 1, ing(ModItems.get("coca_leaf"), 6), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.cocaine_powder", MachineType.BENCH, 300, ModItems.get("cocaine_powder"), 2, ing(ModItems.get("coca_paste"), 1), ing(ModItems.get("chem_base"), 1));
        add("rec.machine.BENCH.crack_rock", MachineType.BENCH, 300, ModItems.get("crack_rock"), 2, ing(ModItems.get("cocaine_powder"), 1), ing(ModItems.get("catalyst"), 1));
        add("rec.machine.BENCH.lsd_solution", MachineType.BENCH, 400, ModItems.get("lsd_solution"), 1, ing(ModItems.get("chem_base"), 1), ing(Items.AMETHYST_SHARD, 1), ing(Items.GLASS_BOTTLE, 1));
        add("rec.machine.BENCH.dmt_crystals", MachineType.BENCH, 400, ModItems.get("dmt_crystals"), 1, ing(ModItems.get("chacrona_leaf"), 4), ing(ModItems.get("solvent"), 1));
        add("rec.machine.BENCH.meth_shard", MachineType.BENCH, 500, ModItems.get("meth_shard"), 2, ing(ModItems.get("raw_crystal"), 2), ing(ModItems.get("catalyst"), 1), ing(ModItems.get("chem_base"), 1));
        add("rec.machine.BENCH.ketamine_vial", MachineType.BENCH, 300, ModItems.get("ketamine_vial"), 1, ing(ModItems.get("chem_base"), 1), ing(ModItems.get("raw_crystal"), 1), ing(Items.GLASS_BOTTLE, 1));
        add("rec.machine.BENCH.zyrax_vial", MachineType.BENCH, 600, ModItems.get("zyrax_vial"), 1, ing(ModItems.get("raw_crystal"), 2), ing(ModItems.get("catalyst"), 1), ing(Items.ENDER_PEARL, 1));
        add("rec.machine.FURNACE.ayahuasca_brew", MachineType.FURNACE, 1200, ModItems.get("ayahuasca_brew"), 1, ing(ModItems.get("jagube_vine"), 2), ing(ModItems.get("chacrona_leaf"), 2), ing(Items.GLASS_BOTTLE, 1));
        add("rec.machine.FURNACE.beer", MachineType.FURNACE, 1200, ModItems.get("beer"), 1, ing(Items.WHEAT, 3), ing(Items.GLASS_BOTTLE, 1));
        add("rec.machine.FURNACE.wine", MachineType.FURNACE, 1200, ModItems.get("wine"), 1, ing(Items.SWEET_BERRIES, 4), ing(Items.GLASS_BOTTLE, 1));
        add("rec.machine.FURNACE.spirit", MachineType.FURNACE, 1600, ModItems.get("spirit"), 1, ing(Items.SUGAR_CANE, 4), ing(Items.GLASS_BOTTLE, 1));
    }
}
