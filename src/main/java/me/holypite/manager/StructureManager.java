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
    
    public enum StructureRotation {
        R0, R90, R180, R270
    }
    
    public enum StructureMirror {
        NONE, X, Z, XZ
    }

    public record StructureBlock(Point relativePos, Block block) {}

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
                    ListBinaryTag pos = ListBinaryTag.builder(BinaryTagTypes.INT)
                            .add(IntBinaryTag.intBinaryTag(x))
                            .add(IntBinaryTag.intBinaryTag(y))
                            .add(IntBinaryTag.intBinaryTag(z))
                            .build();
                    
                    blockTag.put("pos", pos);
                    blockTag.putInt("state", stateId);
                    
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
                .put("blocks", ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(blocks).build())
                .put("palette", ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(palette).build())
                .putInt("DataVersion", 3465) 
                .build();

        try {
            // BinaryTagIO.writer() defaults to GZIP if I remember correctly or handles it via extension
            // For Vanilla compatibility, GZIP is preferred.
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
        List<StructureBlock> structureBlocks = getStructureBlocks(name, rotation, mirror);
        if (structureBlocks == null) return false;

        for (StructureBlock sb : structureBlocks) {
            instance.setBlock(origin.add(sb.relativePos()), sb.block());
        }
        
        System.out.println("Structure placed: " + name + " (Rot: " + rotation + ", Mir: " + mirror + ")");
        return true;
    }

    public List<StructureBlock> getStructureBlocks(String name, StructureRotation rotation, StructureMirror mirror) {
        Path path = STRUCTURES_DIR.resolve(name + (name.endsWith(".nbt") ? "" : ".nbt"));
        if (!path.toFile().exists()) {
            System.err.println("Structure not found: " + path.toAbsolutePath());
            return null;
        }

        try {
            // BinaryTagIO.reader() automatically detects GZIP (Vanilla format)
            CompoundBinaryTag root = BinaryTagIO.reader().read(path);
            List<StructureBlock> structureBlocks = new ArrayList<>();
            
            // Vanilla structures often have data under a root tag (e.g. empty string or 'data')
            // If "blocks" is not at root, try to find it
            CompoundBinaryTag data = root;
            if (!root.keySet().contains("blocks") && root.keySet().size() == 1) {
                String key = root.keySet().iterator().next();
                BinaryTag sub = root.get(key);
                if (sub instanceof CompoundBinaryTag) {
                    data = (CompoundBinaryTag) sub;
                }
            }

            if (!data.keySet().contains("blocks") || !data.keySet().contains("palette")) {
                System.err.println("Invalid structure format: 'blocks' or 'palette' missing in " + name);
                return null;
            }

            // Parse Palette
            List<Block> palette = new ArrayList<>();
            for (BinaryTag tag : data.getList("palette")) {
                if (tag instanceof CompoundBinaryTag ct) {
                    String blockName = ct.getString("Name");
                    Map<String, String> properties = new HashMap<>();
                    if (ct.keySet().contains("Properties")) {
                        CompoundBinaryTag props = ct.getCompound("Properties");
                        for (String key : props.keySet()) {
                            properties.put(key, props.getString(key));
                        }
                    }
                    
                    Block block = null;
                    for (Block b : Block.values()) {
                        if (b.name().equalsIgnoreCase(blockName) || b.key().asString().equalsIgnoreCase(blockName)) {
                            block = b;
                            break;
                        }
                    }
                    
                    if (block != null && !properties.isEmpty()) {
                        block = block.withProperties(properties);
                    }
                    if (block == null) block = Block.AIR;
                    
                    // Apply Transform to Palette Block (State)
                    block = transformBlockState(block, rotation, mirror);
                    
                    palette.add(block);
                }
            }

            // Extract Blocks
            ListBinaryTag blocks = data.getList("blocks");
            for (BinaryTag tag : blocks) {
                if (tag instanceof CompoundBinaryTag ct) {
                    ListBinaryTag pos = ct.getList("pos");
                    int x = pos.getInt(0);
                    int y = pos.getInt(1);
                    int z = pos.getInt(2);
                    
                    int state = ct.getInt("state");
                    Block block = palette.get(state);
                    
                    if (ct.keySet().contains("nbt")) {
                         block = block.withNbt(ct.getCompound("nbt"));
                    }
                    
                    // Transform Position
                    Point finalPos = transformPosition(x, y, z, rotation, mirror);
                    structureBlocks.add(new StructureBlock(finalPos, block));
                }
            }
            return structureBlocks;

        } catch (IOException e) {
            System.err.println("Error reading structure " + name + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private Point transformPosition(int x, int y, int z, StructureRotation rotation, StructureMirror mirror) {
        // Mirror first
        if (mirror == StructureMirror.X || mirror == StructureMirror.XZ) {
            x = -x;
        }
        if (mirror == StructureMirror.Z || mirror == StructureMirror.XZ) {
            z = -z;
        }
        
        // Rotate
        int newX = x;
        int newZ = z;
        
        switch (rotation) {
            case R90:
                newX = -z;
                newZ = x;
                break;
            case R180:
                newX = -x;
                newZ = -z;
                break;
            case R270:
                newX = z;
                newZ = -x;
                break;
            default:
                break;
        }
        
        return new Vec(newX, y, newZ);
    }
    
    private Block transformBlockState(Block block, StructureRotation rotation, StructureMirror mirror) {
        // Apply Mirror First
        if (mirror == StructureMirror.X || mirror == StructureMirror.XZ) {
            block = mirrorBlock(block, true, false);
        }
        if (mirror == StructureMirror.Z || mirror == StructureMirror.XZ) {
            block = mirrorBlock(block, false, true);
        }
        
        // Apply Rotation
        switch (rotation) {
            case R90:
                block = rotateBlock(block);
                break;
            case R180:
                block = rotateBlock(block);
                block = rotateBlock(block);
                break;
            case R270:
                block = rotateBlock(block);
                block = rotateBlock(block);
                block = rotateBlock(block);
                break;
            default:
                break;
        }
        return block;
    }
    
    // Simple helper to rotate 90 degrees clockwise
    private Block rotateBlock(Block block) {
        Map<String, String> props = block.properties();
        if (props.containsKey("facing")) {
            String facing = props.get("facing");
            String newFacing = switch (facing) {
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> facing;
            };
            return block.withProperty("facing", newFacing);
        }
        if (props.containsKey("axis")) {
            String axis = props.get("axis");
            if (axis.equals("x")) return block.withProperty("axis", "z");
            if (axis.equals("z")) return block.withProperty("axis", "x");
        }
        // TODO: Rotation 0-15 (Signs), etc.
        return block;
    }
    
    private Block mirrorBlock(Block block, boolean mirrorX, boolean mirrorZ) {
        // Mirror X: Flips X coord. West becomes East.
        // Mirror Z: Flips Z coord. North becomes South.
        Map<String, String> props = block.properties();
        
        if (props.containsKey("facing")) {
            String facing = props.get("facing");
            String newFacing = facing;
            
            if (mirrorX) {
                if (facing.equals("west")) newFacing = "east";
                if (facing.equals("east")) newFacing = "west";
            }
            if (mirrorZ) {
                if (facing.equals("north")) newFacing = "south";
                if (facing.equals("south")) newFacing = "north";
            }
            return block.withProperty("facing", newFacing);
        }
        // Axis invariant under reflection unless rotated? Axis X mirrored X is still X aligned.
        return block;
    }
}