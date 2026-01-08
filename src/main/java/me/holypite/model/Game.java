package me.holypite.model;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;
import me.holypite.manager.PvpManager;
import me.holypite.manager.projectile.ProjectileManager;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public abstract class Game {

    private final UUID gameId;
    private InstanceContainer lobbyInstance;
    private InstanceContainer gameInstance;
    private final Set<Player> players;
    private GameState state;
    private final int minPlayers;
    private final int maxPlayers;
    
    // Config
    private final String gameName;
    private Runnable onEndCallback;
    
    // Managers
    private final PvpManager pvpManager = new PvpManager();
    private boolean pvpEnabled = false;
    private EventNode<Event> gameEventNode;
    
    // Kits
    private final List<Kit> registeredKits = new ArrayList<>();
    private Kit defaultKit;
    private final Map<Player, Kit> playerKits = new ConcurrentHashMap<>();

    public Game(String gameName, int minPlayers, int maxPlayers) {
        this.gameId = UUID.randomUUID();
        this.gameName = gameName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.players = new HashSet<>();
        this.state = GameState.LOBBY;
        
        // Create the lobby instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        this.lobbyInstance = instanceManager.createInstanceContainer();
        
        setupLobbyInstance(this.lobbyInstance);
    }
    
    public void setOnEndCallback(Runnable onEndCallback) {
        this.onEndCallback = onEndCallback;
    }
    
    protected void setPvpEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
    }
    
    protected void registerKit(Kit kit) {
        registeredKits.add(kit);
        if (defaultKit == null) {
            defaultKit = kit;
        }
    }
    
    public void selectKit(Player player, Kit kit) {
        if (registeredKits.contains(kit)) {
            playerKits.put(player, kit);
            player.sendMessage("Selected kit: " + kit.getName());
        }
    }
    
    public List<Kit> getRegisteredKits() {
        return Collections.unmodifiableList(registeredKits);
    }

    // Abstract methods to be implemented by specific games
    public abstract void setupLobbyInstance(InstanceContainer instance);
    public abstract void setupGameInstance(InstanceContainer instance);
    public abstract void onPlayerJoin(Player player);
    public abstract void onPlayerQuit(Player player);
    public abstract void onGameStart();
    public abstract void onGameEnd();

    public void addPlayer(Player player) {
        if (state != GameState.LOBBY && state != GameState.STARTING) return;
        if (players.size() >= maxPlayers) return;

        players.add(player);
        player.setInstance(lobbyInstance).thenAccept(ignored -> {
            player.teleport(new net.minestom.server.coordinate.Pos(0, 42, 0));
            onPlayerJoin(player);
            checkStart();
        });
    }

    public void removePlayer(Player player) {
        players.remove(player);
        playerKits.remove(player);
        player.getInventory().clear(); // Clear inventory on quit
        onPlayerQuit(player);
        
        if (players.isEmpty() && state != GameState.LOBBY) {
            endGame(); // Security: Close game if empty
        }
    }

    private void checkStart() {
        if (state == GameState.LOBBY && players.size() >= minPlayers) {
            startCountdown();
        }
    }

    private void startCountdown() {
        this.state = GameState.STARTING;
        sendMessageToAll("Game starting in 5 seconds...");
        
        MinecraftServer.getSchedulerManager().buildTask(this::startGame)
                .delay(TaskSchedule.seconds(5))
                .schedule();
    }

    public void startGame() {
        if (players.size() < minPlayers) {
            this.state = GameState.LOBBY;
            sendMessageToAll("Not enough players to start. Countdown cancelled.");
            return;
        }
        
        // Initialize Game Instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        this.gameInstance = instanceManager.createInstanceContainer();
        setupGameInstance(this.gameInstance);
        
        // Setup Scoped Event Node
        this.gameEventNode = EventNode.event("game-" + gameId, EventFilter.ALL, event -> {
            if (event instanceof InstanceEvent ie) return ie.getInstance() == gameInstance;
            if (event instanceof EntityEvent ee) return ee.getEntity().getInstance() == gameInstance;
            return false;
        });
        
        // Setup PvP if enabled
        if (pvpEnabled) {
            this.gameEventNode.addChild(pvpManager.getEventNode());
            new ProjectileManager(this.gameEventNode);
        }
        
        // Register the game node globally
        MinecraftServer.getGlobalEventHandler().addChild(this.gameEventNode);
        
        this.state = GameState.IN_GAME;
        
        // Teleport everyone to the game instance
        List<java.util.concurrent.CompletableFuture<Void>> teleportFutures = new ArrayList<>();
        
        for (Player p : players) {
            teleportFutures.add(p.setInstance(gameInstance).thenAccept(ignored -> {
               p.teleport(new net.minestom.server.coordinate.Pos(0, 42, 0)); 
            }));
        }
        
        // Wait for all players to move before destroying the lobby
        java.util.concurrent.CompletableFuture.allOf(teleportFutures.toArray(new java.util.concurrent.CompletableFuture[0]))
            .thenRun(() -> {
                // Unregister Lobby Instance (cleanup)
                if (instanceManager != null) { // Safety check though var is local
                   instanceManager.unregisterInstance(lobbyInstance);
                } else {
                    MinecraftServer.getInstanceManager().unregisterInstance(lobbyInstance);
                }
                this.lobbyInstance = null;
                
                // Give Kits
                for (Player p : players) {
                    p.heal();
                    p.setFood(20);
                    Kit kit = playerKits.getOrDefault(p, defaultKit);
                    if (kit != null) {
                        kit.apply(p);
                    }
                }
                
                sendMessageToAll("Game Started!");
                onGameStart();
            });
    }

    public void endGame() {
        this.state = GameState.ENDING;
        onGameEnd();
        
        // Unregister game events
        if (this.gameEventNode != null) {
            MinecraftServer.getGlobalEventHandler().removeChild(this.gameEventNode);
            this.gameEventNode = null;
        }

        sendMessageToAll("Game Ended. Sending you back to Hub in 5 seconds...");
        
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (onEndCallback != null) {
                onEndCallback.run();
            }
            // Clear players list as they should be moved by the callback
            players.clear();
        })
        .delay(TaskSchedule.seconds(5))
        .schedule();
    }

    public void sendMessageToAll(String message) {
        for (Player p : players) {
            p.sendMessage(message);
        }
    }

    public InstanceContainer getInstance() {
        return (state == GameState.LOBBY || state == GameState.STARTING) ? lobbyInstance : gameInstance;
    }
    
    public Set<Player> getPlayers() {
        return players;
    }
    
    public GameState getState() {
        return state;
    }
    
    protected EventNode<Event> getGameEventNode() {
        return gameEventNode;
    }
}
