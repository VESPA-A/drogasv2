package dev.drogas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    private ModBlocks() {}

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final List<Block> MACHINES = new ArrayList<>();
    private static final List<Block> CUTOUT = new ArrayList<>();
    private static Block crate, regulator, climber;

    public static Block crop(String blockId) {
        Block b = BLOCKS.get(blockId);
        if (b == null) throw new IllegalStateException("Bloco desconhecido: " + blockId);
        return b;
    }

    public static Block[] machineBlocks() { return MACHINES.toArray(new Block[0]); }
    public static Block crateBlock() { return crate; }
    public static Block regulator() { return regulator; }
    public static Block climber() { return climber; }
    /** Blocos com textura transparente (plantas, videira, trelica): registrados na camada CUTOUT no cliente. */
    public static List<Block> cutoutBlocks() { return CUTOUT; }

    private static BlockBehaviour.Properties props(String id) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, DrogasMod.id(id)));
    }

    private static <B extends Block> B register(String id, B block, boolean withItem) {
        Registry.register(BuiltInRegistries.BLOCK, DrogasMod.id(id), block);
        BLOCKS.put(id, block);
        if (withItem) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DrogasMod.id(id));
            BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, DrogasMod.id(id), item);
            ModItems.putBlockItem(id, item);
        }
        return block;
    }

    private static void machine(MachineType type, SoundType sound, float hardness) {
        BlockBehaviour.Properties p = props(type.id).strength(hardness).sound(sound)
                .lightLevel(state -> state.getValue(MachineBlock.LIT) ? 10 : 0);
        MACHINES.add(register(type.id, new MachineBlock(p, type), true));
    }

    public static void init() {
        for (CropSpec spec : CropSpec.ALL) {
            BlockBehaviour.Properties p = props(spec.blockId()).noCollision().randomTicks().instabreak()
                    .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY);
            CUTOUT.add(register(spec.blockId(), new DrugCropBlock(p, spec), false));
        }
        BlockBehaviour.Properties vine = props("jagube_climber").noCollision().randomTicks().strength(0.2f)
                .sound(SoundType.VINE).pushReaction(PushReaction.DESTROY);
        climber = register("jagube_climber", new JagubeClimberBlock(vine), false);
        CUTOUT.add(climber);

        machine(MachineType.TABLE, SoundType.WOOD, 2.5f);
        machine(MachineType.BENCH, SoundType.METAL, 3.0f);
        machine(MachineType.DRYER, SoundType.WOOD, 2.0f);
        machine(MachineType.GRINDER, SoundType.METAL, 3.0f);
        machine(MachineType.FURNACE, SoundType.STONE, 3.0f);
        crate = register("storage_crate", new StorageCrateBlock(props("storage_crate").strength(2.0f).sound(SoundType.WOOD)), true);

        regulator = register("greenhouse_regulator", new GreenhouseRegulatorBlock(props("greenhouse_regulator").strength(2.5f)
                .sound(SoundType.METAL).lightLevel(state -> state.getValue(GreenhouseRegulatorBlock.LIT) ? 8 : 0)), true);
        CUTOUT.add(register("trellis", new Block(props("trellis").strength(1.0f).sound(SoundType.WOOD).noOcclusion()), true));
        register("organic_substrate", new Block(props("organic_substrate").strength(0.6f).sound(SoundType.GRAVEL)), true);
        for (String kind : new String[]{"lab", "greenhouse", "researcher", "depot"}) {
            register("field_notes_" + kind, new FieldNotesBlock(props("field_notes_" + kind).strength(1.5f).sound(SoundType.WOOD), kind), true);
        }
    }
}
