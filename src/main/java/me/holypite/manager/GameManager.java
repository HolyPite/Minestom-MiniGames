package me.holypite.manager;

import me.holypite.games.duel.DuelGame;
import me.holypite.games.sheepwars.SheepWarsGame;
import me.holypite.model.Game;
import me.holypite.model.GameState;
import me.holypite.model.GameType;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameManager {

    private final List<Game> activeGames = new ArrayList<>();
    private final HubManager hubManager;
    private final MapManager mapManager;

    public GameManager(HubManager hubManager, MapManager mapManager) {
        this.hubManager = hubManager;
        this.mapManager = mapManager;
    }

    public void joinGame(Player player, GameType type) {
        // Try to find an existing Lobby for this game type
        Optional<Game> availableGame = activeGames.stream()
                .filter(g -> getGameType(g) == type)
                .filter(g -> g.getState() == GameState.LOBBY)
                .findFirst();

        Game game;
        if (availableGame.isPresent()) {
            game = availableGame.get();
        } else {
            game = createGame(type);
            
            // Setup cleanup
            Game finalGame = game;
            game.setOnEndCallback(() -> {
                // Send all players to hub
                new ArrayList<>(finalGame.getPlayers()).forEach(p -> hubManager.joinHub(p));
                // Remove game from manager
                removeGame(finalGame);
            });
            
            activeGames.add(game);
        }

        game.addPlayer(player);
    }

    private Game createGame(GameType type) {
        switch (type) {
            case DUEL:
                return new DuelGame(mapManager);
            case SHEEP_WARS:
                return new SheepWarsGame(mapManager);
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
    }
    
    // Helper to identify game type (naive implementation, usually Game would have a getType() method)
    private GameType getGameType(Game game) {
        if (game instanceof DuelGame) return GameType.DUEL;
        if (game instanceof SheepWarsGame) return GameType.SHEEP_WARS;
        return null;
    }
    
    public void removeGame(Game game) {
        activeGames.remove(game);
        // Clean up instance if necessary
        net.minestom.server.MinecraftServer.getInstanceManager().unregisterInstance(game.getInstance());
    }
    
    // Method called when a game ends to send players back
    public void sendToHub(Player player) {
        hubManager.joinHub(player);
    }

    public Game getGameOfPlayer(Player player) {
        return activeGames.stream()
                .filter(g -> g.getPlayers().contains(player))
                .findFirst()
                .orElse(null);
    }
}
