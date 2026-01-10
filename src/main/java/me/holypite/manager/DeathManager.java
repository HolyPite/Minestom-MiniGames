package me.holypite.manager;

import me.holypite.model.Game;
import me.holypite.model.GameState;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.HashSet;
import java.util.Set;

public class DeathManager {

    private final Game game;
    private final EventNode<Event> node;
    private final Set<Player> ghosts = new HashSet<>();

    public DeathManager(Game game, EventNode<Event> node) {
        this.game = game;
        this.node = node;
        registerListeners();
    }

    private void registerListeners() {
        node.addListener(EntityDamageEvent.class, event -> {
            if (!(event.getEntity() instanceof Player player)) return;
            if (ghosts.contains(player)) {
                event.setCancelled(true);
                return;
            }

            float finalHealth = player.getHealth() - event.getDamage().getAmount();
            if (finalHealth <= 0) {
                event.setCancelled(true);
                handleDeath(player);
            }
        });
    }

    public void handleDeath(Player player) {
        if (game.getState() != GameState.IN_GAME) return;

        // 1. Become Ghost
        ghosts.add(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvisible(true);
        player.setAllowFlying(true);
        player.setFlying(true);
        player.getInventory().clear();
        
        player.sendMessage("You died!");

        // 2. Check if can respawn
        if (game.isCanRespawn()) {
            player.sendMessage("Respawning in " + game.getRespawnDelay() + " seconds...");
            MinecraftServer.getSchedulerManager().buildTask(() -> respawn(player))
                    .delay(TaskSchedule.seconds(game.getRespawnDelay()))
                    .schedule();
        } else {
            player.sendMessage("You are out! Waiting for game end...");
            // Custom logic: Call a method in Game to notify an elimination
            game.onPlayerEliminated(player);
            // Check if game should end
            game.checkWinCondition();
        }
    }

    private void respawn(Player player) {
        if (!ghosts.contains(player) || game.getState() != GameState.IN_GAME) return;

        // Reset state
        ghosts.remove(player);
        player.setInvisible(false);
        player.setAllowFlying(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.heal();
        player.setFood(20);

        // Teleport to spawn
        Pos spawnPos = game.getRespawnPos(player);
        player.teleport(spawnPos);

        // Give kit again
        game.applyKit(player);

        player.sendMessage("Respawned!");
    }

    public boolean isGhost(Player player) {
        return ghosts.contains(player);
    }
}
