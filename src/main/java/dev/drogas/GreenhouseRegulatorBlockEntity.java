package dev.drogas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Reservatorios da Estufa Reguladora: combustivel e agua (consumo de 20 unidades por segundo enquanto ativa). */
public class GreenhouseRegulatorBlockEntity extends BlockEntity {
    public static final int FUEL_PER_ITEM = 8000, WATER_PER_BUCKET = 12000, MAX = 24000;
    private int fuel, water;

    public GreenhouseRegulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REGULATOR, pos, state);
    }

    public int fuelPercent() { return fuel * 100 / MAX; }
    public int waterPercent() { return water * 100 / MAX; }
    public void addFuel() { fuel = Math.min(MAX, fuel + FUEL_PER_ITEM); setChanged(); }
    public void addWater() { water = Math.min(MAX, water + WATER_PER_BUCKET); setChanged(); }
    public boolean needsWater() { return water + WATER_PER_BUCKET <= MAX; }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Fuel", fuel);
        output.putInt("Water", water);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fuel = input.getIntOr("Fuel", 0);
        water = input.getIntOr("Water", 0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GreenhouseRegulatorBlockEntity be) {
        if (level.getGameTime() % 20 != 0) return;
        if (be.fuel > 0 && be.water > 0) {
            be.fuel = Math.max(0, be.fuel - 20);
            be.water = Math.max(0, be.water - 20);
            be.setChanged();
        }
        boolean lit = be.fuel > 0 && be.water > 0;
        if (state.getValue(GreenhouseRegulatorBlock.LIT) != lit) level.setBlock(pos, state.setValue(GreenhouseRegulatorBlock.LIT, lit), 3);
    }
}
