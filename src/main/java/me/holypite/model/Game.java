package me.holypite.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import net.minestom.server.entity.Entity;
import net.minestom.server.scoreboard.Sidebar;

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
    private final Map<String, Boolean> teamRespawnStatus = new ConcurrentHashMap<>();
    private final Map<Player, Integer> kills = new ConcurrentHashMap<>();
    private final Sidebar sidebar;
    
    // Managers
    private final PvpManager pvpManager = new PvpManager(this);
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
    private boolean canPlaceBlocks = false;
    private boolean fallDamageEnabled = true;
    private boolean fallingBlockDamage = true;
    private boolean allowDismountSneak = true;
    protected net.minestom.server.entity.GameMode gameMode = net.minestom.server.entity.GameMode.SURVIVAL;
    
    // Kits
    private final List<Kit> registeredKits = new ArrayList<>();
    private Kit defaultKit;
    private final Map<Player, Kit> playerKits = new ConcurrentHashMap<>();

    // Fall Damage Tracker
    private final Map<Player, Double> fallTracker = new ConcurrentHashMap<>();

    public Game(String gameName, int minPlayers, int maxPlayers, me.holypite.manager.MapManager mapManager) {
        this.gameId = UUID.randomUUID();
        this.gameName = gameName;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.mapManager = mapManager;
        this.players = new HashSet<>();
        this.state = GameState.LOBBY;
        
        // Sidebar Init
        this.sidebar = new Sidebar(Component.text("Minestom MiniGames", NamedTextColor.GOLD));
        
        // Create the lobby instance
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        this.lobbyInstance = instanceManager.createInstanceContainer();
        
        setupLobbyInstance(this.lobbyInstance);
        updateScoreboard();
    }
    
    public void setOnEndCallback(Runnable onEndCallback) {
        this.onEndCallback = onEndCallback;
    }
    
    protected void setPvpEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
    }
    
    protected void setGameMode(net.minestom.server.entity.GameMode gameMode) {
        this.gameMode = gameMode;
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
    
    protected void setCanPlaceBlocks(boolean canPlaceBlocks) {
        this.canPlaceBlocks = canPlaceBlocks;
    }
    
    protected void setFallDamageEnabled(boolean fallDamageEnabled) {
        this.fallDamageEnabled = fallDamageEnabled;
    }
    
    protected void setFallingBlockDamage(boolean fallingBlockDamage) {
        this.fallingBlockDamage = fallingBlockDamage;
    }
    
    protected void setAllowDismountSneak(boolean allowDismountSneak) {
        this.allowDismountSneak = allowDismountSneak;
    }
    
    public boolean isFallingBlockDamage() {
        return fallingBlockDamage;
    }

    public boolean isCanRespawn(Player player) {
        TeamConfig team = playerTeams.get(player);
        if (team != null && teamRespawnStatus.containsKey(team.name)) {
            return teamRespawnStatus.get(team.name);
        }
        return canRespawn;
    }

    public void setTeamRespawnStatus(String teamName, boolean canRespawn) {
        teamRespawnStatus.put(teamName, canRespawn);
    }

    public void addKill(Player player) {
        kills.merge(player, 1, Integer::sum);
        player.sendMessage(Component.text("Kill! (Total: " + kills.get(player) + ")", NamedTextColor.GOLD));
        updateScoreboard();
    }

    public int getKills(Player player) {
        return kills.getOrDefault(player, 0);
    }

    public boolean isSameTeam(Entity a, Entity b) {
        if (!(a instanceof Player p1) || !(b instanceof Player p2)) return false;
        TeamConfig t1 = playerTeams.get(p1);
        TeamConfig t2 = playerTeams.get(p2);
        return t1 != null && t1.equals(t2);
    }

    private void updateScoreboard() {
        // Clear all lines
        for (Sidebar.ScoreboardLine line : sidebar.getLines()) {
            sidebar.removeLine(line.getId());
        }

        sidebar.createLine(new Sidebar.ScoreboardLine("line_sep", Component.text("----------------", NamedTextColor.GRAY), 10));
        sidebar.createLine(new Sidebar.ScoreboardLine("line_game", Component.text("Game: ", NamedTextColor.WHITE).append(Component.text(gameName, NamedTextColor.YELLOW)), 9));
        sidebar.createLine(new Sidebar.ScoreboardLine("line_state", Component.text("State: ", NamedTextColor.WHITE).append(Component.text(state.name(), NamedTextColor.YELLOW)), 8));
        sidebar.createLine(new Sidebar.ScoreboardLine("line_sep2", Component.empty(), 7));
        sidebar.createLine(new Sidebar.ScoreboardLine("line_kills_title", Component.text("Top Kills:", NamedTextColor.GOLD), 6));

        // Sort players by kills
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(getKills(p2), getKills(p1)));

        int rank = 5;
        for (int i = 0; i < Math.min(sortedPlayers.size(), 5); i++) {
            Player p = sortedPlayers.get(i);
            sidebar.createLine(new Sidebar.ScoreboardLine("kill_" + i, 
                Component.text(p.getUsername() + ": ", NamedTextColor.WHITE).append(Component.text(getKills(p), NamedTextColor.GREEN)), 
                rank--));
        }
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
        sidebar.addViewer(player);
        updateScoreboard();

        player.setInstance(lobbyInstance, new Pos(0, 42, 0)).thenAccept(ignored -> {
            player.setGameMode(net.minestom.server.entity.GameMode.ADVENTURE);
            onPlayerJoin(player);
            checkStart();
        });
    }

    public void removePlayer(Player player) {
        players.remove(player);
        sidebar.removeViewer(player);
        playerTeams.remove(player); 
        playerKits.remove(player);
        player.getInventory().clear(); 
        onPlayerQuit(player);
        
        updateScoreboard();

        if (state == GameState.IN_GAME) {
            checkWinCondition();
        }
        
        if (players.isEmpty()) {
            if (state == GameState.LOBBY || state == GameState.STARTING) {
                if (lobbyInstance != null) {
                    MinecraftServer.getInstanceManager().unregisterInstance(lobbyInstance);
                    lobbyInstance = null;
                }
                if (onEndCallback != null) {
                    onEndCallback.run();
                }
            } else {
                endGame(); 
            }
        }
    }
    
    public boolean isAlive(Player player) {
        if (deathManager == null) return true;
        return !deathManager.isGhost(player);
    }

    public void checkWinCondition() {
        List<Player> livingPlayers = players.stream().filter(this::isAlive).toList();
        
        if (livingPlayers.size() < 2) { 
            if (minPlayers > 1) {
                if (livingPlayers.size() == 1) {
                    sendMessageToAll("Game Over! Victory for " + livingPlayers.get(0).getUsername());
                } else {
                    sendMessageToAll("Not enough players to continue.");
                }
                endGame();
                return;
            }
        }
        
        if (!playerTeams.isEmpty()) {
            Set<String> activeTeams = new HashSet<>();
            for (Player p : livingPlayers) {
                TeamConfig team = playerTeams.get(p);
                if (team != null) {
                    activeTeams.add(team.name);
                }
            }
            
            if (activeTeams.size() <= 1 && livingPlayers.size() > 0) {
                sendMessageToAll("Game Over! Victory for " + activeTeams.iterator().next());
                endGame();
            }
        }
    }

    private void checkStart() {
        if (state == GameState.LOBBY && players.size() >= minPlayers) {
            startCountdown();
        }
    }

    private void startCountdown() {
        this.state = GameState.STARTING;
        updateScoreboard();
        sendMessageToAll("Game starting in 5 seconds...");
        
        MinecraftServer.getSchedulerManager().buildTask(() -> startGame(false))
                .delay(TaskSchedule.seconds(5))
                .schedule();
    }
    
    public void forceStart() {
        startGame(true);
    }

    public void startGame(boolean force) {
        if (!force && players.size() < minPlayers) {
            this.state = GameState.LOBBY;
            updateScoreboard();
            sendMessageToAll("Not enough players to start. Countdown cancelled.");
            return;
        }
        
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        this.gameInstance = instanceManager.createInstanceContainer();
        setupGameInstance(this.gameInstance);
        
        this.gameInstance.setExplosionSupplier(explosionManager.getSupplier(canBreakBlocks));
        
        this.gameEventNode = EventNode.event("game-" + gameId, EventFilter.ALL, event -> {
            if (event instanceof InstanceEvent ie) return ie.getInstance() == gameInstance;
            if (event instanceof EntityEvent ee) return ee.getEntity().getInstance() == gameInstance;
            return false;
        });
        
        // Setup PvP if enabled
        if (pvpEnabled) {
            this.gameEventNode.addChild(pvpManager.getEventNode());
            this.projectileManager = new ProjectileManager(this.gameEventNode, this.explosionManager);
            this.projectileManager.setGame(this);
        }
        
        this.deathManager = new DeathManager(this, this.gameEventNode);
        
        this.gameEventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            if (!canBreakBlocks && event.getPlayer().getGameMode() != net.minestom.server.entity.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
        
        this.gameEventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (!canPlaceBlocks && event.getPlayer().getGameMode() != net.minestom.server.entity.GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
        
        this.gameEventNode.addListener(net.minestom.server.event.player.PlayerMoveEvent.class, event -> {
            if (!fallDamageEnabled) return;
            Player p = event.getPlayer();
            if (p.getGameMode() == net.minestom.server.entity.GameMode.CREATIVE || p.getGameMode() == net.minestom.server.entity.GameMode.SPECTATOR) return;
            
            double currentY = event.getNewPosition().y();
            
            if (event.isOnGround()) {
                if (fallTracker.containsKey(p)) {
                    double highestY = fallTracker.get(p);
                    double distance = highestY - currentY;
                    
                    if (distance > 3.0) {
                        float damage = (float) (distance - 3.0);
                        p.damage(me.holypite.manager.damage.DamageSources.fall(damage));
                    }
                }
                fallTracker.put(p, currentY);
            } else {
                double highestY = fallTracker.getOrDefault(p, currentY);
                if (currentY > highestY) {
                    fallTracker.put(p, currentY);
                }
            }
        });
        
        this.gameEventNode.addListener(net.minestom.server.event.player.PlayerDisconnectEvent.class, event -> {
            fallTracker.remove(event.getPlayer());
        });
        
        this.gameEventNode.addListener(net.minestom.server.event.player.PlayerTickEvent.class, event -> {
            if (!fallDamageEnabled) return;
            Player p = event.getPlayer();
            if (p.getVehicle() != null) {
                fallTracker.put(p, p.getPosition().y());
            }
        });

        this.gameEventNode.addListener(net.minestom.server.event.player.PlayerStartSneakingEvent.class, event -> {
            if (!allowDismountSneak) return;
            Player player = event.getPlayer();
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                vehicle.removePassenger(player);
            }
        });
        
        MinecraftServer.getGlobalEventHandler().addChild(this.gameEventNode);
        
        this.state = GameState.IN_GAME;
        updateScoreboard();
        
        if (mapConfig != null && mapConfig.teams != null && !mapConfig.teams.isEmpty()) {
            List<Player> unassigned = new ArrayList<>(players);
            Collections.shuffle(unassigned);
            
            int teamIndex = 0;
            for (Player p : unassigned) {
                TeamConfig team = mapConfig.teams.get(teamIndex);
                playerTeams.put(p, team);
                p.sendMessage("You are in team: " + team.name);
                
                teamIndex = (teamIndex + 1) % mapConfig.teams.size();
            }
        }
        
        List<CompletableFuture<Void>> teleportFutures = new ArrayList<>();
        
        for (Player p : players) {
            final Pos finalPos = getRespawnPos(p);
            teleportFutures.add(p.setInstance(gameInstance).thenAccept(ignored -> {
               p.teleport(finalPos); 
            }));
        }
        
        CompletableFuture.allOf(teleportFutures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                if (instanceManager != null) {
                   instanceManager.unregisterInstance(lobbyInstance);
                } else {
                    MinecraftServer.getInstanceManager().unregisterInstance(lobbyInstance);
                }
                this.lobbyInstance = null;
                
                for (Player p : players) {
                    p.heal();
                    p.setFood(20);
                    p.setGameMode(gameMode);
                    applyKit(p);
                }
                
                sendMessageToAll("Game Started!");
                onGameStart();
            });
    }

    public void endGame() {
        this.state = GameState.ENDING;
        updateScoreboard();
        onGameEnd();
        
        if (this.gameEventNode != null) {
            MinecraftServer.getGlobalEventHandler().removeChild(this.gameEventNode);
            this.gameEventNode = null;
        }

        sendMessageToAll("Game Ended. Sending you back to Hub in 5 seconds...");
        
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (onEndCallback != null) {
                onEndCallback.run();
            }
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

    public net.minestom.server.entity.GameMode getGameMode() {
        return gameMode;
    }

    public double getVoidY() {
        if (mapConfig != null && mapConfig.voidY != null) {
            return mapConfig.voidY;
        }
        return -10.0; 
    }
    
    protected EventNode<Event> getGameEventNode() {
        return gameEventNode;
    }
}