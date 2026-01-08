package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;

import java.nio.file.Path;

public class MapManager {

    private static final Path MAPS_FOLDER = Path.of("maps");

    public MapManager() {
        // Ensure maps folder exists
        if (!MAPS_FOLDER.toFile().exists()) {
            MAPS_FOLDER.toFile().mkdirs();
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
        
        // Use AnvilLoader to load the world
        instance.setChunkLoader(new AnvilLoader(mapPath));
        
        // Disable auto-save to treat this map as a template
        // Note: Minestom doesn't auto-save by default unless configured, 
        // but passing the loader ensures chunks are loaded from disk.
        // We just need to make sure we don't call instance.saveChunksToStorage() manually.
        
        return instance;
    }
}
