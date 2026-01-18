package me.holypite.manager;

import me.holypite.games.elytra.Checkpoint;
import me.holypite.utils.TKit;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerStopFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElytraCourseManager {

    private static final List<Checkpoint> CHECKPOINTS = List.of(
            new Checkpoint(1, new Pos(0.5, 63, 53), 3),
            new Checkpoint(2, new Pos(0, 58, 79), 3),
            new Checkpoint(3, new Pos(30, 52, 94), 3),
            new Checkpoint(4, new Pos(88, 127, 123), 3),
            new Checkpoint(5, new Pos(87, 125, 96), 10),
            new Checkpoint(6, new Pos(80, 125, 20), 3),
            new Checkpoint(7, new Pos(82, 124, -19), 2),
            new Checkpoint(8, new Pos(54, 94, -71), 3),
            new Checkpoint(9, new Pos(-17, 70, -65), 3),
            new Checkpoint(10, new Pos(-54, 70, -47), 3),
            new Checkpoint(11, new Pos(-81, 82, -43), 3),
            new Checkpoint(12, new Pos(-100, 97, -77), 3),
            new Checkpoint(13, new Pos(-53, 117, -96), 3),
            new Checkpoint(14, new Pos(-23, 125, -95), 3),
            new Checkpoint(15, new Pos(-1, 127, -92), 3),
            new Checkpoint(16, new Pos(13, 133, -116), 3),
            new Checkpoint(17, new Pos(11, 137, -155), 5),
            new Checkpoint(18, new Pos(35, 150, -203), 3),
            new Checkpoint(19, new Pos(73, 145, -202), 5),
            new Checkpoint(20, new Pos(110, 133, -190), 3),
            new Checkpoint(21, new Pos(143, 127, -169), 4),
            new Checkpoint(22, new Pos(141, 123, -128), 2),
            new Checkpoint(23, new Pos(155, 156, -126), 2),
            new Checkpoint(24, new Pos(153, 194, -142), 2),
            new Checkpoint(25, new Pos(128, 199, -142), 2),
            new Checkpoint(26, new Pos(99, 190, -156), 2),
            new Checkpoint(27, new Pos(44, 199, -159), 4),
            new Checkpoint(28, new Pos(20, 201, -110), 4),
            new Checkpoint(29, new Pos(17, 202, -62), 4),
            new Checkpoint(30, new Pos(12, 194, 24), 4),
            new Checkpoint(31, new Pos(-47, 180, 66), 4),
            new Checkpoint(32, new Pos(-96, 179, 34), 4),
            new Checkpoint(33, new Pos(-138, 176, 15), 4),
            new Checkpoint(34, new Pos(-141, 167, -24), 4),
            new Checkpoint(35, new Pos(-141, 148, -91), 4),
            new Checkpoint(36, new Pos(-137, 135, -146), 4),
            new Checkpoint(37, new Pos(-98, 126, -172), 4),
            new Checkpoint(38, new Pos(-14, 96, -172), 4),
            new Checkpoint(39, new Pos(20, 63, -129), 4),
            new Checkpoint(40, new Pos(16, 49, -80), 4),
            new Checkpoint(41, new Pos(9, 24, 29), 4),
            new Checkpoint(42, new Pos(10, 11, 60), 4),
            new Checkpoint(43, new Pos(-49, 1, 70), 4),
            new Checkpoint(44, new Pos(-85, -9, 68), 4),
            new Checkpoint(45, new Pos(-120, 12, 0), 4),
            new Checkpoint(46, new Pos(-121, 34, -37), 4),
            new Checkpoint(47, new Pos(-120, 52, -83), 4),
            new Checkpoint(48, new Pos(-83, 58, -92), 3),
            new Checkpoint(49, new Pos(-33, 51, -94), 3),
            new Checkpoint(50, new Pos(0, 66, -51), 3)
    );

    private final Map<UUID, CourseSession> sessions = new ConcurrentHashMap<>();
    private final HubManager hubManager;

    public ElytraCourseManager(HubManager hubManager) {
        this.hubManager = hubManager;
        setupListeners();
        startParticleTask();
    }

    private void setupListeners() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            if (!hubManager.isHub(player.getInstance())) return;

            CourseSession session = sessions.get(player.getUuid());
            int nextIdx = (session == null) ? 0 : session.nextCheckpointIndex;

            if (nextIdx < CHECKPOINTS.size()) {
                Checkpoint cp = CHECKPOINTS.get(nextIdx);
                if (cp.contains(event.getNewPosition())) {
                    if (session == null) {
                        // Start race
                        session = new CourseSession();
                        sessions.put(player.getUuid(), session);
                        player.sendMessage(Component.text("Course démarrée !", NamedTextColor.GREEN, TextDecoration.BOLD));
                        TKit.playSound(player.getInstance(), player.getPosition(), SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP.name(), Sound.Source.PLAYER, 1f, 1f);
                    } else {
                        session.nextCheckpointIndex++;
                        if (session.nextCheckpointIndex >= CHECKPOINTS.size()) {
                            finishRace(player, session);
                        } else {
                            player.sendMessage(Component.text("Checkpoint " + session.nextCheckpointIndex + "/" + CHECKPOINTS.size(), NamedTextColor.AQUA));
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
                if (session.nextCheckpointIndex < CHECKPOINTS.size()) {
                    Checkpoint cp = CHECKPOINTS.get(session.nextCheckpointIndex);
                    // Only spawn particles for the specific player to avoid cluttering for everyone
                    TKit.spawnParticles(player.getInstance(), Particle.END_ROD, cp.position(), (float) cp.radius() / 2, (float) cp.radius() / 2, (float) cp.radius() / 2, 0.05f, 15);
                }
            }
        }).repeat(TaskSchedule.tick(5)).schedule();
    }

    private static class CourseSession {
        final long startTime = System.currentTimeMillis();
        int nextCheckpointIndex = 0;
        int rocketsUsed = 0;
    }
}
