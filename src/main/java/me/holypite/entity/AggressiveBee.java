package me.holypite.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveBee extends EntityCreature {

    private long lastAttackTime = 0;
    private int ticksSinceLastMove = 0;

    public AggressiveBee() {
        super(EntityType.BEE);
        setNoGravity(true); // Bees fly
        
        // Target Selector only
        addAIGroup(
                List.of(), 
                List.of(
                        new ClosestEntityTarget(this, 15, Player.class)
                )
        );
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Damage Players on Collision
        if (System.currentTimeMillis() - lastAttackTime > 1000) { 
            if (getInstance() != null) {
                getInstance().getEntities().stream()
                    .filter(e -> e instanceof Player)
                    .filter(e -> e.getPosition().distance(getPosition()) < 1.5)
                    .findFirst()
                    .ifPresent(e -> {
                        ((LivingEntity) e).damage(me.holypite.manager.damage.DamageSources.mobAttack(this, 4.0f));
                        // Poison effect usually?
                        // ((LivingEntity) e).addEffect(new Potion(PotionEffect.POISON, (byte) 1, 100));
                        lastAttackTime = System.currentTimeMillis();
                    });
            }
        }
        
        // Custom Flight AI
        Entity target = getTarget();
        
        if (target != null) {
            // Fly towards target
            Vec direction = target.getPosition().add(0, 1, 0).sub(getPosition()).asVec().normalize();
            setVelocity(direction.mul(8)); // Fast flight
            lookAt(target);
        } else {
            // Random flight
            if (ticksSinceLastMove++ > 40) { // Change direction every 2s
                double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                double verticalAngle = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
                
                Vec randomDir = new Vec(Math.cos(angle), verticalAngle, Math.sin(angle)).normalize();
                setVelocity(randomDir.mul(4));
                
                ticksSinceLastMove = 0;
            }
        }
    }
}
