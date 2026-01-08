package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import com.google.gson.Gson;
import me.holypite.model.map.MapConfig;

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
     * Loads the map configuration (teams, spawns) from config.json.
     */
    public MapConfig loadMapConfig(String mapName) {
        Path configPath = MAPS_FOLDER.resolve(mapName).resolve("config.json");
        if (!configPath.toFile().exists()) {
            System.err.println("Warning: config.json not found for map " + mapName);
            return null;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            return gson.fromJson(reader, MapConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an instance from a map folder.
     * The instance will NOT save changes back to disk (RAM-only session).
     *
     * @param mapName The name of the folder inside the 'maps' directory.
     * @return The created InstanceContainer, or null if loading failed.
     */
    public InstanceContainer createInstanceFromMap(String mapName) {
        Path mapPath = MAPS_FOLDER.resolve(mapName);
        if (!mapPath.toFile().exists()) {
            System.err.println("Error: Map folder not found: " + mapPath.toAbsolutePath());
            return null;
        }

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer();
        
        loadMap(instance, mapName);
        
        return instance;
    }

    public boolean loadMap(InstanceContainer instance, String mapName) {
        Path mapPath = MAPS_FOLDER.resolve(mapName);
        if (!mapPath.toFile().exists()) {
            System.err.println("Error: Map folder not found: " + mapPath.toAbsolutePath());
            return false;
        }

        instance.setChunkLoader(new AnvilLoader(mapPath));
        return true;
    }
}
