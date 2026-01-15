package me.holypite.manager.damage;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.holypite.event.CustomDeathEvent;
import net.minestom.server.entity.Player;

public class DamageManager {

    private final Map<LivingEntity, Long> lastDamageTime = new ConcurrentHashMap<>();
    private static final long INVULNERABILITY_MS = 500;

    public DamageManager() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(EntityDamageEvent.class, this::onEntityDamage);
    }

    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        
        Damage damage = event.getDamage();
        String typeName = damage.getType().name();
        float amount = damage.getAmount();

        // 1. Invulnerability Check
        if (shouldTriggerInvulnerability(typeName)) {
            long now = System.currentTimeMillis();
            long last = lastDamageTime.getOrDefault(victim, 0L);
            if (now - last < INVULNERABILITY_MS) {
                event.setCancelled(true);
                return;
            }
            lastDamageTime.put(victim, now);
        }

        // 2. Armor Reduction
        if (!bypassesArmor(typeName)) {
            float defense = (float) victim.getAttributeValue(Attribute.ARMOR);
            float toughness = (float) victim.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
            
            if (defense > 0) {
                float f = 2.0f + toughness / 4.0f;
                float reduction = Math.min(20.0f, Math.max(defense / 5.0f, defense - amount / f)) / 25.0f;
                amount = amount * (1.0f - reduction);
            }
        }

        // Apply Damage Manually and Cancel Event to override Minestom behavior
        event.setCancelled(true);
        
        if (amount > 0) {
            float finalDamage = amount;
            
            // Check for Fatal Damage on Player
            float currentHealth = victim.getHealth();
            if (victim instanceof Player player && (currentHealth - finalDamage) <= 0) {
                // Prevent death screen by NOT applying damage (or healing)
                // Fire custom death event
                CustomDeathEvent deathEvent = new CustomDeathEvent(player, damage.getAttacker(), typeName);
                MinecraftServer.getGlobalEventHandler().call(deathEvent);
                
                // Ensure player stays alive client-side until Ghost Mode takes over
                player.setHealth((float) player.getAttributeValue(Attribute.MAX_HEALTH)); 
                return;
            }

            // Apply Health for non-fatal or non-player
            float newHealth = currentHealth - finalDamage;
            victim.setHealth(newHealth);
            
            if (newHealth <= 0 && !(victim instanceof Player)) {
                victim.kill();
            }
            
            // Visuals
            victim.triggerStatus((byte) 2); // Hurt Animation (Red Tint)
            
            // Knockback
            applyKnockback(victim, damage, finalDamage);
        }
    }
    
    private boolean shouldTriggerInvulnerability(String type) {
        return type.contains("player_attack") || 
               type.contains("mob_attack") || 
               type.contains("explosion") || 
               type.contains("arrow") || 
               type.contains("trident") ||
               type.contains("thrown");
    }
    
    private boolean bypassesArmor(String type) {
        return type.contains("fall") || 
               type.contains("out_of_world") || 
               type.contains("void") || 
               type.contains("magic") || 
               type.contains("wither") ||
               type.contains("drown") ||
               type.contains("starve");
    }

    private void applyKnockback(LivingEntity victim, Damage damage, float amount) {
        double resistance = victim.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
        if (Math.random() < resistance) return;

        Point sourcePosPoint = damage.getSourcePosition();
        Vec sourcePos = sourcePosPoint != null ? sourcePosPoint.asVec() : null;
        
        if (sourcePos == null && damage.getAttacker() != null) {
            sourcePos = damage.getAttacker().getPosition().asVec();
        }

        if (sourcePos != null) {
            Vec direction = victim.getPosition().sub(sourcePos).asVec();
            
            if (!damage.getType().name().contains("explosion")) {
                direction = direction.withY(0);
            }
            
            if (direction.lengthSquared() < 1.0E-4D) { 
                 direction = new Vec(Math.random() - 0.5, 0, Math.random() - 0.5);
            }
            
            direction = direction.normalize();

            double strength;
            if (damage.getType().name().contains("explosion")) {
                 strength = amount * 0.5; 
            } else {
                 strength = 8.0; 
                 if (damage.getType().name().contains("arrow")) strength = 5.0; 
            }
            
            Vec knockback = direction.mul(strength);
            
            if (!damage.getType().name().contains("explosion")) {
                knockback = knockback.withY(4.0);
            } else {
                knockback = knockback.withY(strength * 0.5); 
            }
            
            victim.setVelocity(victim.getVelocity().add(knockback));
        }
    }
}
