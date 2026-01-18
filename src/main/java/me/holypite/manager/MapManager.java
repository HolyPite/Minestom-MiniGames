package me.holypite.manager;

import com.google.gson.Gson;
import me.holypite.model.map.LoadedMap;
import me.holypite.model.map.MapConfig;
import me.holypite.model.map.MapEntityConfig;
import me.holypite.model.map.MapStructureConfig;
import me.holypite.manager.StructureManager.StructureRotation;
import me.holypite.manager.StructureManager.StructureMirror;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.io.Reader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class MapManager {

    private static final Path MAPS_FOLDER = Path.of("maps");
    private final Gson gson = new Gson();
    private final StructureManager structureManager = new StructureManager();

    public MapManager() {
        // Ensure maps folder exists
        if (!MAPS_FOLDER.toFile().exists()) {
            MAPS_FOLDER.toFile().mkdirs();
        }
    }

    public void saveInstance(InstanceContainer instance, String mapName) {
        Path mapPath = MAPS_FOLDER.resolve(mapName);
        try {
            // 1. Create directories
            Files.createDirectories(mapPath.resolve("region"));

            // 2. Use AnvilLoader to save chunks
            AnvilLoader loader = new AnvilLoader(mapPath);
            
            // Save Instance (level.dat)
            loader.loadInstance(instance); // Minestom's saveInstance is weirdly named in some versions or missing, 
                                           // actually AnvilLoader has saveInstance(Instance)
            loader.saveInstance(instance);

            // Save all loaded chunks
            for (net.minestom.server.instance.Chunk chunk : instance.getChunks()) {
                if (chunk.isLoaded()) {
                    loader.saveChunk(chunk);
                }
            }

            // 3. Save a basic config.json
            Path configPath = mapPath.resolve("config.json");
            if (Files.notExists(configPath)) {
                MapConfig config = new MapConfig();
                config.name = mapName;
                config.voidY = -64.0;
                // Settings & Rules objects need to be initialized if we want to save them
                config.settings = new MapConfig.MapSettings();
                config.settings.time = 6000L;
                config.settings.weather = "clear";
                
                Files.writeString(configPath, gson.toJson(config));
            }
            
            System.out.println("Map '" + mapName + "' saved successfully to " + mapPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save map " + mapName);
            e.printStackTrace();
        }
    }

    private void copyDirectory(Path source, Path target) {
        try {
            if (Files.notExists(target)) {
                Files.createDirectories(target);
            }
            try (Stream<Path> paths = Files.walk(source)) {
                paths.forEach(path -> {
                    try {
                        Path destination = target.resolve(source.relativize(path));
                        if (Files.isDirectory(path)) {
                            if (Files.notExists(destination)) {
                                Files.createDirectories(destination);
                            }
                        } else {
                            Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoadedMap createInstanceFromMap(String mapName) {
        Path mapPath = MAPS_FOLDER.resolve(mapName);
        if (!mapPath.toFile().exists()) {
            System.err.println("Error: Map folder not found: " + mapPath.toAbsolutePath());
            return null;
        }

        // Load Config
        MapConfig config = null;
        Path configPath = mapPath.resolve("config.json");
        if (configPath.toFile().exists()) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                config = gson.fromJson(reader, MapConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Warning: No config.json found for map " + mapName);
        }

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setChunkSupplier(net.minestom.server.instance.LightingChunk::new);
        
        // Region check
        // Region check
        Path regionPath = mapPath.resolve("region");
        if (regionPath.toFile().exists()) {
            instance.setChunkLoader(new AnvilLoader(mapPath));
        } else {
            // Copy "void" template to temp folder
            Path voidTemplate = MAPS_FOLDER.resolve("void");
            if (voidTemplate.toFile().exists()) {
                Path tempPath = Path.of(System.getProperty("java.io.tmpdir"), "minestom_maps", mapName + "_" + java.util.UUID.randomUUID());
                copyDirectory(voidTemplate, tempPath);
                instance.setChunkLoader(new AnvilLoader(tempPath));
            } else {
                // Fallback if void template missing
                instance.setChunkLoader(null);
                instance.setGenerator(unit -> unit.modifier().fillHeight(0, 0, Block.AIR));
            }
        }
        
        // Load Structures (from config)
        if (config != null && config.structures != null) {
            for (MapStructureConfig struct : config.structures) {
                if (struct.pos == null) continue;
                
                StructureRotation rot = StructureRotation.R0;
                if (struct.rotation != null) {
                    switch (struct.rotation) {
                        case "90": rot = StructureRotation.R90; break;
                        case "180": rot = StructureRotation.R180; break;
                        case "270": rot = StructureRotation.R270; break;
                    }
                }
                
                StructureMirror mir = StructureMirror.NONE;
                if (struct.mirror != null) {
                    switch (struct.mirror.toLowerCase()) {
                        case "x": mir = StructureMirror.X; break;
                        case "z": mir = StructureMirror.Z; break;
                        case "xz": mir = StructureMirror.XZ; break;
                    }
                }
                
                structureManager.placeStructureWithResult(instance, struct.pos.toPos(), struct.name, rot, mir);
            }
            // Persist the placed structures to the ChunkLoader (temp files)
            instance.saveChunksToStorage().join();
        }
        
        // Spawn Entities from Config
        if (config != null && config.entities != null) {
            spawnEntities(instance, config);
        }
        
        return new LoadedMap(instance, config);
    }

    public InstanceContainer createBlueprintInstance(String mapName) {
        Path mapPath = MAPS_FOLDER.resolve(mapName);
        if (!mapPath.toFile().exists()) return null;

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer();
        instance.setChunkSupplier(net.minestom.server.instance.LightingChunk::new);
        
        // Region check
        Path regionPath = mapPath.resolve("region");
        if (regionPath.toFile().exists()) {
            instance.setChunkLoader(new AnvilLoader(mapPath));
        } else {
             Path voidTemplate = MAPS_FOLDER.resolve("void");
             if (voidTemplate.toFile().exists()) {
                 Path tempPath = Path.of(System.getProperty("java.io.tmpdir"), "minestom_maps", mapName + "_bp_" + java.util.UUID.randomUUID());
                 copyDirectory(voidTemplate, tempPath);
                 instance.setChunkLoader(new AnvilLoader(tempPath));
             } else {
                 instance.setChunkLoader(null);
                 instance.setGenerator(unit -> unit.modifier().fillHeight(0, 0, Block.AIR));
             }
        }
        
        // We do NOT load entities or structures for the blueprint, strictly blocks
        // Actually, if structures modify blocks, we MIGHT want them?
        // Let's assume for now the map folder/Anvil is the source of truth for "base blocks".
        // If structures are pasted via code (config.json), we should probably paste them here too
        // if we want to repair them.
        
        // Load Config just for structures
        MapConfig config = null;
        Path configPath = mapPath.resolve("config.json");
        if (configPath.toFile().exists()) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                config = gson.fromJson(reader, MapConfig.class);
            } catch (Exception ignored) {}
        }
        
         if (config != null && config.structures != null) {
            for (MapStructureConfig struct : config.structures) {
                if (struct.pos == null) continue;
                StructureRotation rot = StructureRotation.R0;
                if (struct.rotation != null) {
                    switch (struct.rotation) {
                        case "90": rot = StructureRotation.R90; break;
                        case "180": rot = StructureRotation.R180; break;
                        case "270": rot = StructureRotation.R270; break;
                    }
                }
                StructureMirror mir = StructureMirror.NONE;
                if (struct.mirror != null) {
                    switch (struct.mirror.toLowerCase()) {
                        case "x": mir = StructureMirror.X; break;
                        case "z": mir = StructureMirror.Z; break;
                        case "xz": mir = StructureMirror.XZ; break;
                    }
                }
                structureManager.placeStructureWithResult(instance, struct.pos.toPos(), struct.name, rot, mir);
            }
            instance.saveChunksToStorage().join();
        }
        
        return instance;
    }

    private void spawnEntities(InstanceContainer instance, MapConfig config) {
        for (MapEntityConfig entityConfig : config.entities) {
            spawnEntityRecursive(instance, entityConfig, null);
        }
    }

    private void spawnEntityRecursive(InstanceContainer instance, MapEntityConfig config, Entity parent) {
        EntityType type = null;
        for (EntityType t : EntityType.values()) {
            if (t.name().equalsIgnoreCase(config.type) || t.key().asString().equalsIgnoreCase(config.type)) {
                type = t;
                break;
            }
        }
        
        if (type == null) {
            String shortName = config.type.replace("minecraft:", "");
            for (EntityType t : EntityType.values()) {
                if (t.name().equalsIgnoreCase(shortName)) {
                    type = t;
                    break;
                }
            }
        }

        if (type == null) {
            System.err.println("Unknown entity type: " + config.type);
            return;
        }

        Entity entity = new Entity(type);
        
        // Metadata handling
        if (config.meta != null) {
            if (entity.getEntityMeta() instanceof BlockDisplayMeta meta) {
                if (config.meta.containsKey("block_state")) {
                    Block block = getBlock(config.meta.get("block_state"));
                    if (block != null) meta.setBlockState(block);
                }
            } else if (entity.getEntityMeta() instanceof ItemDisplayMeta meta) {
                if (config.meta.containsKey("item")) {
                    String itemName = config.meta.get("item");
                    // Simple parsing for now (no count/nbt support in string yet)
                    
                    String matName = itemName.replace("minecraft:", "").toUpperCase();
                    Material material = null;
                    for (Material m : Material.values()) {
                        if (m.name().equalsIgnoreCase(matName)) {
                            material = m;
                            break;
                        }
                    }
                    
                    if (material != null) {
                        meta.setItemStack(ItemStack.of(material));
                    }
                }
            } else if (entity.getEntityMeta() instanceof TextDisplayMeta meta) {
                if (config.meta.containsKey("text")) {
                    meta.setText(Component.text(config.meta.get("text")));
                }
            }
            
            // Common Display Meta (Transformation)
            if (entity.getEntityMeta() instanceof AbstractDisplayMeta meta) {
                if (config.meta.containsKey("scale")) {
                    String[] scale = config.meta.get("scale").split(",");
                    if (scale.length == 3) {
                        try {
                            meta.setScale(new Vec(Double.parseDouble(scale[0]), Double.parseDouble(scale[1]), Double.parseDouble(scale[2])));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (config.meta.containsKey("transformation")) {
                    String[] t = config.meta.get("transformation").split(",");
                    if (t.length == 16) {
                        try {
                            // Extract Translation (Column 3)
                            double tx = Double.parseDouble(t[3]);
                            double ty = Double.parseDouble(t[7]);
                            double tz = Double.parseDouble(t[11]);
                            meta.setTranslation(new Vec(tx, ty, tz));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        // Spawn logic
        if (parent == null) {
            if (config.pos != null) {
                entity.setInstance(instance, config.pos.toPos()).join();
            } else {
                // Default pos
                entity.setInstance(instance, new net.minestom.server.coordinate.Pos(0, 100, 0)).join();
            }
        } else {
            // Passenger
            // Spawn at parent pos first
            entity.setInstance(instance, parent.getPosition()).join();
            parent.addPassenger(entity);
        }
        
        // Recursion
        if (config.passengers != null) {
            for (MapEntityConfig passengerConfig : config.passengers) {
                spawnEntityRecursive(instance, passengerConfig, entity);
            }
        }
    }

    private Block getBlock(String id) {
        for (Block b : Block.values()) {
            if (b.name().equalsIgnoreCase(id) || b.key().asString().equalsIgnoreCase(id)) {
                return b;
            }
        }
        return null;
    }
}