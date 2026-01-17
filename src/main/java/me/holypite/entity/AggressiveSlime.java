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

    private static final int TARGET_RANGE = 15;
    private static final int JUMP_INTERVAL_TICKS = 15;
    private static final double JUMP_VELOCITY_TARGET = 1.2;
    private static final double JUMP_VELOCITY_RANDOM = 0.7;
    private static final double JUMP_HEIGHT = 0.7;
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
        
        // Target Selector only
        addAIGroup(
                List.of(), 
                List.of(
                        new ClosestEntityTarget(this, TARGET_RANGE, Player.class)
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
        if (System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN_MS) {
            if (getInstance() != null) {
                getInstance().getEntities().stream()
                    .filter(e -> e instanceof Player)
                    .filter(e -> e.getPosition().distance(getPosition()) < COLLISION_RADIUS)
                    .findFirst() // Hit only one player
                    .ifPresent(e -> {
                        ((LivingEntity) e).damage(me.holypite.manager.damage.DamageSources.mobAttack(this, ATTACK_DAMAGE));
                        lastAttackTime = System.currentTimeMillis();
                    });
            }
        }
        
        // Custom Slime Jump AI
        if (isOnGround() && getAliveTicks() % JUMP_INTERVAL_TICKS == 0) {
            Entity target = getTarget();
            
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