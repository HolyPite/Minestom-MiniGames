package me.holypite.model;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;
import me.holypite.manager.PvpManager;
import me.holypite.manager.DeathManager;
import me.holypite.manager.explosion.ExplosionManager;
import me.holypite.manager.projectile.ProjectileManager;
import me.holypite.model.map.MapConfig;
import me.holypite.model.map.TeamConfig;
import me.holypite.model.map.MapSpawn;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.coordinate.Pos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

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
    protected MapConfig mapConfig;
    protected final Map<Player, TeamConfig> playerTeams = new ConcurrentHashMap<>();
    
    // Managers
    private final PvpManager pvpManager = new PvpManager();
    protected final me.holypite.manager.MapManager mapManager; 
    protected final ExplosionManager explosionManager = new ExplosionManager();
    private boolean pvpEnabled = false;
    private EventNode<Event> gameEventNode;
    private ProjectileManager projectileManager;
    private DeathManager deathManager;
    
    // Game Rules
    private boolean canRespawn = false;
    private int respawnDelay = 3;
    private boolean canBreakBlocks = false;
    
    // Kits
    private final List<Kit> registeredKits = new ArrayList<>();
    private Kit defaultKit;
    private final Map<Player, Kit> playerKits = new ConcurrentHashMap<>();

    public Game(String gameName, int minPlayers, int maxPlayers, me.holypite.manager.MapManager mapManager) {
        this.gameId = UUID.randomUUID();
        this.gameName = gameName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.mapManager = mapManager;
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

    protected void setCanRespawn(boolean canRespawn) {
        this.canRespawn = canRespawn;
    }

    protected void setRespawnDelay(int respawnDelay) {
        this.respawnDelay = respawnDelay;
    }
    
    protected void setCanBreakBlocks(boolean canBreakBlocks) {
        this.canBreakBlocks = canBreakBlocks;
    }

    public boolean isCanRespawn() {
        return canRespawn;
    }

    public int getRespawnDelay() {
        return respawnDelay;
    }

    public void onPlayerEliminated(Player player) {
        // To be overridden by specific games
    }

    public Pos getRespawnPos(Player p) {
        Pos spawnPos = new Pos(0, 42, 0);
        if (mapConfig != null) {
            TeamConfig team = playerTeams.get(p);
            if (team != null && team.spawns != null && !team.spawns.isEmpty()) {
                MapSpawn randomSpawn = team.spawns.get(ThreadLocalRandom.current().nextInt(team.spawns.size()));
                spawnPos = randomSpawn.toPos();
            } else if (mapConfig.spawns != null && !mapConfig.spawns.isEmpty()) {
                MapSpawn randomSpawn = mapConfig.spawns.get(ThreadLocalRandom.current().nextInt(mapConfig.spawns.size()));
                spawnPos = randomSpawn.toPos();
            }
        }
        return spawnPos;
    }

    public void applyKit(Player p) {
        Kit kit = playerKits.getOrDefault(p, defaultKit);
        if (kit != null) {
            kit.apply(p);
        }
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
        
        // Setup Explosion Supplier
        this.gameInstance.setExplosionSupplier(explosionManager.getSupplier(canBreakBlocks));
        
        // Setup Scoped Event Node
        this.gameEventNode = EventNode.event("game-" + gameId, EventFilter.ALL, event -> {
            if (event instanceof InstanceEvent ie) return ie.getInstance() == gameInstance;
            if (event instanceof EntityEvent ee) return ee.getEntity().getInstance() == gameInstance;
            return false;
        });
        
        // Setup PvP if enabled
        if (pvpEnabled) {
            this.gameEventNode.addChild(pvpManager.getEventNode());
            new ProjectileManager(this.gameEventNode, this.explosionManager);
        }
        
        // Setup Death Management
        this.deathManager = new DeathManager(this, this.gameEventNode);
        
        // Setup Block Protection
        this.gameEventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            if (!canBreakBlocks && event.getPlayer().getGameMode() != net.minestom.server.entity.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
        
        this.gameEventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (!canBreakBlocks && event.getPlayer().getGameMode() != net.minestom.server.entity.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
        
        // Register the game node globally
        MinecraftServer.getGlobalEventHandler().addChild(this.gameEventNode);
        
        this.state = GameState.IN_GAME;
        
        // Assign Teams if Config exists
        if (mapConfig != null && mapConfig.teams != null && !mapConfig.teams.isEmpty()) {
            List<Player> unassigned = new ArrayList<>(players);
            Collections.shuffle(unassigned);
            
            int teamIndex = 0;
            for (Player p : unassigned) {
                TeamConfig team = mapConfig.teams.get(teamIndex);
                // Check max players per team? For now simple round robin
                playerTeams.put(p, team);
                p.sendMessage("You are in team: " + team.name);
                
                teamIndex = (teamIndex + 1) % mapConfig.teams.size();
            }
        }
        
        // Teleport everyone to the game instance
        List<java.util.concurrent.CompletableFuture<Void>> teleportFutures = new ArrayList<>();
        
        for (Player p : players) {
            final Pos finalPos = getRespawnPos(p);
            teleportFutures.add(p.setInstance(gameInstance).thenAccept(ignored -> {
               p.teleport(finalPos); 
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
                    applyKit(p);
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
