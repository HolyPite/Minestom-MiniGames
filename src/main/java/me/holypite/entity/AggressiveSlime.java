package me.holypite.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveSlime extends EntityCreature {

    private long lastAttackTime = 0;

    public AggressiveSlime(int size) {
        super(EntityType.SLIME);
        
        // Configure Size and Health
        if (getEntityMeta() instanceof SlimeMeta meta) {
            meta.setSize(size);
        }
        
        float maxHealth = size == 1 ? 2.0f : 6.0f;
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        setHealth(maxHealth);
        
        // Target Selector only
        addAIGroup(
                List.of(), 
                List.of(
                        new ClosestEntityTarget(this, 15, Player.class)
                )
        );
    }
    
    // Default constructor for compatibility if needed (defaults to size 1)
    public AggressiveSlime() {
        this(1);
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Damage Players on Collision (with cooldown)
        if (System.currentTimeMillis() - lastAttackTime > 1000) { // 1 sec cooldown
            if (getInstance() != null) {
                getInstance().getEntities().stream()
                    .filter(e -> e instanceof Player)
                    .filter(e -> e.getPosition().distance(getPosition()) < 1.5)
                    .findFirst() // Hit only one player
                    .ifPresent(e -> {
                        ((LivingEntity) e).damage(new Damage(DamageType.MOB_ATTACK, this, this, getPosition(), 4.0f));
                        lastAttackTime = System.currentTimeMillis();
                    });
            }
        }
        
        // Custom Slime Jump AI
        if (isOnGround() && getAliveTicks() % 20 == 0) { // Jump every second approx
            Entity target = getTarget();
            
            if (target != null) {
                // Look at target
                lookAt(target);
                
                // Jump towards target
                Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
                setVelocity(direction.mul(5).withY(7)); 
            } else {
                // Random jump
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                    Vec randomDir = new Vec(Math.cos(angle), 0, Math.sin(angle));
                    
                    // Look in jump direction
                    float yaw = (float) Math.toDegrees(Math.atan2(-randomDir.x(), randomDir.z()));
                    setView(yaw, 0);
                    
                    setVelocity(randomDir.mul(3).withY(7));
                }
            }
        }
    }
}