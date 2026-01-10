package me.holypite.manager;

import com.google.gson.Gson;
import me.holypite.model.map.LoadedMap;
import me.holypite.model.map.MapConfig;
import me.holypite.model.map.MapEntityConfig;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapManager {

    private static final Path MAPS_FOLDER = Path.of("maps");
    private final Gson gson = new Gson();

    public MapManager() {
        // Ensure maps folder exists
        if (!MAPS_FOLDER.toFile().exists()) {
            MAPS_FOLDER.toFile().mkdirs();
        }
    }

    /**
     * Creates an instance from a map folder and loads its config.
     * The instance will NOT save changes back to disk (RAM-only session).
     *
     * @param mapName The name of the folder inside the 'maps' directory.
     * @return The LoadedMap containing instance and config, or null if loading failed.
     */
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
        
        // Use AnvilLoader to load the world
        instance.setChunkLoader(new AnvilLoader(mapPath));
        
        // Spawn Entities from Config
        if (config != null && config.entities != null) {
            spawnEntities(instance, config);
        }
        
        return new LoadedMap(instance, config);
    }

    private void spawnEntities(InstanceContainer instance, MapConfig config) {
        for (MapEntityConfig entityConfig : config.entities) {
            EntityType type = null;
            // Try to find type
            for (EntityType t : EntityType.values()) {
                if (t.name().equalsIgnoreCase(entityConfig.type) || t.key().asString().equalsIgnoreCase(entityConfig.type)) {
                    type = t;
                    break;
                }
            }
            
            if (type == null) {
                // Try removing minecraft: prefix
                String shortName = entityConfig.type.replace("minecraft:", "");
                for (EntityType t : EntityType.values()) {
                    if (t.name().equalsIgnoreCase(shortName)) {
                        type = t;
                        break;
                    }
                }
            }

            if (type == null) {
                System.err.println("Unknown entity type: " + entityConfig.type);
                continue;
            }

            Entity entity = new Entity(type);
            entity.setInstance(instance, entityConfig.pos.toPos());

            // Metadata handling
            if (entityConfig.meta != null) {
                if (entity.getEntityMeta() instanceof BlockDisplayMeta meta) {
                    if (entityConfig.meta.containsKey("block_state")) {
                        Block block = getBlock(entityConfig.meta.get("block_state"));
                        if (block != null) meta.setBlockState(block);
                    }
                    if (entityConfig.meta.containsKey("scale")) {
                        // Parse scale "x,y,z"
                        String[] scale = entityConfig.meta.get("scale").split(",");
                        if (scale.length == 3) {
                            try {
                                meta.setScale(new Vec(Double.parseDouble(scale[0]), Double.parseDouble(scale[1]), Double.parseDouble(scale[2])));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                } else if (entity.getEntityMeta() instanceof TextDisplayMeta meta) {
                    if (entityConfig.meta.containsKey("text")) {
                        meta.setText(Component.text(entityConfig.meta.get("text")));
                    }
                }
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
