package me.holypite.manager;

import me.holypite.manager.damage.DamageSources;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.item.Material;

public class PvpManager {

    private final EventNode<Event> pvpNode;

    public PvpManager() {
        this.pvpNode = EventNode.all("pvp-node");
        
        // Attack Management
        pvpNode.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof Player attacker)) return;
            if (!(event.getTarget() instanceof LivingEntity victim)) return;

            // Attack Logic
            handleAttack(attacker, victim);
        });
    }

    private void handleAttack(Player attacker, LivingEntity victim) {
        // Invulnerability and Knockback are now handled by DamageManager (listening to EntityDamageEvent)
        
        // Calculate Damage
        float damage = getDamageFromItem(attacker.getItemInMainHand().material());
        
        // Apply Damage using generalized source
        victim.damage(DamageSources.playerAttack(attacker, damage));
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