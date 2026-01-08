package me.holypite.games.implem;

import me.holypite.model.Game;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

public class TestGame extends Game {

    public TestGame() {
        super("Test Game", 1, 10);
        setPvpEnabled(true);
    }

    @Override
    public void setupLobbyInstance(InstanceContainer instance) {
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.OAK_PLANKS));
    }

    @Override
    public void setupGameInstance(InstanceContainer instance) {
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.STONE));
    }

    @Override
    public void onPlayerJoin(Player player) {
        sendMessageToAll(player.getUsername() + " has joined the Test Game!");
    }

    @Override
    public void onPlayerQuit(Player player) {
        sendMessageToAll(player.getUsername() + " left the Test Game.");
    }

    @Override
    public void onGameStart() {
        sendMessageToAll("Test Game Logic Starting... Fight! (Game ends in 15s)");
        // Logic for game specific stuff
        
        MinecraftServer.getSchedulerManager().buildTask(this::endGame)
                .delay(TaskSchedule.seconds(15))
                .schedule();
    }

    @Override
    public void onGameEnd() {
        sendMessageToAll("Test Game Over!");
    }
}
