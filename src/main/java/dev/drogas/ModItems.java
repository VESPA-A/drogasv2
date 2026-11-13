package dev.drogas;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

/** Registra todos os itens a partir do ItemCatalog (gerado). Tambem guarda os itens de bloco (para loot/menus). */
public final class ModItems implements ItemCatalog.Definer {
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static Item module1, module2, module3;

    private ModItems() {}

    public static void init() {
        ItemCatalog.define(new ModItems());
    }

    public static Item get(String path) {
        Item i = ITEMS.get(path);
        if (i == null) throw new IllegalStateException("Item desconhecido: " + path);
        return i;
    }

    public static void putBlockItem(String id, Item item) { ITEMS.put(id, item); }
    public static Collection<Item> all() { return ITEMS.values(); }
    public static int count() { return ITEMS.size(); }

    /** Nivel (1-3) de um modulo de velocidade; 0 se nao for um modulo. */
    public static int moduleTier(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (module1 == null) { module1 = ITEMS.get("speed_module_1"); module2 = ITEMS.get("speed_module_2"); module3 = ITEMS.get("speed_module_3"); }
        Item i = stack.getItem();
        return i == module1 ? 1 : i == module2 ? 2 : i == module3 ? 3 : 0;
    }

    private static Item.Properties props(String id, int stack) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DrogasMod.id(id));
        return new Item.Properties().setId(key).stacksTo(stack);
    }

    private static <T extends Item> T reg(String id, T item) {
        Registry.register(BuiltInRegistries.ITEM, DrogasMod.id(id), item);
        ITEMS.put(id, item);
        return item;
    }

    private static Consumable custom(float seconds, ItemUseAnimation anim, String sound, String after) {
        return Consumable.builder().consumeSeconds(seconds).animation(anim).sound(ModSounds.holder(sound))
                .soundAfterConsume(ModSounds.holder(after)).hasConsumeParticles(false).build();
    }

    /** Animacao/sons de consumo por forma: pilula, bebida, fumar, cheirar, injetar, mastigar. */
    private static Consumable consumable(ItemCatalog.Form form) {
        return switch (form) {
            case PILL -> custom(1.0f, ItemUseAnimation.EAT, "item.pill", "item.swallow");
            case DRINK -> Consumables.defaultDrink().build();
            case CHEW -> Consumables.defaultFood().build();
            case SMOKE -> custom(2.4f, ItemUseAnimation.TOOT_HORN, "item.tick_puff", "item.exhale");
            case SNORT -> custom(1.2f, ItemUseAnimation.SPYGLASS, "item.tick_puff", "item.snort");
            case INJECT -> custom(1.4f, ItemUseAnimation.BRUSH, "item.rustle", "item.inject");
        };
    }

    private static Item.Properties withConsumable(Item.Properties p, Consumable c) {
        return p.component(DataComponents.CONSUMABLE, c);
    }

    @Override public void plain(String id, int stack) { reg(id, new Item(props(id, stack))); }

    @Override public void seeds(String id, String cropBlockId) { reg(id, new BlockItem(ModBlocks.crop(cropBlockId), props(id, 64))); }

    @Override
    public void drug(String id, String substanceId, float dose, ItemCatalog.Form form, ItemCatalog.Rem remainder, int stack) {
        Substance s = Substance.byId(substanceId);
        if (s == null) throw new IllegalStateException("Substância desconhecida no catálogo: " + substanceId);
        reg(id, new DrugItem(withConsumable(props(id, stack), consumable(form)), s, dose, form, remainder));
    }

    @Override public void clipboard(String id) { reg(id, new ClipboardItem(props(id, 1))); }

    @Override public void antidote(String id, int stack) { reg(id, new TreatmentItem(withConsumable(props(id, stack), custom(1.2f, ItemUseAnimation.EAT, "item.pill", "item.swallow")), TreatmentItem.Kind.EMERGENCY)); }

    @Override public void detox(String id, int stack) { reg(id, new TreatmentItem(withConsumable(props(id, stack), Consumables.defaultDrink().build()), TreatmentItem.Kind.DETOX)); }

    @Override public void module(String id, int tier) { reg(id, new Item(props(id, 16))); }

    @Override public void doc(String id) { reg(id, new DocumentItem(props(id, 16), id)); }

    @Override public void fertilizer(String id) { reg(id, new FertilizerItem(props(id, 64))); }

    @Override public void encyclopedia(String id) { reg(id, new EncyclopediaItem(props(id, 1))); }
}
