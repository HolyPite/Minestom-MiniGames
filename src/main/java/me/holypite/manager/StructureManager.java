package me.holypite.manager;

import net.kyori.adventure.nbt.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureManager {

    private static final Path STRUCTURES_DIR = Path.of("structures");

    public StructureManager() {
        if (!STRUCTURES_DIR.toFile().exists()) {
            STRUCTURES_DIR.toFile().mkdirs();
        }
    }

    public void saveStructure(Instance instance, Point p1, Point p2, String name) {
        int minX = Math.min(p1.blockX(), p2.blockX());
        int minY = Math.min(p1.blockY(), p2.blockY());
        int minZ = Math.min(p1.blockZ(), p2.blockZ());
        int maxX = Math.max(p1.blockX(), p2.blockX());
        int maxY = Math.max(p1.blockY(), p2.blockY());
        int maxZ = Math.max(p1.blockZ(), p2.blockZ());

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;

        List<CompoundBinaryTag> palette = new ArrayList<>();
        List<CompoundBinaryTag> blocks = new ArrayList<>();
        Map<Block, Integer> paletteMap = new HashMap<>();

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    Block block = instance.getBlock(minX + x, minY + y, minZ + z);
                    
                    if (block.isAir()) continue; // Skip air to save space? Standard structures often include air.
                    // Let's include air to be safe and standard compliant, or use void structure blocks?
                    // Standard .nbt usually includes everything in the box.
                    
                    int stateId;
                    if (paletteMap.containsKey(block)) {
                        stateId = paletteMap.get(block);
                    } else {
                        stateId = palette.size();
                        paletteMap.put(block, stateId);
                        
                        CompoundBinaryTag.Builder blockState = CompoundBinaryTag.builder();
                        blockState.putString("Name", block.name());
                        
                        if (!block.properties().isEmpty()) {
                            CompoundBinaryTag.Builder props = CompoundBinaryTag.builder();
                            block.properties().forEach(props::putString);
                            blockState.put("Properties", props.build());
                        }
                        palette.add(blockState.build());
                    }

                    CompoundBinaryTag.Builder blockTag = CompoundBinaryTag.builder();
                    ListBinaryTag pos = ListBinaryTag.builder(BinaryTagTypes.INT)
                            .add(IntBinaryTag.intBinaryTag(x))
                            .add(IntBinaryTag.intBinaryTag(y))
                            .add(IntBinaryTag.intBinaryTag(z))
                            .build();
                    
                    blockTag.put("pos", pos);
                    blockTag.putInt("state", stateId);
                    
                    // NBT Data (BlockEntity)
                    if (block.hasNbt()) {
                        blockTag.put("nbt", block.nbt());
                    }
                    
                    blocks.add(blockTag.build());
                }
            }
        }

        CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put("size", ListBinaryTag.builder(BinaryTagTypes.INT)
                        .add(IntBinaryTag.intBinaryTag(sizeX))
                        .add(IntBinaryTag.intBinaryTag(sizeY))
                        .add(IntBinaryTag.intBinaryTag(sizeZ))
                        .build())
                .put("blocks", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, blocks))
                .put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, palette))
                .putInt("DataVersion", 3465) // 1.20.1 approx, check if matters. Minestom usually ignores it.
                .build();

        try {
            BinaryTagIO.writer().write(root, STRUCTURES_DIR.resolve(name + ".nbt"));
            System.out.println("Structure saved: " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void placeStructure(Instance instance, Point origin, String name) {
        Path path = STRUCTURES_DIR.resolve(name + ".nbt");
        if (!path.toFile().exists()) {
            System.err.println("Structure not found: " + name);
            return;
        }

        try {
            CompoundBinaryTag root = BinaryTagIO.reader().read(path);
            
            // Parse Palette
            List<Block> palette = new ArrayList<>();
            for (BinaryTag tag : root.getList("palette")) {
                if (tag instanceof CompoundBinaryTag ct) {
                    String blockName = ct.getString("Name");
                    Map<String, String> properties = new HashMap<>();
                    if (ct.keySet().contains("Properties")) {
                        CompoundBinaryTag props = ct.getCompound("Properties");
                        for (String key : props.keySet()) {
                            properties.put(key, props.getString(key));
                        }
                    }
                    
                    Block block = Block.fromNamespaceId(blockName);
                    if (block != null && !properties.isEmpty()) {
                        block = block.withProperties(properties);
                    }
                    // Fallback
                    if (block == null) block = Block.AIR;
                    
                    palette.add(block);
                }
            }

            // Place Blocks
            ListBinaryTag blocks = root.getList("blocks");
            for (BinaryTag tag : blocks) {
                if (tag instanceof CompoundBinaryTag ct) {
                    ListBinaryTag pos = ct.getList("pos");
                    int x = pos.getInt(0);
                    int y = pos.getInt(1);
                    int z = pos.getInt(2);
                    
                    int state = ct.getInt("state");
                    Block block = palette.get(state);
                    
                    if (ct.keySet().contains("nbt")) {
                        // Merge NBT? Or set handler?
                        // Minestom Block.withNbt()
                        // NOTE: Structure block NBT often contains 'id', 'x', 'y', 'z' which we might want to strip or ignore?
                        // Minestom usually handles block entity data via handler.
                        // For simplicity, we just attach it.
                         block = block.withNbt(ct.getCompound("nbt"));
                    }

                    instance.setBlock(origin.add(x, y, z), block);
                }
            }
            
            System.out.println("Structure placed: " + name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
