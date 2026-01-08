package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HubManager {

    private final List<InstanceContainer> hubs = new ArrayList<>();
    private static final int MAX_PLAYERS_PER_HUB = 50;

    public HubManager() {
        // Create initial hub
        createNewHub();
    }

    public void createNewHub() {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer hub = instanceManager.createInstanceContainer();
        
        // Simple flat generation for Hub
        hub.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        
        hubs.add(hub);
        System.out.println("New Hub created. Total Hubs: " + hubs.size());
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
        player.setInstance(targetHub, new Pos(0, 42, 0));
    }
}
