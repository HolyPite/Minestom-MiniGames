package me.holypite;

import me.holypite.commands.PlayCommand;
import me.holypite.manager.GameManager;
import me.holypite.manager.HubManager;
import me.holypite.manager.MapManager;
import me.holypite.model.Game;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.coordinate.Pos;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.entity.GameMode;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());

        // Managers
        MapManager mapManager = new MapManager();
        HubManager hubManager = new HubManager(mapManager);
        GameManager gameManager = new GameManager(hubManager, mapManager);

        // Commands
        MinecraftServer.getCommandManager().register(new PlayCommand(gameManager));

        // Events
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // Hub Protection
        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (hubManager.isHub(event.getInstance()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
        
        globalEventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (hubManager.isHub(event.getInstance()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });

        // Login -> Send to Hub
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            net.minestom.server.instance.InstanceContainer hub = hubManager.getBestHub();
            if (hub == null) {
                System.err.println("CRITICAL: No hub found for player " + event.getPlayer().getUsername());
            } else {
                System.out.println("Assigning hub to player " + event.getPlayer().getUsername());
                event.setSpawningInstance(hub);
                event.getPlayer().setRespawnPoint(new Pos(0.5, 64, 0.5));
            }
        });

        // Disconnect -> Clean up from games
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            Game game = gameManager.getGameOfPlayer(player);
            if (game != null) {
                game.removePlayer(player);
            }
        });

        System.out.println("Server starting...");
        minecraftServer.start("0.0.0.0", 25565);
    }
}