package me.holypite.games.sheepwars;

import me.holypite.games.sheepwars.SheepRegistry;
import me.holypite.manager.MapManager;
import me.holypite.model.Game;
import me.holypite.model.map.LoadedMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;

public class SheepWarsGame extends Game {

    public SheepWarsGame(MapManager mapManager) {
        super("SheepWars", 1, 8, mapManager);
        setPvpEnabled(true);
        setCanRespawn(false); // Elimination mode
        setRespawnDelay(5);
        setCanBreakBlocks(true); // Explosions will destroy blocks!
        
        // Register default kit
        registerKit(new SheepWarsKit()); 
    }

    @Override
    public void setupLobbyInstance(InstanceContainer instance) {
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.OAK_PLANKS));
    }

    @Override
    public void setupGameInstance(InstanceContainer instance) {
        // Try to load map
        // We reuse "map_test" for now, ideally create "sheepwars_arena"
        LoadedMap loadedMap = mapManager.createInstanceFromMap("map_test");
        if (loadedMap != null) {
            instance.setChunkLoader(loadedMap.getInstance().getChunkLoader());
            this.mapConfig = loadedMap.getConfig();
        } else {
            // Fallback
            instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        }
        
        instance.setWorldBorder(instance.getWorldBorder().withDiameter(100));
    }

    @Override
    public void onPlayerJoin(Player player) {
        sendMessageToAll(player.getUsername() + " joined the SheepWars!");
    }

    @Override
    public void onPlayerQuit(Player player) {
        sendMessageToAll(player.getUsername() + " left.");
    }

    @Override
    public void onGameStart() {
        sendMessageToAll("SheepWars Started! Destroy everything!");
        
        // Give Sheeps every 10 seconds
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (getState() != me.holypite.model.GameState.IN_GAME) {
                return TaskSchedule.stop();
            }
            
            for (Player p : getPlayers()) {
                if (isAlive(p)) {
                    ItemStack sheepItem = SheepRegistry.getRandomSheepItem();
                    if (sheepItem != ItemStack.AIR) {
                        p.getInventory().addItemStack(sheepItem);
                        p.sendMessage("You received a sheep!");
                    }
                }
            }
            
            return TaskSchedule.seconds(10);
        });
    }

    @Override
    public void onGameEnd() {
        sendMessageToAll("SheepWars Ended!");
    }
}