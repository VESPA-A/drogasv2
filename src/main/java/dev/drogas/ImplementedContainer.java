package dev.drogas;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Container simples apoiado numa NonNullList (padrao da documentacao do Fabric). */
public interface ImplementedContainer extends Container {
    NonNullList<ItemStack> getItems();

    @Override
    default int getContainerSize() { return getItems().size(); }

    @Override
    default boolean isEmpty() {
        for (ItemStack s : getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    default ItemStack getItem(int slot) { return getItems().get(slot); }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(getItems(), slot); }

    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        setChanged();
    }

    @Override
    default void clearContent() { getItems().clear(); }

    @Override
    default void setChanged() { }

    @Override
    default boolean stillValid(Player player) { return true; }
}
