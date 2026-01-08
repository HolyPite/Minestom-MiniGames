package me.holypite.games.implem;

import me.holypite.manager.MapManager;

import me.holypite.model.Game;

import net.minestom.server.entity.Player;

import net.minestom.server.event.Event;

import net.minestom.server.event.EventNode;

import net.minestom.server.event.player.PlayerDeathEvent;

import net.minestom.server.instance.InstanceContainer;

import net.minestom.server.instance.block.Block;

import me.holypite.games.kits.ClassicKit;



public class DuelGame extends Game {



    private EventNode<Event> gameNode;



    public DuelGame(MapManager mapManager) {

        super("Duel 1v1", 2, 2, mapManager);

        setPvpEnabled(true);

        registerKit(new ClassicKit());

    }

    @Override
    public void setupLobbyInstance(InstanceContainer instance) {
        // Simple glass platform for lobby
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GLASS));
    }

    @Override
    public void setupGameInstance(InstanceContainer instance) {
        // Small arena for duel
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.SANDSTONE));
        instance.setWorldBorder(instance.getWorldBorder().withDiameter(50)); // Limit arena size
    }

    @Override
    public void onPlayerJoin(Player player) {
        sendMessageToAll(player.getUsername() + " joined the Duel!");
    }

    @Override
    public void onPlayerQuit(Player player) {
        sendMessageToAll(player.getUsername() + " fled the fight!");
        // If someone quits during game, the other wins automatically
        if (getState().toString().equals("IN_GAME")) {
            endGame(); 
        }
    }

    @Override
    public void onGameStart() {
        sendMessageToAll("Duel started! Fight to the death!");
        
        getGameEventNode().addListener(PlayerDeathEvent.class, event -> {
            Player victim = event.getPlayer();
            sendMessageToAll(victim.getUsername() + " was eliminated!");
            
            // Find winner (the other player)
            for (Player p : getPlayers()) {
                if (!p.equals(victim)) {
                    sendMessageToAll("Winner: " + p.getUsername());
                    break;
                }
            }
            
            // End game
            endGame();
        });
    }

    @Override
    public void onGameEnd() {
        sendMessageToAll("Duel ended!");
    }
}
