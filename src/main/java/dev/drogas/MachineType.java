package dev.drogas;

/**
 * Tipos de maquina. Ordem dos slots (compativel com saves da v1.0): entradas, [combustivel/saida...], melhoria.
 * Indices: TABLE e GRINDER nao usam combustivel; BENCH e FURNACE exigem; DRYER usa so de noite/chuva.
 */
public enum MachineType {
    //            id                  in out fuel upg count fuelReq wear soundDeTrabalho
    TABLE("processing_table", 3, 3, -1, 4, 5, false, 6, "machine.hum"),
    BENCH("chemistry_bench", 3, 3, 4, 5, 6, true, 10, "machine.bubble"),
    DRYER("dryer", 1, 1, 2, 3, 4, false, 8, "machine.fan"),
    GRINDER("grinder", 1, 1, -1, 2, 3, false, 14, "machine.grind"),
    FURNACE("processing_furnace", 3, 4, 3, 5, 6, true, 10, "machine.fire");

    public final String id;
    public final int inputs, outSlot, fuelSlot, upgradeSlot, slotCount, wearPerCraft;
    public final boolean fuelRequired;
    public final String workSound;

    MachineType(String id, int inputs, int outSlot, int fuelSlot, int upgradeSlot, int slotCount, boolean fuelRequired, int wearPerCraft, String workSound) {
        this.id = id; this.inputs = inputs; this.outSlot = outSlot; this.fuelSlot = fuelSlot; this.upgradeSlot = upgradeSlot;
        this.slotCount = slotCount; this.fuelRequired = fuelRequired; this.wearPerCraft = wearPerCraft; this.workSound = workSound;
    }

    public boolean hasFuel() { return fuelSlot >= 0; }
}
