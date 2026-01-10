package me.holypite.manager;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

public class EntityLoader {

    public void loadEntities(Instance instance, Path mapPath) {
        Path entitiesPath = mapPath.resolve("entities");
        if (!entitiesPath.toFile().exists()) return;

        File[] files = entitiesPath.toFile().listFiles((dir, name) -> name.endsWith(".mca"));
        if (files == null) return;

        for (File file : files) {
            try {
                loadMcaFile(instance, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMcaFile(Instance instance, File file) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (int i = 0; i < 1024; i++) {
                raf.seek(i * 4);
                int offset = raf.readInt();
                if (offset == 0) continue;

                int sectorOffset = (offset >> 8) * 4096;
                int sectorLength = (offset & 0xFF) * 4096;

                raf.seek(sectorOffset);
                int length = raf.readInt();
                int compressionType = raf.readByte(); // 2 = Zlib

                if (compressionType != 2) continue; // Only handle Zlib for now

                byte[] compressedData = new byte[length - 1];
                raf.readFully(compressedData);

                try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
                     InflaterInputStream iis = new InflaterInputStream(bis)) {
                    
                    // Fixed: No asCompound() if read returns CompoundBinaryTag? 
                    // Actually read() returns Map.Entry<String, CompoundBinaryTag> in some versions, or BinaryTag.
                    // Let's assume it returns CompoundBinaryTag directly or cast.
                    CompoundBinaryTag root = (CompoundBinaryTag) BinaryTagIO.reader().read(iis, BinaryTagIO.Compression.NONE);
                    
                    processChunkData(instance, root);
                }
            }
        }
    }

    private void processChunkData(Instance instance, CompoundBinaryTag root) {
        // "Position" [x, z]
        ListBinaryTag entities = root.getList("Entities");
        if (entities.size() == 0) return;

        for (net.kyori.adventure.nbt.BinaryTag tag : entities) {
            if (tag instanceof CompoundBinaryTag entityTag) {
                spawnEntity(instance, entityTag);
            }
        }
    }

    private void spawnEntity(Instance instance, CompoundBinaryTag tag) {
        String id = tag.getString("id");
        if (id.isEmpty()) return;

        // Corrected: Use registry or valueOf if fromNamespaceId is missing
        EntityType type = null;
        try {
            // Remove "minecraft:" if needed for valueOf, but namespace is better
            // Trying fromNamespaceId again? No, it failed.
            // Let's iterate values.
            for (EntityType t : EntityType.values()) {
                if (t.name().equalsIgnoreCase(id) || t.key().asString().equalsIgnoreCase(id)) {
                    type = t;
                    break;
                }
            }
        } catch (Exception e) {}

        if (type == null) {
            // System.out.println("Unknown entity type: " + id);
            return;
        }

        // Pos
        ListBinaryTag posTag = tag.getList("Pos");
        ListBinaryTag rotTag = tag.getList("Rotation");
        if (posTag.size() < 3) return;

        double x = posTag.getDouble(0);
        double y = posTag.getDouble(1);
        double z = posTag.getDouble(2);
        
        float yaw = rotTag.size() > 0 ? rotTag.getFloat(0) : 0;
        float pitch = rotTag.size() > 1 ? rotTag.getFloat(1) : 0;

        Entity entity = new Entity(type);
        entity.setInstance(instance, new Pos(x, y, z, yaw, pitch));

        // Metadata Parsing (Basic)
        if (id.equals("minecraft:block_display")) {
            CompoundBinaryTag blockState = tag.getCompound("block_state");
            String blockName = blockState.getString("Name");
            if (!blockName.isEmpty()) {
                // Corrected: Block.fromNamespaceId might be Block.fromNamespace
                Block block = null;
                for (Block b : Block.values()) { // Slow but works
                    if (b.name().equalsIgnoreCase(blockName) || b.key().asString().equalsIgnoreCase(blockName)) {
                        block = b;
                        break;
                    }
                }
                
                if (block != null) {
                    ((BlockDisplayMeta) entity.getEntityMeta()).setBlockState(block);
                }
            }
            
            // Transformation (Scale, Translation, Rotation)
            // This is complex (matrix/quaternion), Minestom supports it via setTransformation
            // Parsing "transformation" tag: [translation, left_rotation, scale, right_rotation]
            // Each is a list of floats.
            
            // TODO: Parse transformation if needed
        }
        
        // Add other display types if needed
    }
}