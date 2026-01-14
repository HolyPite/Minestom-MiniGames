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
        setCanRespawn(true); // For debug
        setRespawnDelay(5);
        setCanBreakBlocks(true); // Explosions will destroy blocks!
        setGameMode(net.minestom.server.entity.GameMode.SURVIVAL);
        setAllowDismountSneak(true);
        
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
        // Wool Drop on Sheep Death (Lethal Damage Check)
        getGameEventNode().addListener(net.minestom.server.event.entity.EntityDamageEvent.class, event -> {
            if (event.getEntity() instanceof me.holypite.games.sheepwars.sheeps.SheepProjectile sheep) {
                // Check if lethal
                float currentHealth = sheep.getHealth();
                float damageAmount = event.getDamage().getAmount();
                
                boolean dead = sheep.getHealth() <= 0;
                
                if (!dead && (currentHealth - damageAmount <= 0)) {
                    dead = true;
                }
                
                if (dead && event.getDamage().getAttacker() instanceof Player killer) {
                    String id = sheep.getId();
                    ItemStack wool = SheepRegistry.getSheepItemById(id);
                    if (wool != ItemStack.AIR) {
                        killer.getInventory().addItemStack(wool);
                        killer.sendMessage(net.kyori.adventure.text.Component.text("You recovered a " + id + " sheep!", net.kyori.adventure.text.format.NamedTextColor.GREEN));
                        
                        // Feedback: Sound & Particles
                        killer.playSound(net.kyori.adventure.sound.Sound.sound(net.minestom.server.sound.SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, net.kyori.adventure.sound.Sound.Source.PLAYER, 1f, 1.5f));
                        
                        killer.getInstance().sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                                net.minestom.server.particle.Particle.HAPPY_VILLAGER,
                                sheep.getPosition().add(0, 0.5, 0),
                                new net.minestom.server.coordinate.Vec(0.2, 0.2, 0.2),
                                0.05f, 15
                        ));
                    }
                }
            }
        });

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