package me.holypite.entity;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveSlime extends EntityCreature {

    private static final int TARGET_RANGE = 15;
    private static final int JUMP_INTERVAL_TICKS = 10;
    private static final double JUMP_VELOCITY_TARGET = 8.0;
    private static final double JUMP_VELOCITY_RANDOM = 4.0;
    private static final double JUMP_HEIGHT = 0.8;
    private static final float ATTACK_DAMAGE = 4.0f;
    private static final long ATTACK_COOLDOWN_MS = 1000;
    private static final double COLLISION_RADIUS = 1.5;

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
        
        // Add a goal to keep AI ticking and a target selector
        addAIGroup(
                List.of(new RandomStrollGoal(this, 20)), 
                List.of(new ClosestEntityTarget(this, TARGET_RANGE, Player.class))
        );
    }
    
    public AggressiveSlime() {
        this(1);
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (getInstance() == null) return;

        // Damage Players on Collision (with cooldown)
        if (System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN_MS) {
            getInstance().getEntities().stream()
                .filter(e -> e instanceof Player)
                .filter(e -> e.getPosition().distance(getPosition()) < COLLISION_RADIUS)
                .findFirst() 
                .ifPresent(e -> {
                    ((LivingEntity) e).damage(me.holypite.manager.damage.DamageSources.mobAttack(this, ATTACK_DAMAGE));
                    lastAttackTime = System.currentTimeMillis();
                });
        }
        
        // Custom Slime Jump AI
        if (isOnGround() && getAliveTicks() % JUMP_INTERVAL_TICKS == 0) {
            Entity target = getTarget();
            
            // Manual fallback
            if (target == null) {
                target = TKit.getNearestPlayer(getInstance(), getPosition(), TARGET_RANGE, true);
            }

            if (target != null) {
                // Look at target
                lookAt(target);
                
                // Jump towards target
                Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
                setVelocity(direction.mul(JUMP_VELOCITY_TARGET).withY(JUMP_HEIGHT)); 
            } else {
                // Random jump
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                    Vec randomDir = new Vec(Math.cos(angle), 0, Math.sin(angle));
                    
                    // Look in jump direction
                    float yaw = (float) Math.toDegrees(Math.atan2(-randomDir.x(), randomDir.z()));
                    setView(yaw, 0);
                    
                    setVelocity(randomDir.mul(JUMP_VELOCITY_RANDOM).withY(JUMP_HEIGHT));
                }
            }
        }
    }
}
