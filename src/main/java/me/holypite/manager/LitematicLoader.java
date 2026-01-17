package me.holypite.manager;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.util.*;

public class LitematicLoader {

    public static StructureManager.StructureData load(CompoundBinaryTag root) {
        // Litematic stores regions in "Regions" tag
        CompoundBinaryTag regions = root.getCompound("Regions");
        if (regions.keySet().isEmpty()) return null;

        // We only load the first region found (standard for single builds)
        String regionName = regions.keySet().iterator().next();
        CompoundBinaryTag region = regions.getCompound(regionName);

        // Get Dimensions
        CompoundBinaryTag size = region.getCompound("Size");
        int width = Math.abs(size.getInt("x"));  // Width (X)
        int height = Math.abs(size.getInt("y")); // Height (Y)
        int length = Math.abs(size.getInt("z")); // Length (Z)

        // Palette
        ListBinaryTag paletteTag = region.getList("BlockStatePalette");
        List<Block> palette = new ArrayList<>();
        for (var tag : paletteTag) {
            CompoundBinaryTag blockTag = (CompoundBinaryTag) tag;
            String name = blockTag.getString("Name");
            Block block = null;
            for (Block b : Block.values()) {
                if (b.name().equalsIgnoreCase(name) || b.key().asString().equalsIgnoreCase(name)) {
                    block = b;
                    break;
                }
            }
            
            if (block == null) block = Block.AIR;

            if (blockTag.keySet().contains("Properties")) {
                Map<String, String> properties = new HashMap<>();
                CompoundBinaryTag props = blockTag.getCompound("Properties");
                for (String key : props.keySet()) {
                    properties.put(key, props.getString(key));
                }
                try {
                    block = block.withProperties(properties);
                } catch (Exception ignored) {} // Ignore invalid properties
            }
            palette.add(block);
        }

        // Block States (Bit-Packed)
        long[] blockStates = region.getLongArray("BlockStates");
        
        // If blockStates is empty, the region might be full of the first palette block (usually air)
        if (blockStates.length == 0) {
             return new StructureManager.StructureData(new ArrayList<>(), Vec.ZERO, new Vec(width, height, length));
        }

        // Calculate bits per entry
        int paletteSize = palette.size();
        int bitsPerEntry = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1));
        
        // Unpack (Bit Stream format, not Aligned)
        int[] indices = unpack(blockStates, bitsPerEntry, width * height * length);

        List<StructureManager.StructureBlock> blocks = new ArrayList<>();

        // Iterate (Litematic order is YZX)
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    if (index >= indices.length) break;
                    
                    int paletteIndex = indices[index++];
                    if (paletteIndex >= 0 && paletteIndex < palette.size()) {
                        Block block = palette.get(paletteIndex);
                        if (!block.isAir()) {
                            blocks.add(new StructureManager.StructureBlock(new Vec(x, y, z), block));
                        }
                    }
                }
            }
        }
        
        return new StructureManager.StructureData(blocks, Vec.ZERO, new Vec(width, height, length));
    }

    private static int[] unpack(long[] data, int bitsPerEntry, int size) {
        int[] result = new int[size];
        long mask = (1L << bitsPerEntry) - 1;
        
        int bitIndex = 0;
        for (int i = 0; i < size; i++) {
            int startLongIndex = bitIndex / 64;
            int startBitOffset = bitIndex % 64;
            
            if (startLongIndex >= data.length) break;

            long value;
            if (startBitOffset + bitsPerEntry <= 64) {
                // Contained in one long
                value = (data[startLongIndex] >>> startBitOffset) & mask;
            } else {
                // Spans two longs
                // Part 1: from startBitOffset to 63
                long part1 = data[startLongIndex] >>> startBitOffset;
                // Part 2: remaining bits from next long
                int bitsRead = 64 - startBitOffset;
                int bitsLeft = bitsPerEntry - bitsRead;
                
                long part2 = 0;
                if (startLongIndex + 1 < data.length) {
                    part2 = data[startLongIndex + 1] & ((1L << bitsLeft) - 1);
                }
                
                value = (part1 | (part2 << bitsRead)) & mask;
            }
            
            result[i] = (int) value;
            bitIndex += bitsPerEntry;
        }
        return result;
    }
}
