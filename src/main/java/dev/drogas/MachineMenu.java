package dev.drogas;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menu generico das maquinas (layout depende do MachineType). */
public class MachineMenu extends AbstractContainerMenu {
    public final MachineType type;
    private final Container container;
    private final ContainerData data;

    /** Lado do cliente. */
    public MachineMenu(int id, Inventory inventory, MachineType type) {
        this(id, inventory, type, new SimpleContainer(type.slotCount), new SimpleContainerData(9));
    }

    public MachineMenu(int id, Inventory inventory, MachineType type, Container container, ContainerData data) {
        super(ModMenus.get(type), id);
        this.type = type;
        this.container = container;
        this.data = data;
        checkContainerSize(container, type.slotCount);
        int[][] pos = layout(type);
        for (int s = 0; s < type.slotCount; s++) {
            if (s == type.outSlot) {
                addSlot(new Slot(container, s, pos[s][0], pos[s][1]) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            } else if (s == type.fuelSlot) {
                addSlot(new Slot(container, s, pos[s][0], pos[s][1]) {
                    @Override public boolean mayPlace(ItemStack stack) { return MachineBlockEntity.isFuel(stack); }
                });
            } else if (s == type.upgradeSlot) {
                addSlot(new Slot(container, s, pos[s][0], pos[s][1]) {
                    @Override public boolean mayPlace(ItemStack stack) { return ModItems.moduleTier(stack) > 0; }
                    @Override public int getMaxStackSize() { return 1; }
                });
            } else {
                addSlot(new Slot(container, s, pos[s][0], pos[s][1]));
            }
        }
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    /** Posicao (x, y) de cada slot, indexada pelo indice do slot da maquina. */
    public static int[][] layout(MachineType t) {
        return switch (t) {
            case TABLE -> new int[][]{{34, 17}, {34, 35}, {34, 53}, {124, 31}, {124, 57}};
            case BENCH -> new int[][]{{34, 17}, {34, 35}, {34, 53}, {124, 31}, {79, 57}, {124, 57}};
            case DRYER -> new int[][]{{45, 31}, {124, 31}, {79, 57}, {124, 57}};
            case GRINDER -> new int[][]{{45, 31}, {124, 31}, {124, 57}};
            case FURNACE -> new int[][]{{28, 17}, {46, 17}, {64, 17}, {46, 53}, {124, 31}, {124, 57}};
        };
    }

    public int progress() { return data.get(0); }
    public int maxProgress() { return data.get(1); }
    public int burn() { return data.get(2); }
    public int burnTotal() { return data.get(3); }
    public int wear() { return data.get(4); }
    public int tier() { return data.get(5); }
    public int recipeIndex() { return data.get(6) - 1; }
    public int status() { return data.get(7); }
    public int speedPercent() { return data.get(8); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int machineSlots = type.slotCount;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (ModItems.moduleTier(stack) > 0) {
            if (!moveItemStackTo(stack, type.upgradeSlot, type.upgradeSlot + 1, false)) return ItemStack.EMPTY;
        } else if (type.hasFuel() && MachineBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, type.fuelSlot, type.fuelSlot + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, type.inputs, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
