package dev.drogas;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Maquina v2.0: ate 3 entradas, combustivel (conforme o tipo), slot de melhoria, saida, desgaste/manutencao,
 * qualidade do produto, integracao com hoppers (WorldlyContainer) e descoberta de receitas por experimentacao.
 */
public class MachineBlockEntity extends BlockEntity implements ImplementedContainer, WorldlyContainer, MenuProvider {
    public static final int WEAR_MAX = 1000, WEAR_DIRTY = 500, WEAR_WORN = 800;
    public static final int STATUS_IDLE = 0, STATUS_WORKING = 1, STATUS_NO_FUEL = 2, STATUS_FULL = 3, STATUS_BROKEN = 4, STATUS_NO_SUN = 5;
    private static final float[] SPEED = {1.0f, 1.5f, 2.0f, 3.0f};
    private static final int[] FUEL_USE = {1, 2, 2, 3};

    private static Map<Item, Integer> FUELS;

    public static synchronized int fuelValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (FUELS == null) {
            FUELS = new HashMap<>();
            FUELS.put(Items.COAL, 1600);
            FUELS.put(Items.CHARCOAL, 1600);
            FUELS.put(Items.BLAZE_POWDER, 1000);
            FUELS.put(Items.BLAZE_ROD, 2400);
        }
        return FUELS.getOrDefault(stack.getItem(), 0);
    }

    public static boolean isFuel(ItemStack stack) { return fuelValue(stack) > 0; }

    private final MachineType type;
    private final NonNullList<ItemStack> items;
    private final int[] inputSlots, sideSlots, outSlots;
    private float progress;
    private int maxProgress, burn, burnTotal, wear, speedPct = 100, status, cycleQuality = -1;
    private boolean dirtyRecipe = true;
    private MachineRecipes.Recipe recipe;
    private UUID lastUser;

    /** Sincronizado com o menu: progresso, total, comb., comb. total, desgaste, nivel, receita+1, estado, velocidade%. */
    public final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> (int) progress; case 1 -> maxProgress; case 2 -> burn; case 3 -> burnTotal; case 4 -> wear;
                case 5 -> ModItems.moduleTier(items.get(type.upgradeSlot)); case 6 -> recipe == null ? 0 : MachineRecipes.indexOf(recipe) + 1;
                case 7 -> status; case 8 -> speedPct; default -> 0;
            };
        }
        @Override public void set(int i, int v) { }
        @Override public int getCount() { return 9; }
    };

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE, pos, state);
        this.type = state.getBlock() instanceof MachineBlock mb ? mb.type : MachineType.TABLE;
        this.items = NonNullList.withSize(type.slotCount, ItemStack.EMPTY);
        inputSlots = new int[type.inputs];
        for (int i = 0; i < type.inputs; i++) inputSlots[i] = i;
        sideSlots = type.hasFuel() ? new int[type.inputs + 1] : inputSlots;
        if (type.hasFuel()) { System.arraycopy(inputSlots, 0, sideSlots, 0, type.inputs); sideSlots[type.inputs] = type.fuelSlot; }
        outSlots = new int[]{type.outSlot};
    }

    public MachineType type() { return type; }

    @Override
    public NonNullList<ItemStack> getItems() { return items; }

    @Override
    public void setChanged() {
        dirtyRecipe = true;      // o conteudo mudou: reavaliar a receita
        super.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("Progress", (int) progress);
        output.putInt("Burn", burn);
        output.putInt("BurnTotal", burnTotal);
        output.putInt("Wear", wear);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        progress = input.getIntOr("Progress", 0);
        burn = input.getIntOr("Burn", 0);
        burnTotal = input.getIntOr("BurnTotal", 0);
        wear = input.getIntOr("Wear", 0);
        dirtyRecipe = true;
    }

    // ------------------------------------------------------------------ hoppers / slots
    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) { case DOWN -> outSlots; case UP -> inputSlots; default -> sideSlots; };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return canPlaceItem(slot, stack); }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == type.outSlot; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == type.outSlot) return false;
        if (slot == type.fuelSlot) return isFuel(stack);
        if (slot == type.upgradeSlot) return ModItems.moduleTier(stack) > 0;
        return ModItems.moduleTier(stack) == 0 && !stack.is(ModItems.get("maintenance_kit"));
    }

    @Override
    public boolean stillValid(Player player) {
        Level lvl = getLevel();
        BlockPos p = getBlockPos();
        return lvl != null && lvl.getBlockEntity(p) == this && player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        lastUser = player.getUUID();
        if (player instanceof ServerPlayer sp) Knowledge.unlock(sp, "equip." + type.id);
        return new MachineMenu(containerId, inventory, type, this, data);
    }

    /** Manutencao com o Kit: remove 60% do desgaste. */
    public boolean service(Player player, ItemStack kit) {
        if (wear < 50) { player.displayClientMessage(Component.translatable("message.drogas.no_wear"), true); return false; }
        wear = Math.max(0, wear - 600);
        if (!player.getAbilities().instabuild) kit.shrink(1);
        lastUser = player.getUUID();
        player.displayClientMessage(Component.translatable("message.drogas.serviced"), true);
        Level lvl = getLevel();
        BlockPos bp = getBlockPos();
        if (lvl != null) ModSounds.play(lvl, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, "item.rustle", SoundSource.BLOCKS, 0.8f, 1.0f);
        if (player instanceof ServerPlayer sp) Knowledge.unlock(sp, "guide.maintenance", "equip.maintenance_kit");
        super.setChanged();
        return true;
    }

    // ------------------------------------------------------------------ logica
    private float wearFactor() {
        return wear >= WEAR_MAX ? 0f : wear >= WEAR_WORN ? 0.65f : wear >= WEAR_DIRTY ? 0.85f : 1.0f;
    }

    private void refreshRecipe() {
        MachineRecipes.Recipe next = MachineRecipes.find(type, items);
        if (next != recipe) { progress = 0; cycleQuality = -1; }
        recipe = next;
        dirtyRecipe = false;
        maxProgress = recipe == null ? 0 : recipe.ticks();
    }

    private int computeQuality(MachineRecipes.Recipe r, int tier, RandomSource rnd) {
        int[] slots = r.match(items);
        int sum = 0, n = 0;
        if (slots != null) for (int s : slots) { ItemStack st = items.get(s); if (Quality.capable(st.getItem())) { sum += Quality.of(st); n++; } }
        int q = n == 0 ? Quality.STANDARD : Math.round((float) sum / n);
        if (wear >= WEAR_WORN) q--;
        else if (wear < 100 && rnd.nextInt(5) == 0) q++;
        if (tier == 3 && rnd.nextInt(4) == 0) q--;
        return Mth.clamp(q, Quality.POOR, Quality.EXCELLENT);
    }

    private boolean canOutput(MachineRecipes.Recipe r) {
        ItemStack out = items.get(type.outSlot);
        if (out.isEmpty()) return true;
        if (!out.is(r.output()) || out.getCount() + r.outCount() > out.getMaxStackSize()) return false;
        return !Quality.capable(r.output()) || Quality.of(out) == cycleQuality;
    }

    private void craft(ServerLevel level, MachineRecipes.Recipe r, int tier) {
      int[] slots = r.match(items);
      if (slots == null) return;
      for (int k = 0; k < slots.length; k++) items.get(slots[k]).shrink(r.inputs().get(k).count());

      int madeQuality = cycleQuality;
      ItemStack out = items.get(type.outSlot);
        if (out.isEmpty()) {
            ItemStack made = new ItemStack(r.output(), r.outCount());
            if (Quality.capable(r.output())) Quality.with(made, madeQuality);
            items.set(type.outSlot, made);
        } else {
            out.grow(r.outCount());
        }
       
        int before = wear;
        wear = Math.min(WEAR_MAX, wear + Math.round(type.wearPerCraft * (1f + 0.25f * tier)));
        cycleQuality = -1;
        BlockPos bp = getBlockPos();
        double x = bp.getX() + 0.5, y = bp.getY() + 0.5, z = bp.getZ() + 0.5;
        ModSounds.play(level, x, y, z, "machine.done", SoundSource.BLOCKS, 0.6f, 1.0f);
        ServerPlayer user = lastUser == null ? null : level.getServer().getPlayerList().getPlayer(lastUser);
        if (user != null) {
            Knowledge.unlock(user, r.id());
            if (wear >= WEAR_DIRTY) Knowledge.unlock(user, "guide.maintenance");
            if (Quality.capable(r.output()) && madeQuality != Quality.STANDARD) Knowledge.unlock(user, "guide.quality");
        }
        if (before < WEAR_MAX && wear >= WEAR_MAX) {
            ModSounds.play(level, x, y, z, "machine.break", SoundSource.BLOCKS, 0.9f, 1.0f);
            if (user != null) user.displayClientMessage(Component.translatable("message.drogas.machine_broken"), true);
        }
        dirtyRecipe = true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MachineBlockEntity be) {
        if (level instanceof ServerLevel sl) be.tick(sl, pos, state);
    }

    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean changed = false;
        int tier = ModItems.moduleTier(items.get(type.upgradeSlot));
        if (dirtyRecipe) refreshRecipe();
        MachineRecipes.Recipe r = recipe;
        long dayTime = level.getDayTime() % 24000L;
        boolean sunny = type == MachineType.DRYER && level.canSeeSky(pos.above()) && !level.isRaining() && (dayTime < 12500L || dayTime > 23500L);
        boolean working = false;
        status = STATUS_IDLE;

        if (wear >= WEAR_MAX) {
            status = STATUS_BROKEN;
        } else if (r != null) {
            if (cycleQuality < 0) cycleQuality = computeQuality(r, tier, level.getRandom());
            if (!canOutput(r)) {
                status = STATUS_FULL;
            } else {
                boolean needsFuel = type.fuelRequired || (type == MachineType.DRYER && !sunny);
                boolean powered = true;
                if (needsFuel) {
                    if (burn <= 0 && type.hasFuel()) {
                        ItemStack fuel = items.get(type.fuelSlot);
                        int v = fuelValue(fuel);
                        if (v > 0) { burn = v; burnTotal = v; fuel.shrink(1); changed = true; }
                    }
                    powered = burn > 0;
                    if (!powered) status = type == MachineType.DRYER ? STATUS_NO_SUN : STATUS_NO_FUEL;
                }
                if (powered) {
                    working = true;
                    status = STATUS_WORKING;
                    if (needsFuel) burn = Math.max(0, burn - FUEL_USE[tier]);
                    float mult = SPEED[tier] * wearFactor() * (sunny ? 1.5f : 1f);
                    speedPct = Math.round(mult * 100f);
                    progress += mult;
                    changed = true;
                    if (progress >= maxProgress) { craft(level, r, tier); progress = 0; }
                    if (level.getGameTime() % 40 == 0) {
                        ModSounds.play(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, type.workSound, SoundSource.BLOCKS, 0.5f, 0.9f + 0.1f * tier);
                    }
                }
            }
        }
        if (!working && progress > 0) { progress = Math.max(0, progress - 2); changed = true; }
        if (!working) speedPct = Math.round(SPEED[tier] * wearFactor() * 100f);
        if (state.getValue(MachineBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, working), 3);
            changed = true;
        }
        if (changed) super.setChanged();    // nao reavalia a receita (super evita o dirtyRecipe do override)
    }
}
