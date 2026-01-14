package me.holypite.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.holypite.model.CourseSession;
import me.holypite.model.map.ElytraCourseConfig;
import me.holypite.model.map.LoadedMap;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.particle.Particle;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ElytraCourseManager {

    private final Map<UUID, CourseSession> sessions = new ConcurrentHashMap<>();
    private final List<ScoreEntry> scores = new ArrayList<>();
    private final File scoreFile = new File("elytra_scores.json");
    private final Gson gson = new Gson();
    
    // Config loaded from map
    private ElytraCourseConfig config;
    
    // Display entity for leaderboard
    private final Map<Instance, Entity> leaderboards = new ConcurrentHashMap<>();

    public ElytraCourseManager() {
        loadScores();
        
        // Global listener for rocket usage and movement
        EventNode<Event> node = EventNode.all("elytra-course");
        
        node.addListener(PlayerMoveEvent.class, this::onMove);
        node.addListener(PlayerUseItemEvent.class, this::onUseItem);
        
        MinecraftServer.getGlobalEventHandler().addChild(node);
        
        // Particle task
        MinecraftServer.getSchedulerManager().buildTask(this::tickParticles)
                .repeat(TaskSchedule.tick(5))
                .schedule();
    }

    public void setup(Instance instance, ElytraCourseConfig config) {
        this.config = config;
        if (config == null) return;
        
        // Setup start pad
        instance.setBlock(config.start, Block.HEAVY_WEIGHTED_PRESSURE_PLATE);
        
        // Setup Leaderboard
        spawnLeaderboard(instance);
    }

    private void spawnLeaderboard(Instance instance) {
        if (config == null || config.leaderboardPos == null) return;
        
        Entity leaderboard = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) leaderboard.getEntityMeta();
        meta.setBillboardRenderConstraints(TextDisplayMeta.BillboardConstraints.CENTER);
        meta.setScale(new Vec(1.5, 1.5, 1.5));
        
        updateLeaderboardText(leaderboard);
        
        leaderboard.setInstance(instance, config.leaderboardPos);
        leaderboards.put(instance, leaderboard);
    }
    
    private void updateLeaderboardText(Entity entity) {
        if (entity == null) return;
        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
        
        Component text = Component.text("Top 10 Elytra Course", NamedTextColor.GOLD).append(Component.newline());
        
        scores.sort(Comparator.comparingLong(ScoreEntry::time).thenComparingInt(ScoreEntry::rockets));
        
        int count = 0;
        for (ScoreEntry score : scores) {
            if (count >= 10) break;
            text = text.append(Component.newline())
                    .append(Component.text((count + 1) + ". " + score.playerName + " - " + formatTime(score.time) + " (" + score.rockets + " \uD83D\uDE80)", NamedTextColor.YELLOW));
            count++;
        }
        
        meta.setText(text);
    }
    
    private void updateAllLeaderboards() {
        leaderboards.values().forEach(this::updateLeaderboardText);
    }

    private void onMove(PlayerMoveEvent event) {
        if (config == null) return;
        Player player = event.getPlayer();
        CourseSession session = sessions.get(player.getUuid());
        
        if (session == null) {
            // Check for start
            if (player.getInstance().getBlock(player.getPosition().sub(0, 0.5, 0)).compare(Block.HEAVY_WEIGHTED_PRESSURE_PLATE)) {
                if (player.getPosition().distance(config.start) < 2) {
                    startSession(player);
                }
            }
        } else {
            // Check checkpoints
            if (session.getNextCheckpointIndex() < config.checkpoints.size()) {
                Pos target = config.checkpoints.get(session.getNextCheckpointIndex());
                if (player.getPosition().distance(target) < 3.0) {
                    session.incrementCheckpointIndex();
                    player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f));
                    player.sendMessage(Component.text("Checkpoint " + session.getNextCheckpointIndex() + "/" + config.checkpoints.size(), NamedTextColor.GREEN));
                }
            } else {
                // Check finish (return to start or specific finish line? Assuming start is finish for loop or last checkpoint is finish)
                // Let's assume the last checkpoint is the finish line for simplicity based on "passer dans les anneaux dans un ordre donner".
                // Or maybe return to start? The user said "classer en fonction du temps". Usually start -> rings -> finish.
                // Let's assume the last point in checkpoints is the finish.
                finishSession(player, session);
            }
        }
    }

    private void onUseItem(PlayerUseItemEvent event) {
        if (event.getItemStack().material() == Material.FIREWORK_ROCKET) {
            CourseSession session = sessions.get(event.getPlayer().getUuid());
            if (session != null) {
                session.incrementRocketsUsed();
                // Infinite rockets: Give back the item if consumed (handled by game mode mostly, but let's ensure)
                // Actually creative/adventure might not consume.
                // But let's cancel the event to prevent consumption if not in creative? 
                // Wait, if we cancel, the rocket won't boost.
                // So we let it happen, and ensure player has rockets.
                event.getPlayer().getInventory().setItemStack(event.getPlayer().getHeldSlot(), event.getItemStack());
            }
        }
    }
    
    private void startSession(Player player) {
        if (sessions.containsKey(player.getUuid())) return;
        
        CourseSession session = new CourseSession(player);
        sessions.put(player.getUuid(), session);
        
        // Give gear
        player.getInventory().setEquipment(EquipmentSlot.CHESTPLATE, (byte)0, ItemStack.of(Material.ELYTRA));
        player.getInventory().setItemStack(player.getHeldSlot(), ItemStack.of(Material.FIREWORK_ROCKET));
        
        player.sendMessage(Component.text("Go! Course started!", NamedTextColor.GREEN));
        player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 0.5f));
    }
    
    private void finishSession(Player player, CourseSession session) {
        sessions.remove(player.getUuid());
        long time = System.currentTimeMillis() - session.getStartTime();
        
        player.sendMessage(Component.text("Finished in " + formatTime(time) + " using " + session.getRocketsUsed() + " rockets!", NamedTextColor.GOLD));
        player.playSound(Sound.sound(SoundEvent.UI_TOAST_CHALLENGE_COMPLETE, Sound.Source.MASTER, 1f, 1f));
        
        // Remove gear
        player.getInventory().setEquipment(EquipmentSlot.CHESTPLATE, (byte)0, ItemStack.AIR);
        
        // Remove all rockets from inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack stack = player.getInventory().getItemStack(i);
            if (stack.material() == Material.FIREWORK_ROCKET) {
                player.getInventory().setItemStack(i, ItemStack.AIR);
            }
        }
        
        // Save score
        addScore(player.getUsername(), time, session.getRocketsUsed());
    }
    
    private void tickParticles() {
        if (config == null || config.checkpoints == null) return;
        
        for (Instance instance : leaderboards.keySet()) {
             // Draw checkpoints
             int index = 0;
             for (Pos checkpoint : config.checkpoints) {
                 // Draw a simple circle or point
                 // For now just a point is enough to see
                 // Use Particle.fromKey to avoid symbol issues if constant missing
                 Particle rod = Particle.fromKey("minecraft:end_rod");
                 if (rod != null) {
                     ParticlePacket packet = new ParticlePacket(
                             rod,
                             true,
                             false,
                             checkpoint.x(), checkpoint.y(), checkpoint.z(),
                             0.5f, 0.5f, 0.5f,
                             0.1f, 5
                     );
                     instance.getPlayers().forEach(p -> p.sendPacket(packet));
                 }
                     
                 // If it's the next checkpoint for this player, make it glow more or different color
                 int finalIndex = index;
                 Particle happy = Particle.fromKey("minecraft:happy_villager");
                 if (happy != null) {
                     instance.getPlayers().forEach(p -> {
                         CourseSession session = sessions.get(p.getUuid());
                         if (session != null && session.getNextCheckpointIndex() == finalIndex) {
                             p.sendPacket(new ParticlePacket(
                                     happy,
                                     true,
                                     false,
                                     checkpoint.x(), checkpoint.y(), checkpoint.z(),
                                     1f, 1f, 1f,
                                     0.1f, 10
                             ));
                         }
                     });
                 }
                 index++;
             }
        }
    }

    private void addScore(String name, long time, int rockets) {
        scores.add(new ScoreEntry(name, time, rockets));
        saveScores();
        updateAllLeaderboards();
    }
    
    private void loadScores() {
        if (scoreFile.exists()) {
            try (FileReader reader = new FileReader(scoreFile)) {
                Type type = new TypeToken<List<ScoreEntry>>(){}.getType();
                List<ScoreEntry> loaded = gson.fromJson(reader, type);
                if (loaded != null) scores.addAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveScores() {
        try (FileWriter writer = new FileWriter(scoreFile)) {
            gson.toJson(scores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String formatTime(long millis) {
        long sec = millis / 1000;
        long ms = millis % 1000;
        return String.format("%d.%03ds", sec, ms);
    }

    private record ScoreEntry(String playerName, long time, int rockets) {}
}
