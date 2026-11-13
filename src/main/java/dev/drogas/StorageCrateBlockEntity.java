package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Recipiente de armazenamento (27 slots, usa a interface de bau vanilla). */
public class StorageCrateBlockEntity extends BlockEntity implements ImplementedContainer, MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public StorageCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRATE, pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
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
        return ChestMenu.threeRows(containerId, inventory, this);
    }
}
