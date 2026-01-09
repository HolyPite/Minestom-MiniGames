package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.item.Material;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PvpManager {

    private final EventNode<Event> pvpNode;
    private final Map<LivingEntity, Long> lastHitMap = new ConcurrentHashMap<>();
    private static final long INVULNERABILITY_MS = 500; // 0.5s like Vanilla

    public PvpManager() {
        this.pvpNode = EventNode.all("pvp-node");
        
        // Attack Management
        pvpNode.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof Player attacker)) return;
            if (!(event.getTarget() instanceof LivingEntity victim)) return;

            // Attack Logic
            handleAttack(attacker, victim);
        });
        
        // Damage Management (optional custom logic, e.g. armor)
        pvpNode.addListener(EntityDamageEvent.class, event -> {
            // Here we could reduce damage based on armor
        });
    }

    private void handleAttack(Player attacker, LivingEntity victim) {
        // 1. Invulnerability Check
        long now = System.currentTimeMillis();
        long lastHit = lastHitMap.getOrDefault(victim, 0L);
        if (now - lastHit < INVULNERABILITY_MS) {
            return; // Target is invulnerable
        }
        lastHitMap.put(victim, now);

        // 2. Calculate Damage
        float damage = getDamageFromItem(attacker.getItemInMainHand().material());
        
        // 3. Apply Damage
        // Using generic player attack damage type
        victim.damage(new Damage(DamageType.PLAYER_ATTACK, attacker, attacker, attacker.getPosition(), damage));
        
        // 4. Knockback
        // Minestom handles basic knockback if we use takeKnockback with the right params
        // x and z are essentially the direction vector components based on attacker's yaw
        double yaw = Math.toRadians(attacker.getPosition().yaw());
        double kbX = Math.sin(yaw);
        double kbZ = -Math.cos(yaw);
        
        victim.takeKnockback(0.4f, kbX, kbZ);
    }
    
    private float getDamageFromItem(Material material) {
        // Simple mapping, can be expanded
        if (material == Material.DIAMOND_SWORD) return 7.0f;
        if (material == Material.IRON_SWORD) return 6.0f;
        if (material == Material.STONE_SWORD) return 5.0f;
        if (material == Material.WOODEN_SWORD) return 4.0f;
        return 1.0f; // Fist
    }

    public EventNode<Event> getEventNode() {
        return pvpNode;
    }
}
