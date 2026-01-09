package me.holypite.manager;

import com.google.gson.Gson;
import me.holypite.model.map.LoadedMap;
import me.holypite.model.map.MapConfig;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;

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
        
        return new LoadedMap(instance, config);
    }
}