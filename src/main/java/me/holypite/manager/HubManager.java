package me.holypite.manager;

import me.holypite.inventory.GameSelectorInventory;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
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
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class HubManager {

    private final List<InstanceContainer> hubs = new ArrayList<>();
    private final MapManager mapManager;
    private GameSelectorInventory selectorInventory;
    private static final int MAX_PLAYERS_PER_HUB = 50;

    private static final ItemStack COMPASS_ITEM = new ItemBuilder(Material.COMPASS)
            .name(Component.text("Sélectionneur de Jeux", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            .build();

    public HubManager(MapManager mapManager) {
        this.mapManager = mapManager;
        // Create initial hub
        createNewHub();
    }

    public void init(GameManager gameManager) {
        this.selectorInventory = new GameSelectorInventory(gameManager);

        // Listen for right click
        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            if (!isHub(event.getInstance())) return;
            if (event.getItemStack().equals(COMPASS_ITEM)) {
                selectorInventory.open(event.getPlayer());
            }
        });

        // Ensure player state is reset on spawn in hub
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            if (isHub(event.getInstance())) {
                resetPlayer(event.getPlayer());
            }
        });

        // Prevent dropping items in Hub
        MinecraftServer.getGlobalEventHandler().addListener(ItemDropEvent.class, event -> {
            if (isHub(event.getInstance()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });

        // Prevent moving items in Hub inventory
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getInventory() == null && isHub(event.getPlayer().getInstance()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
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
    }

    private void resetPlayer(Player player) {
        // Reset Player State
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.heal();
        player.setFood(20);
        player.setInvisible(false);
        player.setAllowFlying(false);
        player.setFlying(false);
        player.clearEffects();

        // Give Compass
        player.getInventory().setItemStack(4, COMPASS_ITEM);
    }
}
