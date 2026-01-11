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
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveSlime extends EntityCreature {

    public AggressiveSlime() {
        super(EntityType.SLIME);
        
        // Target Selector only
        addAIGroup(
                List.of(), // No movement goals, we handle it manually
                List.of(
                        new ClosestEntityTarget(this, 15, Player.class)
                )
        );
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Damage Players on Collision
        if (getInstance() != null) {
            getInstance().getEntities().stream()
                .filter(e -> e instanceof Player)
                .filter(e -> e.getPosition().distance(getPosition()) < 1.5)
                .forEach(e -> ((LivingEntity) e).damage(new Damage(DamageType.MOB_ATTACK, this, this, getPosition(), 4.0f)));
        }
        
        // Custom Slime Jump AI
        if (isOnGround() && getAliveTicks() % 20 == 0) { // Jump every second approx
            Entity target = getTarget();
            
            if (target != null) {
                // Jump towards target
                Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
                setVelocity(direction.mul(5).withY(7)); // Jump forward
                lookAt(target);
            } else {
                // Random jump
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                    Vec randomDir = new Vec(Math.cos(angle), 0, Math.sin(angle));
                    setVelocity(randomDir.mul(3).withY(7));
                    setView((float) Math.toDegrees(angle), 0);
                }
            }
        }
    }
}