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
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ElytraCourseManager {

    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private final Map<UUID, CourseSession> sessions = new ConcurrentHashMap<>();
    private final List<ScoreEntry> scores = new ArrayList<>();
    private final Map<Instance, List<Entity>> leaderboardEntities = new ConcurrentHashMap<>();
    private final HubManager hubManager;
    private final Gson gson = new Gson();
    private final Path scoresPath = Path.of("maps/hub/elytra_scores.json");

    private static final Pos TIME_LEADERBOARD_POS = new Pos(-5, 68, -55);
    private static final Pos ROCKET_LEADERBOARD_POS = new Pos(5, 68, -55);

    public ElytraCourseManager(HubManager hubManager) {
        this.hubManager = hubManager;
        loadCheckpoints();
        loadScores();
        setupListeners();
        startParticleTask();

        // Spawn for existing hubs
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            if (hubManager.isHub(instance)) {
                spawnLeaderboards(instance);
            }
        }

        // Listen for new hub instances
        MinecraftServer.getGlobalEventHandler().addListener(net.minestom.server.event.instance.InstanceRegisterEvent.class, event -> {
            if (hubManager.isHub(event.getInstance())) {
                spawnLeaderboards(event.getInstance());
            }
        });
    }

    private void loadCheckpoints() {
        Path path = Path.of("maps/hub/checkpoints_elytra.json");
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path)) {
            List<Checkpoint> loaded = gson.fromJson(reader, new TypeToken<List<Checkpoint>>() {}.getType());
            if (loaded != null) checkpoints.addAll(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadScores() {
        if (!Files.exists(scoresPath)) return;
        try (Reader reader = Files.newBufferedReader(scoresPath)) {
            List<ScoreEntry> loaded = gson.fromJson(reader, new TypeToken<List<ScoreEntry>>() {}.getType());
            if (loaded != null) scores.addAll(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveScores() {
        try {
            Files.createDirectories(scoresPath.getParent());
            Files.writeString(scoresPath, gson.toJson(scores));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void recordScore(Player player, long time, int rockets) {
        scores.removeIf(s -> s.uuid.equals(player.getUuid()));
        scores.add(new ScoreEntry(player.getUuid(), player.getUsername(), time, rockets));
        saveScores();
        updateLeaderboards();
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
                        session = new CourseSession();
                        session.nextCheckpointIndex = 1;
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
            if (sessions.containsKey(event.getPlayer().getUuid())) {
                sessions.remove(event.getPlayer().getUuid());
                event.getPlayer().sendMessage(Component.text("Course annulée.", NamedTextColor.RED));
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().material() == Material.FIREWORK_ROCKET) {
                CourseSession session = sessions.get(event.getPlayer().getUuid());
                if (session != null) session.rocketsUsed++;
            }
        });
    }

    private void finishRace(Player player, CourseSession session) {
        long duration = System.currentTimeMillis() - session.startTime;
        recordScore(player, duration, session.rocketsUsed);
        player.sendMessage(Component.text("--- COURSE TERMINÉE ---", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("Temps : " + String.format("%.2fs", duration / 1000.0), NamedTextColor.YELLOW));
        TKit.playSound(player.getInstance(), player.getPosition(), SoundEvent.UI_TOAST_CHALLENGE_COMPLETE.name(), Sound.Source.PLAYER, 1f, 1f);
        sessions.remove(player.getUuid());
    }

    private void spawnLeaderboards(Instance instance) {
        Entity timeDisplay = createLeaderboardEntity(instance, TIME_LEADERBOARD_POS);
        Entity rocketDisplay = createLeaderboardEntity(instance, ROCKET_LEADERBOARD_POS);
        leaderboardEntities.put(instance, new CopyOnWriteArrayList<>(List.of(timeDisplay, rocketDisplay)));
        updateLeaderboardText(timeDisplay, rocketDisplay);
    }

    private Entity createLeaderboardEntity(Instance instance, Pos pos) {
        Entity entity = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) entity.getEntityMeta();
        meta.setBillboardRenderConstraints(TextDisplayMeta.BillboardConstraints.CENTER);
        entity.setInstance(instance, pos);
        return entity;
    }

    private void updateLeaderboards() {
        for (List<Entity> displays : leaderboardEntities.values()) {
            if (displays.size() == 2) updateLeaderboardText(displays.get(0), displays.get(1));
        }
    }

    private void updateLeaderboardText(Entity timeDisplay, Entity rocketDisplay) {
        // Time Top
        List<ScoreEntry> timeTop = scores.stream().sorted(Comparator.comparingLong(s -> s.time)).limit(10).toList();
        Component timeText = Component.text("🏆 Top 10 - Chrono", NamedTextColor.GOLD, TextDecoration.BOLD).append(Component.newline());
        for (int i = 0; i < 10; i++) {
            timeText = timeText.append(Component.newline());
            if (i < timeTop.size()) {
                ScoreEntry s = timeTop.get(i);
                timeText = timeText.append(Component.text((i + 1) + ". " + s.name + " - " + String.format("%.2fs", s.time / 1000.0), NamedTextColor.WHITE));
            } else timeText = timeText.append(Component.text((i + 1) + ". ---", NamedTextColor.DARK_GRAY));
        }
        ((TextDisplayMeta) timeDisplay.getEntityMeta()).setText(timeText);

        // Rocket Top
        List<ScoreEntry> rocketTop = scores.stream().sorted(Comparator.comparingInt(s -> s.rockets)).limit(10).toList();
        Component rocketText = Component.text("🚀 Top 10 - Précision", NamedTextColor.AQUA, TextDecoration.BOLD).append(Component.newline());
        for (int i = 0; i < 10; i++) {
            rocketText = rocketText.append(Component.newline());
            if (i < rocketTop.size()) {
                ScoreEntry s = rocketTop.get(i);
                rocketText = rocketText.append(Component.text((i + 1) + ". " + s.name + " - " + s.rockets + " 🚀", NamedTextColor.WHITE));
            } else rocketText = rocketText.append(Component.text((i + 1) + ". ---", NamedTextColor.DARK_GRAY));
        }
        ((TextDisplayMeta) rocketDisplay.getEntityMeta()).setText(rocketText);
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
        double r = cp.radius();
        Pos center = cp.position();
        String axis = cp.axis().toUpperCase();
        for (int i = 0; i < 360; i += 20) {
            double angle = Math.toRadians(i);
            double cos = Math.cos(angle) * r;
            double sin = Math.sin(angle) * r;
            Pos p = switch (axis) {
                case "X" -> center.add(0, cos, sin);
                case "Y" -> center.add(cos, 0, sin);
                default -> center.add(cos, sin, 0);
            };
            TKit.spawnParticles(player.getInstance(), Particle.END_ROD, p, 0.01f, 0.01f, 0.01f, 0.01f, 1);
        }
    }

    private static class CourseSession {
        final long startTime = System.currentTimeMillis();
        int nextCheckpointIndex = 0;
        int rocketsUsed = 0;
    }

    private static class ScoreEntry {
        final UUID uuid;
        final String name;
        final long time;
        final int rockets;
        ScoreEntry(UUID uuid, String name, long time, int rockets) {
            this.uuid = uuid; this.name = name; this.time = time; this.rockets = rockets;
        }
    }
}