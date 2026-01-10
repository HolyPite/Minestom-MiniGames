package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import me.holypite.model.map.LoadedMap;
import net.minestom.server.entity.GameMode;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minestom.server.instance.Instance;

public class HubManager {

    private final List<InstanceContainer> hubs = new ArrayList<>();
    private final MapManager mapManager;
    private final EntityLoader entityLoader = new EntityLoader();
    private static final int MAX_PLAYERS_PER_HUB = 50;

    public HubManager(MapManager mapManager) {
        this.mapManager = mapManager;
        // Create initial hub
        createNewHub();
    }

    public boolean isHub(Instance instance) {
        return hubs.contains(instance);
    }

    public void createNewHub() {
        // Try to load Hub map
        LoadedMap loadedMap = mapManager.createInstanceFromMap("hub");
        
        InstanceContainer hub;
        if (loadedMap != null) {
            hub = loadedMap.getInstance();
            System.out.println("Loaded Hub map successfully.");
            
            // Load Entities (Displays, etc.)
            entityLoader.loadEntities(hub, Path.of("maps/hub"));
            
        } else {
            // Fallback
            InstanceManager instanceManager = MinecraftServer.getInstanceManager();
            hub = instanceManager.createInstanceContainer();
            hub.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
            System.err.println("Failed to load Hub map, using fallback generator.");
        }
        
        hubs.add(hub);
        System.out.println("Total Hubs: " + hubs.size());
    }

    public InstanceContainer getBestHub() {
        // Find hub with most space but not full
        return hubs.stream()
                .filter(h -> h.getPlayers().size() < MAX_PLAYERS_PER_HUB)
                .min(Comparator.comparingInt(h -> h.getPlayers().size())) // Fill empty ones first or balanced? Let's balance.
                .orElseGet(() -> {
                    createNewHub();
                    return hubs.get(hubs.size() - 1);
                });
    }

    public void joinHub(Player player) {
        InstanceContainer targetHub = getBestHub();
        player.setInstance(targetHub, new Pos(0.5, 64, 0.5));
        
        // Reset Player State
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.heal();
        player.setFood(20);
        player.setInvisible(false);
        player.setAllowFlying(false);
        player.setFlying(false);
        player.clearEffects();
    }
}
