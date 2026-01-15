package me.holypite.manager;

import net.kyori.adventure.nbt.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class StructureManager {

    private static final Path STRUCTURES_DIR = Path.of("structures");
    
    public enum StructureRotation {
        R0, R90, R180, R270
    }
    
    public enum StructureMirror {
        NONE, X, Z, XZ
    }

    public record StructureBlock(Point relativePos, Block block) {}
    public record StructureData(List<StructureBlock> blocks, Point minPoint, Point maxPoint) {}

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
                    if (block.isAir()) continue; 
                    
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
                    blockTag.put("pos", ListBinaryTag.builder(BinaryTagTypes.INT)
                            .add(IntBinaryTag.intBinaryTag(x))
                            .add(IntBinaryTag.intBinaryTag(y))
                            .add(IntBinaryTag.intBinaryTag(z))
                            .build());
                    blockTag.putInt("state", stateId);
                    if (block.hasNbt()) blockTag.put("nbt", block.nbt());
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
                .put("blocks", ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(blocks).build())
                .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(palette).build())
                .putInt("DataVersion", 3465) 
                .build();

        try {
            BinaryTagIO.writer().write(root, STRUCTURES_DIR.resolve(name + ".nbt"));
            System.out.println("Structure saved: " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void placeStructure(Instance instance, Point origin, String name) {
        placeStructureWithResult(instance, origin, name, StructureRotation.R0, StructureMirror.NONE);
    }

    public boolean placeStructureWithResult(Instance instance, Point origin, String name, StructureRotation rotation, StructureMirror mirror) {
        StructureData data = getStructureBlocks(name, rotation, mirror, true);
        if (data == null) return false;

        for (StructureBlock sb : data.blocks()) {
            instance.setBlock(origin.add(sb.relativePos()), sb.block());
        }
        System.out.println("Structure placed: " + name);
        return true;
    }

    public StructureData getStructureBlocks(String name, StructureRotation rotation, StructureMirror mirror) {
        return getStructureBlocks(name, rotation, mirror, true);
    }

    public StructureData getStructureBlocks(String name, StructureRotation rotation, StructureMirror mirror, boolean includeAir) {
        Path path = STRUCTURES_DIR.resolve(name + (name.endsWith(".nbt") ? "" : ".nbt"));
        if (!path.toFile().exists()) return null;

        try {
            // Manual check for GZIP
            CompoundBinaryTag root;
            try (InputStream is = new BufferedInputStream(new FileInputStream(path.toFile()))) {
                is.mark(2);
                int b1 = is.read();
                int b2 = is.read();
                is.reset();
                
                if (b1 == 0x1f && b2 == 0x8b) {
                    root = BinaryTagIO.reader(Long.MAX_VALUE).read(new GZIPInputStream(is));
                } else {
                    root = BinaryTagIO.reader(Long.MAX_VALUE).read(is);
                }
            }

            CompoundBinaryTag data = findDataTag(root);
            if (data == null) return null;

            List<StructureBlock> structureBlocks = new ArrayList<>();
            List<Block> palette = new ArrayList<>();
            
            for (BinaryTag tag : data.getList("palette")) {
                CompoundBinaryTag ct = (CompoundBinaryTag) tag;
                String blockName = ct.getString("Name");
                Map<String, String> properties = new HashMap<>();
                if (ct.keySet().contains("Properties")) {
                    CompoundBinaryTag props = ct.getCompound("Properties");
                    for (String key : props.keySet()) properties.put(key, props.getString(key));
                }
                
                Block block = null;
                for (Block b : Block.values()) {
                    if (b.name().equalsIgnoreCase(blockName) || b.key().asString().equalsIgnoreCase(blockName)) {
                        block = b;
                        break;
                    }
                }
                if (block == null) block = Block.AIR;
                else if (!properties.isEmpty()) block = block.withProperties(properties);
                
                palette.add(transformBlockState(block, rotation, mirror));
            }

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

            for (BinaryTag tag : data.getList("blocks")) {
                CompoundBinaryTag ct = (CompoundBinaryTag) tag;
                ListBinaryTag pos = ct.getList("pos");
                Block block = palette.get(ct.getInt("state"));
                
                if (!includeAir && block.isAir()) continue;

                if (ct.keySet().contains("nbt")) block = block.withNbt(ct.getCompound("nbt"));
                
                Point finalPos = transformPosition(pos.getInt(0), pos.getInt(1), pos.getInt(2), rotation, mirror);
                structureBlocks.add(new StructureBlock(finalPos, block));

                if (finalPos.blockX() < minX) minX = finalPos.blockX();
                if (finalPos.blockY() < minY) minY = finalPos.blockY();
                if (finalPos.blockZ() < minZ) minZ = finalPos.blockZ();
                if (finalPos.blockX() > maxX) maxX = finalPos.blockX();
                if (finalPos.blockY() > maxY) maxY = finalPos.blockY();
                if (finalPos.blockZ() > maxZ) maxZ = finalPos.blockZ();
            }
            
            // Handle empty structure case (e.g. only air blocks filtered out)
            if (structureBlocks.isEmpty()) {
                 return new StructureData(structureBlocks, Vec.ZERO, Vec.ZERO);
            }
            
            return new StructureData(structureBlocks, new Vec(minX, minY, minZ), new Vec(maxX, maxY, maxZ));

        } catch (Throwable t) {
            System.err.println("Error loading structure " + name + ": " + t.getMessage());
            return null;
        }
    }

    private CompoundBinaryTag findDataTag(CompoundBinaryTag root) {
        if (root.keySet().contains("blocks") && root.keySet().contains("palette")) return root;
        for (String key : root.keySet()) {
            BinaryTag sub = root.get(key);
            if (sub instanceof CompoundBinaryTag ct) {
                if (ct.keySet().contains("blocks") && ct.keySet().contains("palette")) return ct;
                for (String skey : ct.keySet()) {
                    BinaryTag ssub = ct.get(skey);
                    if (ssub instanceof CompoundBinaryTag cct && cct.keySet().contains("blocks") && cct.keySet().contains("palette")) return cct;
                }
            }
        }
        return null;
    }

    private Point transformPosition(int x, int y, int z, StructureRotation rotation, StructureMirror mirror) {
        if (mirror == StructureMirror.X || mirror == StructureMirror.XZ) x = -x;
        if (mirror == StructureMirror.Z || mirror == StructureMirror.XZ) z = -z;
        int nx = x, nz = z;
        switch (rotation) {
            case R90 -> { nx = -z; nz = x; }
            case R180 -> { nx = -x; nz = -z; }
            case R270 -> { nx = z; nz = -x; }
        }
        return new Vec(nx, y, nz);
    }

    private Block transformBlockState(Block block, StructureRotation rotation, StructureMirror mirror) {
        if (mirror == StructureMirror.X || mirror == StructureMirror.XZ) block = mirrorBlock(block, true, false);
        if (mirror == StructureMirror.Z || mirror == StructureMirror.XZ) block = mirrorBlock(block, false, true);
        switch (rotation) {
            case R90 -> block = rotateBlock(block);
            case R180 -> { block = rotateBlock(block); block = rotateBlock(block); }
            case R270 -> { block = rotateBlock(block); block = rotateBlock(block); block = rotateBlock(block); }
        }
        return block;
    }

    private Block rotateBlock(Block block) {
        Map<String, String> props = block.properties();
        if (props.containsKey("facing")) {
            String f = props.get("facing");
            String nf = switch (f) {
                case "north" -> "east"; case "east" -> "south"; case "south" -> "west"; case "west" -> "north";
                default -> f;
            };
            return block.withProperty("facing", nf);
        }
        if (props.containsKey("axis")) {
            String a = props.get("axis");
            if (a.equals("x")) return block.withProperty("axis", "z");
            if (a.equals("z")) return block.withProperty("axis", "x");
        }
        return block;
    }

    private Block mirrorBlock(Block block, boolean mx, boolean mz) {
        Map<String, String> props = block.properties();
        if (props.containsKey("facing")) {
            String f = props.get("facing");
            String nf = f;
            if (mx) { if (f.equals("west")) nf = "east"; else if (f.equals("east")) nf = "west"; }
            if (mz) { if (f.equals("north")) nf = "south"; else if (f.equals("south")) nf = "north"; }
            return block.withProperty("facing", nf);
        }
        return block;
    }
}