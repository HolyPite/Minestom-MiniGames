package me.holypite.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.holypite.games.elytra.Checkpoint;
import me.holypite.utils.TKit;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerStopFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElytraCourseManager {

    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private final Map<UUID, CourseSession> sessions = new ConcurrentHashMap<>();
    private final HubManager hubManager;
    private final Gson gson = new Gson();

    public ElytraCourseManager(HubManager hubManager) {
        this.hubManager = hubManager;
        loadCheckpoints();
        setupListeners();
        startParticleTask();
    }

    private void loadCheckpoints() {
        Path path = Path.of("maps/hub/checkpoints_elytra.json");
        if (!Files.exists(path)) {
            System.err.println("Elytra checkpoints file not found at " + path.toAbsolutePath());
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            List<Checkpoint> loaded = gson.fromJson(reader, new TypeToken<List<Checkpoint>>() {}.getType());
            if (loaded != null) {
                checkpoints.addAll(loaded);
                System.out.println("Loaded " + checkpoints.size() + " elytra checkpoints.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            if (!hubManager.isHub(player.getInstance())) return;

            CourseSession session = sessions.get(player.getUuid());
            int nextIdx = (session == null) ? 0 : session.nextCheckpointIndex;

            if (nextIdx < checkpoints.size()) {
                Checkpoint cp = checkpoints.get(nextIdx);
                if (cp.isPassed(player.getPosition(), event.getNewPosition())) {
                    if (session == null) {
                        // Start race
                        session = new CourseSession();
                        sessions.put(player.getUuid(), session);
                        player.sendMessage(Component.text("Course démarrée !", NamedTextColor.GREEN, TextDecoration.BOLD));
                        TKit.playSound(player.getInstance(), player.getPosition(), SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP.name(), Sound.Source.PLAYER, 1f, 1f);
                    } else {
                        session.nextCheckpointIndex++;
                        if (session.nextCheckpointIndex >= checkpoints.size()) {
                            finishRace(player, session);
                        } else {
                            player.sendMessage(Component.text("Checkpoint " + session.nextCheckpointIndex + "/" + checkpoints.size(), NamedTextColor.AQUA));
                            TKit.playSound(player.getInstance(), player.getPosition(), SoundEvent.BLOCK_NOTE_BLOCK_PLING.name(), Sound.Source.PLAYER, 1f, 2f);
                        }
                    }
                }
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerStopFlyingWithElytraEvent.class, event -> {
            Player player = event.getPlayer();
            if (sessions.containsKey(player.getUuid())) {
                sessions.remove(player.getUuid());
                player.sendMessage(Component.text("Course annulée (vol interrompu).", NamedTextColor.RED));
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().material() == Material.FIREWORK_ROCKET) {
                CourseSession session = sessions.get(event.getPlayer().getUuid());
                if (session != null) {
                    session.rocketsUsed++;
                }
            }
        });
    }

    private void finishRace(Player player, CourseSession session) {
        long duration = System.currentTimeMillis() - session.startTime;
        double seconds = duration / 1000.0;

        player.sendMessage(Component.text("--- COURSE TERMINÉE ---", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("Temps : ", NamedTextColor.YELLOW).append(Component.text(String.format("%.2fs", seconds), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Fusées : ", NamedTextColor.YELLOW).append(Component.text(session.rocketsUsed, NamedTextColor.WHITE)));

        TKit.playSound(player.getInstance(), player.getPosition(), SoundEvent.UI_TOAST_CHALLENGE_COMPLETE.name(), Sound.Source.PLAYER, 1f, 1f);
        sessions.remove(player.getUuid());
    }

    private void startParticleTask() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (Map.Entry<UUID, CourseSession> entry : sessions.entrySet()) {
                Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.getKey());
                if (player == null || player.getInstance() == null) continue;

                CourseSession session = entry.getValue();
                if (session.nextCheckpointIndex < checkpoints.size()) {
                    Checkpoint cp = checkpoints.get(session.nextCheckpointIndex);
                    spawnRingParticles(player, cp);
                }
            }
        }).repeat(TaskSchedule.tick(5)).schedule();
    }

    private void spawnRingParticles(Player player, Checkpoint cp) {
        double radius = cp.radius();
        Pos center = cp.position();
        String axis = cp.axis().toUpperCase();

        for (int i = 0; i < 360; i += 20) {
            double angle = Math.toRadians(i);
            double cos = Math.cos(angle) * radius;
            double sin = Math.sin(angle) * radius;

            Pos particlePos = switch (axis) {
                case "X" -> center.add(0, cos, sin);
                case "Y" -> center.add(cos, 0, sin);
                default -> center.add(cos, sin, 0); // Z
            };

            TKit.spawnParticles(player.getInstance(), Particle.END_ROD, particlePos, 0.01f, 0.01f, 0.01f, 0.01f, 1);
        }
    }

    private static class CourseSession {
        final long startTime = System.currentTimeMillis();
        int nextCheckpointIndex = 0;
        int rocketsUsed = 0;
    }
}
