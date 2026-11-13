package dev.drogas;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<MachineBlockEntity> MACHINE = register("machine",
            FabricBlockEntityTypeBuilder.create(MachineBlockEntity::new, ModBlocks.machineBlocks()).build());
    public static final BlockEntityType<StorageCrateBlockEntity> CRATE = register("storage_crate",
            FabricBlockEntityTypeBuilder.create(StorageCrateBlockEntity::new, ModBlocks.crateBlock()).build());

    public static final BlockEntityType<GreenhouseRegulatorBlockEntity> REGULATOR = register("greenhouse_regulator",
            FabricBlockEntityTypeBuilder.create(GreenhouseRegulatorBlockEntity::new, ModBlocks.regulator()).build());

    private static <T extends BlockEntityType<?>> T register(String path, T type) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, DrogasMod.id(path), type);
    }

    public static void init() {
        // carregar a classe registra os tipos
    }
}
