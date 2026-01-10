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
        // Calculate direction vector: Attacker -> Victim
        Vec direction = victim.getPosition().sub(attacker.getPosition()).asVec();
        direction = direction.withY(0).normalize(); // Horizontal only logic first
        
        // Strength
        double strength = 12.0; // Velocity multiplier (needs tuning, Minestom units are blocks/tick * conversion)
        // Note: Minestom velocity is approx blocks/tick * 8000/20 in packets? No, setVelocity uses server units.
        // Let's try a reasonable value. Standard jump is ~10 Y.
        
        Vec knockback = direction.mul(strength).withY(4.0); // Add lift
        
        // Apply (Add to current velocity or set?)
        // Usually setting adds impulse if we just add.
        // But to be sure we override previous motion or add to it?
        // Let's add to simulate impact.
        victim.setVelocity(victim.getVelocity().add(knockback));
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
