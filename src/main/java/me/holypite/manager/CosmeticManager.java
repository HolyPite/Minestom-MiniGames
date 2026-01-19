package me.holypite.manager;

import me.holypite.cosmetics.TrailType;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticManager {

    private final HubManager hubManager;
    private final Map<UUID, TrailType> activeTrails = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastEmissionTime = new ConcurrentHashMap<>();

    // Frequency control (ms)
    private static final long EMISSION_COOLDOWN = 100;

    public CosmeticManager(HubManager hubManager) {
        this.hubManager = hubManager;

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerMoveEvent.class, this::onMove);
    }

    public void setTrail(Player player, TrailType type) {
        if (type == TrailType.NONE) {
            activeTrails.remove(player.getUuid());
            player.sendMessage(Component.text("Cosmétique désactivé.", type.getColor()));
        } else {
            activeTrails.put(player.getUuid(), type);
            player.sendMessage(Component.text("Cosmétique activé : " + type.getDisplayName(), type.getColor()));
        }
    }

    private void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 1. Check if in Hub
        if (!hubManager.isHub(player.getInstance())) return;

        // 2. Check if player has a trail
        TrailType type = activeTrails.get(player.getUuid());
        if (type == null) return;

        // 3. Limit emission rate (Performance & Visuals)
        long now = System.currentTimeMillis();
        long last = lastEmissionTime.getOrDefault(player.getUuid(), 0L);
        if (now - last < EMISSION_COOLDOWN) return;

        // 4. Check if actually moving (ignore head rotation only)
        Pos from = event.getNewPosition(); // Using new position
        // We can just rely on the event firing, or check distance if we want strict "walking" only.
        // For trails, just firing on move event is usually fine, but cooldown handles the spam.

        // 5. Emit Particles
        spawnTrailParticle(player, type);
        lastEmissionTime.put(player.getUuid(), now);
    }

    private void spawnTrailParticle(Player player, TrailType type) {
        Pos pos = player.getPosition();
        
        switch (type) {
            case SMOKE -> TKit.spawnParticles(player.getInstance(), Particle.CLOUD, pos, 0.2f, 0.1f, 0.2f, 0.05f, 3);
            case FLAME -> TKit.spawnParticles(player.getInstance(), Particle.FLAME, pos, 0.2f, 0.1f, 0.2f, 0.05f, 2);
            case HEARTS -> TKit.spawnParticles(player.getInstance(), Particle.HEART, pos.add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 0f, 1);
            case MUSIC -> TKit.spawnParticles(player.getInstance(), Particle.NOTE, pos.add(0, 0.5, 0), 0.3f, 0.3f, 0.3f, 1f, 1); // Speed affects note color
            case RAINBOW -> {
                // Determine color based on time or position
                // Minestom's DUST particle needs color data. 
                // Since TKit.spawnParticles uses generic Particle packet, specialized data handling might be needed.
                // For simplicity, let's use randomly colored dust if possible, or standard colorful particles.
                // Actually, PacketWrapper logic in TKit might not support custom data easily. 
                // Let's stick to valid Enum particles for now or multiple distinct particles.
                
                // Hacky Rainbow: Random basic particles or specific colored notes
                // Let's use TOTEM_OF_UNDYING (Green/Yellow) + DRAGON_BREATH (Purple) + FLAME (Red)
                TKit.spawnParticles(player.getInstance(), Particle.WAX_OFF, pos, 0.3f, 0.2f, 0.3f, 0f, 2);
                TKit.spawnParticles(player.getInstance(), Particle.WAX_ON, pos, 0.3f, 0.2f, 0.3f, 0f, 2);
            }
        }
    }
}
